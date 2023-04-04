package tlc2.controlled.protocol;

import java.nio.charset.StandardCharsets;
import java.util.*;
import tlc2.tool.Action;
import tlc2.value.impl.BoolValue;
import tlc2.value.impl.IntValue;
import tlc2.value.impl.Value;

public class RaftActionMapper extends BaseActionMapper{

    public RaftActionMapper(List<Action> enabledActions) {
        super(enabledActions);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> getParam(AbstractAction abstractAction, String param) {
        if(!abstractAction.params.containsKey(param)) {
            return Optional.empty();
        }
        Object paramValue = abstractAction.params.get(param);
        try {
            T result = (T) paramValue;
            return Optional.of(result);
        } catch(Exception e) {
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<List<Map<String, Object>>> getEntries(AbstractAction abstractAction) {
        if(!abstractAction.params.containsKey("entries")) {
            return Optional.empty();
        }
        Object paramValue = abstractAction.params.get("entries");
        try {
            List<Map<String, Object>> entries = (List<Map<String, Object>>) paramValue;
            return Optional.of(entries);
        } catch(Exception e) {
            return Optional.empty();
        }
    }

    protected Action mapClientRequest(int requestID, int leader) {
        for(Action a: this.enabledActionMap.get("ClientRequest")) {
            Map<String, Value> params = a.getParams();
            if(params.containsKey("i") && params.containsKey("v")) {
                IntValue i = (IntValue) params.get("i");
                IntValue v = (IntValue) params.get("v");
                if (i.val == leader && v.val == requestID) {
                    return a;
                }
            }
        }
        return null;
    }

    protected Action mapBecomeLeader(int node) {
        for (Action a : this.enabledActionMap.get("BecomeLeader")) {
            Map<String, Value> params = a.getParams();
            if(params.containsKey("i")) {
                IntValue i = (IntValue) params.get("i");
                if (i.val == node) {
                    return a;
                }
            }
        }
        return null;
    }

    protected Action mapTimeout(int node) {
        for (Action a : this.enabledActionMap.get("Timeout")) {
            Map<String, Value> params = a.getParams();
            if(params.containsKey("i")) {
                IntValue i = (IntValue) params.get("i");
                if (i.val == node) {
                    return a;
                }
            }
        }
        return null;
    }

    protected Action mapAdvanceCommitIndex(int node) {
        for (Action a : this.enabledActionMap.get("AdvanceCommitIndex")) {
            Map<String, Value> params = a.getParams();
            if(params.containsKey("i")) {
                IntValue i = (IntValue) params.get("i");
                if (i.val == node) {
                    return a;
                }
            }
        }
        return null;
    }

    protected Action mapHandleRequestVoteRequest(int i, int j, int lTerm, int lIndex, int term) {
        for (Action a: this.enabledActionMap.get("HandleRequestVoteRequest")) {
            Map<String, Value> params = a.getParams();
            IntValue iP = (IntValue) params.get("i");
            IntValue jP = (IntValue) params.get("j");
            IntValue lTermP = (IntValue) params.get("lTerm");
            IntValue lIndexP = (IntValue) params.get("lIndex");
            IntValue termP = (IntValue) params.get("term");
            if (iP.val == i && jP.val == j && lTermP.val == lTerm && lIndexP.val == lIndex && termP.val == term) {
                return a;
            }
        }
        return null;
    }

    protected Action mapHandleRequestVoteResponse(int i, int j, int term, boolean grant) {
        for (Action a: this.enabledActionMap.get("HandleRequestVoteResponse")) {
            Map<String, Value> params = a.getParams();
            IntValue iP = (IntValue) params.get("i");
            IntValue jP = (IntValue) params.get("j");
            IntValue termP  = (IntValue) params.get("term");
            BoolValue grantP = (BoolValue) params.get("grant");
            if (iP.val == i && jP.val == j && grant == grantP.val && termP.val == term) {
                return a;
            }
        }
        return null;
    }

    protected Action mapHandleAppendEntriesResponse(int i, int j, int term, boolean success, int mIndex) {
        for (Action a: this.enabledActionMap.get("HandleAppendEntriesResponse")) {
            Map<String, Value> params = a.getParams();
            IntValue iP = (IntValue) params.get("i");
            IntValue jP = (IntValue) params.get("j");
            IntValue termP  = (IntValue) params.get("term");
            BoolValue successP = (BoolValue) params.get("success");
            IntValue mIndexP = (IntValue) params.get("mIndex");
            if (iP.val == i && jP.val == j && success == successP.val && termP.val == term && mIndex == mIndexP.val) {
                return a;
            }
        }
        return null;
    }

    protected Action mapHandleAppendEntriesRequest(int i,int j,int pLogIndex,int pLogTerm,int term, List<Map<String, Object>> entries ,int cIndex) {
        if(entries.size() == 0) {
            for (Action a: this.enabledActionMap.get("HandleNilAppendEntriesRequest")) {
                Map<String, Value> params = a.getParams();
                IntValue iP = (IntValue) params.get("i");
                IntValue jP = (IntValue) params.get("j");
                IntValue termP  = (IntValue) params.get("term");
                IntValue pLogIndexP  = (IntValue) params.get("pLogIndex");
                IntValue pLogTermP  = (IntValue) params.get("pLogTerm");
                IntValue cIndexP = (IntValue) params.get("cIndex");
                if (iP.val == i && jP.val == j && termP.val == term && cIndex == cIndexP.val && pLogIndex == pLogIndexP.val && pLogTerm == pLogTermP.val) {
                    return a;
                }
            }
        } else {
            Double eTerm = (Double) entries.get(0).get("Term");
            int eValue = 0;
            if(entries.get(0).containsKey("Data")) {
                byte[] data = (byte[]) entries.get(0).get("Data");
                String s = new String(data, StandardCharsets.UTF_8);
                try {
                    eValue = Integer.parseInt(s);
                } catch (Exception e) {
                    return null;
                }
            }
            for (Action a: this.enabledActionMap.get("HandleAppendEntriesRequest")) {
                Map<String, Value> params = a.getParams();
                IntValue iP = (IntValue) params.get("i");
                IntValue jP = (IntValue) params.get("j");
                IntValue termP  = (IntValue) params.get("term");
                IntValue pLogIndexP  = (IntValue) params.get("pLogIndex");
                IntValue pLogTermP  = (IntValue) params.get("pLogTerm");
                IntValue entryTermP = (IntValue) params.get("entryTerm");
                IntValue entryValueP = (IntValue) params.get("entryValue");
                IntValue cIndexP = (IntValue) params.get("cIndex");
                if (iP.val == i && 
                    jP.val == j && 
                    termP.val == term && 
                    cIndex == cIndexP.val && 
                    pLogIndex == pLogIndexP.val && 
                    pLogTerm == pLogTermP.val && 
                    eTerm.intValue() == entryTermP.val &&
                    eValue == entryValueP.val
                ) {
                    return a;
                }
            }
        }
        return null;
    }

    public Action mapAction(AbstractAction abstractAction) {
        try {
            switch (abstractAction.name) {
                case "ClientRequest":
                    Optional<Double> requestID = this.getParam(abstractAction, "request");
                    Optional<Double> leader = this.getParam(abstractAction, "leader");
                    if(requestID.isEmpty() || leader.isEmpty()) {
                        return null;
                    }
                    return mapClientRequest(requestID.get().intValue(), leader.get().intValue());
                case "SendMessage":
                    break;
                case "DeliverMessage":
                    Optional<String> messageType = this.getParam(abstractAction, "type");
                    if(messageType.isEmpty()) {
                        return null;
                    }
                    switch (messageType.get()) {
                        case "MsgVote":
                            // Handle request vote 
                            Optional<Double> sender = this.getParam(abstractAction, "from");
                            Optional<Double> receiver = this.getParam(abstractAction, "to");
                            Optional<Double> term = this.getParam(abstractAction, "term");
                            Optional<Double> logTerm  = this.getParam(abstractAction, "log_term");
                            Optional<Double> logIndex = this.getParam(abstractAction, "index");
                            return mapHandleRequestVoteRequest(
                                sender.get().intValue(), 
                                receiver.get().intValue(), 
                                logTerm.get().intValue(), 
                                logIndex.get().intValue(), 
                                term.get().intValue()
                            );
                        case "MsgVoteResp":
                            // Handle request vote response
                            sender = this.getParam(abstractAction, "from");
                            receiver = this.getParam(abstractAction, "to");
                            term = this.getParam(abstractAction, "term");
                            Optional<Boolean> grant = this.getParam(abstractAction, "reject");
                            return mapHandleRequestVoteResponse(
                                sender.get().intValue(), 
                                receiver.get().intValue(),
                                term.get().intValue(),
                                !grant.get().booleanValue()
                            );
                        case "MsgApp":
                            // Handle append entries request
                            sender = this.getParam(abstractAction, "from");
                            receiver = this.getParam(abstractAction, "to");
                            term = this.getParam(abstractAction, "term");
                            Optional<Double> cIndex = this.getParam(abstractAction, "commit");
                            Optional<Double> pLogTerm  = this.getParam(abstractAction, "log_term");
                            Optional<Double> pLogIndex = this.getParam(abstractAction, "index");
                            Optional<List<Map<String, Object>>> entries = this.getEntries(abstractAction);
                            if(entries.isEmpty()) {
                                return null;
                            }
                            return mapHandleAppendEntriesRequest(
                                sender.get().intValue(), 
                                receiver.get().intValue(), 
                                pLogIndex.get().intValue(), 
                                pLogTerm.get().intValue(), 
                                term.get().intValue(), 
                                entries.get(), 
                                cIndex.get().intValue()
                            );
                        case "MsgAppResp":
                            // Handle append entries response
                            sender = this.getParam(abstractAction, "from");
                            receiver = this.getParam(abstractAction, "to");
                            term = this.getParam(abstractAction, "term");
                            grant = this.getParam(abstractAction, "reject");
                            Optional<Double> mIndex = this.getParam(abstractAction, "index");
                            return mapHandleAppendEntriesResponse(
                                sender.get().intValue(), 
                                receiver.get().intValue(),
                                term.get().intValue(),
                                !grant.get().booleanValue(),
                                mIndex.get().intValue()
                            );
                    }
                    break;
                case "BecomeLeader":
                    Optional<Double> nodeID = this.getParam(abstractAction, "node");
                    if(nodeID.isEmpty()) {
                        return null;
                    }
                    return mapBecomeLeader(nodeID.get().intValue());
                case "Timeout":
                    nodeID = this.getParam(abstractAction, "node");
                    if(nodeID.isEmpty()) {
                        return null;
                    }
                    return mapTimeout(nodeID.get().intValue());
                case "AdvanceCommitIndex":
                    nodeID = this.getParam(abstractAction, "node");
                    if(nodeID.isEmpty()) {
                        return null;
                    }
                    return mapAdvanceCommitIndex(nodeID.get().intValue());
            }
        } catch (Exception e) {

        }
        return null;

        // To Map
        // [ ] 1. Restart(p)
        // [x] 2. Timeout(p)
        // [x] 3. BecomeLeader(p)
        // [x] 4. ClientRequest(i,v)
        // [x] 5. AdvanceCommitIndex(p)
        // [x] 6. HandleRequestVoteRequest(i,j,lTerm,lIndex,term)
        // [x] 7. HandleRequestVoteResponse(i, j, term, grant)
        // [x] 8. HandleNilAppendEntriesRequest(i, j, pLogIndex, pLogTerm, term, cIndex)
        // [x] 9. HandleAppendEntriesRequest(i, j, pLogIndex, pLogTerm, term, entryTerm, entryValue, cIndex)
        // [x] 10. HandleAppendEntriesResponse(i, j, term, success, mIndex)  
    }
}
