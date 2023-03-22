package tlc2.controlled.protocol;

import java.lang.StackWalker.Option;
import java.util.*;
import tlc2.tool.Action;
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

    public Action mapAction(AbstractAction abstractAction) {
        try {
            switch (abstractAction.name) {
                case "SendMessage":
                    Optional<String> messageType = this.getParam(abstractAction, "type");
                    if (messageType.isEmpty()) {
                        return null;
                    }
                    switch(messageType.get()) {
                        case "MsgVote":
                        
                    }
                    break;
                case "DeliverMessage":
                    break;
                case "BecomeLeader":
                    Optional<Double> nodeID = this.getParam(abstractAction, "node");
                    if(nodeID.isEmpty()) {
                        return null;
                    }
                    int node = nodeID.get().intValue();
                    for (Action a : this.enabledActionMap.get("BecomeLeader")) {
                        Map<String, Value> params = a.getParams();
                        if(params.containsKey("i")) {
                            IntValue i = (IntValue) params.get("i");
                            if (i.val == node) {
                                return a;
                            }
                        }
                    }
                    break;
                case "Timeout":
                    nodeID = this.getParam(abstractAction, "node");
                    if(nodeID.isEmpty()) {
                        return null;
                    }
                    node = nodeID.get().intValue();
                    for (Action a : this.enabledActionMap.get("Timeout")) {
                        Map<String, Value> params = a.getParams();
                        if(params.containsKey("i")) {
                            IntValue i = (IntValue) params.get("i");
                            if (i.val == node) {
                                return a;
                            }
                        }
                    }
                    break;
                case "AdvanceCommitIndex":
                    break;
            }
        } catch (Exception e) {

        }
        return null;

        // To Map
        // 1. Restart(p)
        // 2. Timeout(p)
        // 3. Send RequestVote(p,q)
        // 4. BecomeLeader(p)
        // 5. ClientRequest(i,v)
        // 6. AdvanceCommitIndex(p)
        // 7. send AppendEntries(i,j)
        // 8. Receive(m)
        // 9. DropMessage(m)
    }
}
