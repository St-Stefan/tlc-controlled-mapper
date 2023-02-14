package tlc2.controlled.protocol;

import java.util.List;

import com.google.gson.JsonSyntaxException;

import tlc2.tool.Action;

// TwoPhaseCommit action mapper
public class TPCActionMapper extends BaseActionMapper {

    public TPCActionMapper(List<Action> enabledActions) {
        super(enabledActions);
    }

    public Action mapAction(AbstractAction abstractAction) {
        try{
            int senderId = abstractAction.senderId;
            int receiverId = abstractAction.receiverId;
            String message = abstractAction.message;

            for(Action a: this.enabledActions) {
                if(a.getName().equals(message) && a.getName().equals("RMPrepare")
                        && Integer.parseInt(a.con.getValue().toString()) == receiverId) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("TMRcvPrepared")
                        && Integer.parseInt(a.con.getValue().toString()) == senderId) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("RMChooseToAbort")
                        && Integer.parseInt(a.con.getValue().toString()) == senderId) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("TMRcvAborted")
                        && Integer.parseInt(a.con.getValue().toString()) == senderId) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("RMRcvAbortMsg")
                        && Integer.parseInt(a.con.getValue().toString()) == receiverId) {
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("TMCommit")) { // sends all at once // send first to a0, a1, etc does not matter!
                    return a;
                } else if(a.getName().equals(message) && a.getName().equals("RMRcvCommitMsg")
                        && Integer.parseInt(a.con.getValue().toString()) == receiverId) {
                    return a;
                }
            }
    
            return null;


        } catch (JsonSyntaxException e) {
            System.out.println("[CoyoteActionMapper] Invalid action");
        }

        return null;
    }
}
