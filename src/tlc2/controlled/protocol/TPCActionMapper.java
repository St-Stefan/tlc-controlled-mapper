package tlc2.controlled.protocol;

import tlc2.tool.Action;
import tlc2.value.impl.IntValue;
import tlc2.value.impl.Value;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

// TwoPhaseCommit (TwoPhaseCommit with parametric number of transaction requests) action mapper
public class TPCActionMapper extends BaseActionMapper {

    private HashMap<String, List<Action>> enabledActionMap;

    public TPCActionMapper(List<Action> enabledActions) {
        super(enabledActions);
        this.enabledActionMap = new HashMap<String, List<Action>>();
        for (Action a: enabledActions) {
            String name = a.getName().toString();
            List<Action> currentActions;
            if (!this.enabledActionMap.containsKey(name)) {
                currentActions = new ArrayList<Action>();
            } else {
                currentActions = this.enabledActionMap.get(name);
            }
            currentActions.add(a);
            this.enabledActionMap.put(name, currentActions);
        }
    }

    private Optional<Integer> getRequestId(AbstractAction abstractAction) {
        if(!abstractAction.params.containsKey("request_id")) {
            return Optional.empty();
        }
        Object requestIDObject = abstractAction.params.get("request_id");
        if (requestIDObject instanceof String) {
            Integer requestId = Integer.parseInt((String) abstractAction.params.get("request_id"));
            return Optional.of(requestId);
        }
        try {
            Double requestID = (Double) requestIDObject;
            return Optional.of(requestID.intValue());
        } catch(Exception e) {
            return Optional.empty();
        }
    }

    public Action mapAction(AbstractAction abstractAction) {
        try{
            String message = abstractAction.name;
            
            switch (message) {
                case "SendEvent":
                    String event = (String) abstractAction.params.get("event");
                    switch (event) {
                        case "TwoPhaseCommit.RequestEvent":
                            Optional<Integer> requestId = getRequestId(abstractAction);
                            if (requestId.isEmpty()) {
                                return null;
                            }
                            int request = requestId.get();
                            if (!this.enabledActionMap.containsKey("TMSendPrepareReq")) {
                                return null;
                            }
                            for (Action a: this.enabledActionMap.get("TMSendPrepareReq")) {
                                Map<String, Value> params = a.getParams();
                                if (params.containsKey("i")) {
                                    IntValue i = (IntValue) params.get("i");
                                    if (i.val == request) {
                                        return a;
                                    }
                                }
                            }
                            break;
                            // Map to "TMSendPrepareReq" of the corresponding request
                        case "TwoPhaseCommit.PreparedEvent":
                            int sender = Integer.parseInt((String) abstractAction.params.get("sender_id"));

                            requestId = getRequestId(abstractAction);
                            if (requestId.isEmpty()) {
                                return null;
                            }
                            request = requestId.get();
                            if (!this.enabledActionMap.containsKey("RMSendPrepared")) {
                                return null;
                            }
                            for (Action a: this.enabledActionMap.get("RMSendPrepared")) {
                                Map<String, Value> params = a.getParams();
                                if (params.containsKey("i") && params.containsKey("r")) {
                                    IntValue i = (IntValue) params.get("i");
                                    IntValue r = (IntValue) params.get("r");
                                    if (i.val == request && r.val == sender) {
                                        return a;
                                    }
                                }
                            }
                            break;
                            // Map to "RMSendPrepared"
                        case "TwoPhaseCommit.AbortEvent":
                            sender = Integer.parseInt((String) abstractAction.params.get("sender_id"));
                            requestId = getRequestId(abstractAction);
                            if (requestId.isEmpty()) {
                                return null;
                            }
                            request = requestId.get();
                            if (!this.enabledActionMap.containsKey("RMSendAborted")) {
                                return null;
                            }
                            for (Action a: this.enabledActionMap.get("RMSendAborted")) {
                                Map<String, Value> params = a.getParams();
                                if (params.containsKey("i") && params.containsKey("r")) {
                                    IntValue i = (IntValue) params.get("i");
                                    IntValue r = (IntValue) params.get("r");
                                    if (i.val == request && r.val == sender) {
                                        return a;
                                    }
                                }
                            }
                            break;
                            // Map to "RMSendAborted"
                        case "TwoPhaseCommit.GlobalCommitEvent":
                            requestId = getRequestId(abstractAction);
                            if (requestId.isEmpty()) {
                                return null;
                            }
                            request = requestId.get();
                            if (!this.enabledActionMap.containsKey("TMSendGlobalCommit")) {
                                return null;
                            }
                            for (Action a: this.enabledActionMap.get("TMSendGlobalCommit")) {
                                Map<String, Value> params = a.getParams();
                                if (params.containsKey("i")) {
                                    IntValue i = (IntValue) params.get("i");
                                    if (i.val == request) {
                                        return a;
                                    }
                                }
                            }
                            break;
                            // Map to "TMSendGlobalCommit"
                        case "TwoPhaseCommit.GlobalAbortEvent":
                            break;
                            // Map to "TMSendGlobalAbort" but the model doesn't have corresponding action for this
                    }
                    break;
                case "ReceiveEvent":
                    event = (String) abstractAction.params.get("event");
                    Optional<Integer> requestId = getRequestId(abstractAction);
                    if (requestId.isEmpty()) {
                        return null;
                    }
                    int i_val = requestId.get();
                    int r_val = -1;
                    String actionMapKey = "";
                    switch (event) {
                        case "TwoPhaseCommit.RequestEvent":
                            r_val = Integer.parseInt((String) abstractAction.params.get("receiver_id"));
                            actionMapKey = "RMRcvPrepareReq";
                            break;
                            // Map to "RMRcvPrepareReq"
                        case "TwoPhaseCommit.PreparedEvent":
                            r_val = Integer.parseInt((String) abstractAction.params.get("sender_id"));
                            actionMapKey = "TMRcvPrepared";
                            break;
                            // Map to "TMRcvPrepared"
                        case "TwoPhaseCommit.AbortEvent":
                            r_val = Integer.parseInt((String) abstractAction.params.get("sender_id"));
                            actionMapKey = "TMRcvAborted";
                            break;
                            // Map to "TMRcvAborted"
                        case "TwoPhaseCommit.GlobalAbortEvent":
                            r_val = Integer.parseInt((String) abstractAction.params.get("receiver_id"));
                            actionMapKey = "RMRcvGlobalAbort";
                            break;
                            // Map to "RMRcvGlobalAbort"
                        case "TwoPhaseCommit.GlobalCommitEvent":
                            r_val = Integer.parseInt((String) abstractAction.params.get("receiver_id"));
                            actionMapKey = "RmRcvGlobalCommit";
                            break;
                            // Map to "RmRcvGlobalCommit"
                    }
                    if (actionMapKey != "" && this.enabledActionMap.containsKey(actionMapKey)) {
                        for (Action a: this.enabledActionMap.get(actionMapKey)) {
                            Map<String, Value> params = a.getParams();
                            if (params.containsKey("i") && params.containsKey("r")) {
                                IntValue i = (IntValue) params.get("i");
                                IntValue r = (IntValue) params.get("r");
                                if (i.val == i_val && r.val == r_val) {
                                    return a;
                                }
                            }
                        }
                    }
                    break;
            }
            return null;
        } catch (Exception e) {
            System.out.println("[TPCActionMapper] Invalid action");
            System.out.println("Error: "+e.getMessage());
        }

        return null;
    }

}
