package tlc2.controlled.protocol;

import tlc2.tool.Action;
import tlc2.value.impl.IntValue;
import tlc2.value.impl.Value;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Mapper for RecoveryAndCommitSpec/AccordSpec.tla.
// Single-shard: all TLC actions take (p, id), wrappers are Handle*Wrapper.
// Crash/Restart are the exception: they take only a process id, read the same
// way as everywhere else in this file ("to", falling back to "p"; no "id").
//
// Java trace field conventions (post-flip):
//   "type"   — message type (TypePreAccept, TypeStable, TypeCommit, TypeApply, TypeRead, ...)
//   "phaseq" — Commit.Kind value on TypeStable / TypeCommit events
//
// Routing rules:
//   TypeStable  phaseq=StableFastPath/StableMediumPath/StableWithTxnAndDeps
//               → HandleCommitWrapper + HandleStableWrapper  (combined commit+stable, no prior TypeCommit)
//   TypeStable  phaseq=StableSlowPath (or absent)
//               → HandleStableWrapper only  (separate stable, TypeCommit already fired HandleCommitWrapper)
//   TypeCommit  any phaseq → HandleCommitWrapper only
//   TypeApply   → Execute(to, id)  (replica executes after reaching StablePhase)
//   TypeRead    → skipped (no read messages in RecoveryAndCommitSpec)
public class RecoveryCommitActionMapper extends BaseActionMapper {

    // Tracks (coordinator*1000+id) pairs for which StartRecover has been synthesized this trace.
    // Cleared on reset so each trace starts fresh.
    private final Set<Long> startRecoverFired = new HashSet<>();

    public RecoveryCommitActionMapper(List<Action> enabledActions) {
        super(enabledActions);
        System.out.println("[RecoveryCommitActionMapper] Available TLC action names: " + this.enabledActionMap.keySet());
    }

    private int intParam(AbstractAction a, String key, int defaultVal) {
        if (a.params == null || !a.params.containsKey(key)) return defaultVal;
        Object v = a.params.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) { try { return Integer.parseInt((String) v); } catch (NumberFormatException ignored) {} }
        return defaultVal;
    }

    private int intParam(AbstractAction a, String key) {
        Object v = a.params.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) return Integer.parseInt((String) v);
        throw new IllegalArgumentException("Missing required param: " + key);
    }

    private String strParam(AbstractAction a, String key) {
        Object v = a.params.get(key);
        return v instanceof String ? (String) v : null;
    }

    private Action mapPIdAction(String key, int p, int id) {
        if (!enabledActionMap.containsKey(key)) return null;
        for (Action candidate : enabledActionMap.get(key)) {
            Map<String, Value> params = candidate.getParams();
            try {
                if (((IntValue) params.get("p")).val == p && ((IntValue) params.get("id")).val == id)
                    return candidate;
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Action mapPIdTAction(String key, int p, int id, int t) {
        if (!enabledActionMap.containsKey(key)) return null;
        for (Action candidate : enabledActionMap.get(key)) {
            Map<String, Value> params = candidate.getParams();
            try {
                if (((IntValue) params.get("p")).val == p &&
                    ((IntValue) params.get("id")).val == id &&
                    ((IntValue) params.get("t")).val == t)
                    return candidate;
            } catch (Exception ignored) {}
        }
        return null;
    }

    // Crash(p)/Restart(p) take a single process-id parameter, unlike every other
    // action in this spec which is keyed on (p, id).
    private Action mapPAction(String key, int p) {
        if (!enabledActionMap.containsKey(key)) return null;
        for (Action candidate : enabledActionMap.get(key)) {
            Map<String, Value> params = candidate.getParams();
            try {
                if (((IntValue) params.get("p")).val == p)
                    return candidate;
            } catch (Exception ignored) {}
        }
        return null;
    }

    // Returns true for Commit.Kind values that combine commit+stable in one Java message.
    // StableSlowPath is excluded — it is always preceded by a separate TypeCommit event.
    private boolean isCombinedStable(String phaseq) {
        switch (phaseq) {
            case "StableFastPath": case "StableMediumPath": case "StableWithTxnAndDeps":
                return true;
            default: return false;
        }
    }

    // Handles the two cases that expand one trace event into multiple TLC actions:
    //   TypeStable(combined) → HandleCommitWrapper then HandleStableWrapper
    //   TypeApply            → Execute
    // All other Deliver events fall through to mapAction → routeByType.
    @Override
    public List<ActionWrapper> mapListOfActions(String actionString) {
        List<ActionWrapper> out = new ArrayList<>();
        for (AbstractAction a : listFromJson(actionString)) {
            if (a.isReset()) { startRecoverFired.clear(); out.add(ActionWrapper.reset()); continue; }
            if ("Deliver".equals(a.name)) {
                String type   = strParam(a, "type");
                String phaseq = strParam(a, "phaseq");
                int to = intParam(a, "to", intParam(a, "p", -1));
                int id = intParam(a, "id");

                if ("TypeApply".equals(type)) {
                    Action exec = mapPIdAction("Execute", to, id);
                    if (exec != null) out.add(ActionWrapper.action(exec));
                    continue;
                }
                if ("TypeStable".equals(type) && phaseq != null && isCombinedStable(phaseq)) {
                    Action commit = mapPIdAction("HandleCommitWrapper", to, id);
                    Action stable = mapPIdAction("HandleStableWrapper", to, id);
                    if (commit != null) out.add(ActionWrapper.action(commit));
                    if (stable != null) out.add(ActionWrapper.action(stable));
                    continue;
                }
                if ("TypeRecover".equals(type)) {
                    int from = intParam(a, "from", -1);
                    if (from >= 0) {
                        long srKey = (long) from * 100000 + id;
                        if (!startRecoverFired.contains(srKey)) {
                            Action sr = mapPIdAction("StartRecover", from, id);
                            if (sr != null) out.add(ActionWrapper.action(sr));
                            startRecoverFired.add(srKey);
                        }
                    }
                }
            }
            Action mapped = mapAction(a);
            if (mapped != null) out.add(ActionWrapper.action(mapped));
        }
        return out;
    }

    private Action routeByType(String type, int to, int id) {
        switch (type) {
            case "TypePreAccept":   case "PRE_ACCEPT_REQ":  case "PreAccept":
                return mapPIdAction("HandlePreAcceptWrapper", to, id);
            case "TypeAccept":      case "ACCEPT_REQ":      case "Accept":
                return mapPIdAction("HandleAcceptWrapper", to, id);
            case "TypeCommit":      case "COMMIT_REQ":      case "Commit":
            case "CommitSlowPath":  case "CommitWithTxn":
                return mapPIdAction("HandleCommitWrapper", to, id);
            case "TypeStable":      case "STABLE_REQ":      case "Stable":
            case "StableSlowPath":
                return mapPIdAction("HandleStableWrapper", to, id);
            case "TypeBeginRecovery":
                return mapPIdAction("StartRecover", to, id);
            case "TypeRecover":     case "RECOVER_REQ":     case "Recover":
                return mapPIdAction("HandleRecoverWrapper", to, id);
            case "TypePreAcceptOK": case "PRE_ACCEPT_RSP":  case "PreAcceptOK":
                return mapPIdAction("HandlePreAcceptOK", to, id);
            case "TypeAcceptOK":    case "ACCEPT_RSP":      case "AcceptOK":
                return mapPIdAction("HandleAcceptOK", to, id);
            case "TypeCommitOK":    case "COMMIT_RSP":      case "CommitOK":
                return mapPIdAction("HandleCommitOK", to, id);
            case "TypeRecoverOK":   case "RECOVER_RSP":     case "RecoverOK":
                return mapPIdAction("HandleRecoverOK", to, id);
            case "TypeApply":
                return mapPIdAction("Execute", to, id);
            case "TypeRead": case "TypeReadOk": case "TypeReadOK":
                return null;
            default:
                System.out.println("[RecoveryCommitActionMapper] Unknown type/kind: " + type);
                return null;
        }
    }

    @Override
    protected Action mapAction(AbstractAction a) {
        try {
            int to = intParam(a, "to", intParam(a, "p", -1));

            // Crash/Restart have no "id" -- handle before the required-id parse below,
            // which would otherwise throw and get logged as a mapping error.
            if (a.name.equals("Crash") || a.name.equals("Restart"))
                return mapPAction(a.name, to);

            int id = intParam(a, "id");

            switch (a.name) {
                case "Submit": {
                    int t = intParam(a, "t", -1);
                    if (t >= 0) return mapPIdTAction("Submit", to, id, t);
                    return mapPIdAction("Submit", to, id);
                }
                case "HandlePreAcceptOK": case "HandleAcceptOK": case "HandleCommitOK":
                case "StartRecover":      case "HandleRecoverOK": case "HandlePostWaiting":
                case "Execute":
                    return mapPIdAction(a.name, to, id);
                case "HandlePreAccept":  return mapPIdAction("HandlePreAcceptWrapper", to, id);
                case "HandleAccept":     return mapPIdAction("HandleAcceptWrapper",    to, id);
                case "HandleCommit":     return mapPIdAction("HandleCommitWrapper",    to, id);
                case "HandleStable":     return mapPIdAction("HandleStableWrapper",    to, id);
                case "HandleRecover":    return mapPIdAction("HandleRecoverWrapper",   to, id);
                case "Deliver": {
                    String kind = strParam(a, "kind");
                    if (kind != null) return routeByType(kind, to, id);
                    String type = strParam(a, "type");
                    if (type != null) return routeByType(type, to, id);
                    System.out.println("[RecoveryCommitActionMapper] Deliver missing both 'kind' and 'type'");
                    return null;
                }
                default:
                    System.out.println("[RecoveryCommitActionMapper] Unknown action name: " + a.name);
                    return null;
            }
        } catch (Exception e) {
            System.out.println("[RecoveryCommitActionMapper] Error mapping '" + a.name + "': " + e.getMessage());
            return null;
        }
    }
}