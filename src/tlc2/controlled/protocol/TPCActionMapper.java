package tlc2.controlled.protocol;

import com.google.gson.JsonSyntaxException;
import tlc2.tool.Action;

import java.util.List;

// TwoPhaseCommit (TwoPhaseCommit with parametric number of transaction requests) action mapper
public class TPCActionMapper extends BaseActionMapper {

    public TPCActionMapper(List<Action> enabledActions) {
        super(enabledActions);
    }

    public Action mapAction(AbstractAction abstractAction) {
        try{
            String message = abstractAction.message;

            for(Action a: this.enabledActions) {
                if(a.getName().equals(message) && a.getName().equals("NextRequest")) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("TMSendPrepareReq") // sends all at once // send first to a0, a1, etc does not matter!
                        && abstractAction.hasParams(a.getParams())) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("RMRcvPrepareReq")
                        && abstractAction.hasParams(a.getParams())) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("RMSendPrepared")
                        && abstractAction.hasParams(a.getParams())) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("TMRcvPrepared")
                        && abstractAction.hasParams(a.getParams())) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("RMSendAborted")
                        && abstractAction.hasParams(a.getParams())) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("TMRcvAborted") // sends GlobalAbort upon receipt of Aborted
                        && abstractAction.hasParams(a.getParams())) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("RMRcvGlobalAbort")
                        && abstractAction.hasParams(a.getParams())) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("TMSendGlobalCommit")
                        && abstractAction.hasParams(a.getParams())) { // sends all at once // send first to a0, a1, etc does not matter
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("RMRcvGlobalCommit")
                        && abstractAction.hasParams(a.getParams())) {
                    return a;
                }
            }

            return null;


        } catch (JsonSyntaxException e) {
            System.out.println("[TPCActionMapper] Invalid action");
            System.out.println("Error: "+e.getMessage());
        }

        return null;
    }

}
