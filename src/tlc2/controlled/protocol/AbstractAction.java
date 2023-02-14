package tlc2.controlled.protocol;

// Currently only supports message send/delivery actions
// TODO: Extend with fault actions: drop a message, isolate/crash a process

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import tlc2.tool.Action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AbstractAction {
    public final int senderId;
    public final int receiverId;
    public final String message;

    public static final AbstractAction UNKNOWN = new AbstractAction(-1, -1, "");

    public AbstractAction(int senderId, int receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
    }

    // Model specific!
    // Returns the TLA action from the given list of actions
    public ActionWrapper mapToAction(final List<Action> actionList) {
        if(this.equals(UNKNOWN)) return ActionWrapper.action(Action.UNKNOWN);

        // For the Actions in TwoPhaseCommit
        // node ids: RMs: r1, r2, r3
        // mapped to: TM:0, RM1: 1, RM2: 2, RM3: 3
        for(Action a: actionList) {
            if(a.getName().equals(message) && a.getName().equals("RMPrepare")
                    && Integer.parseInt(a.con.getValue().toString()) == receiverId) {
                return ActionWrapper.action(a);
            } else if(a.getName().equals(message) && a.getName().equals("TMRcvPrepared")
                    && Integer.parseInt(a.con.getValue().toString()) == senderId) {
                return ActionWrapper.action(a);
            } else if(a.getName().equals(message) && a.getName().equals("RMChooseToAbort")
                    && Integer.parseInt(a.con.getValue().toString()) == senderId) {
                return ActionWrapper.action(a);
            } else if(a.getName().equals(message) && a.getName().equals("TMRcvAborted")
                    && Integer.parseInt(a.con.getValue().toString()) == senderId) {
                return ActionWrapper.action(a);
            } else if(a.getName().equals(message) && a.getName().equals("RMRcvAbortMsg")
                    && Integer.parseInt(a.con.getValue().toString()) == receiverId) {
                return ActionWrapper.action(a);
            } else if(a.getName().equals(message) && a.getName().equals("TMCommit")) { // sends all at once // send first to a0, a1, etc does not matter!
                return ActionWrapper.action(a);
            } else if(a.getName().equals(message) && a.getName().equals("RMRcvCommitMsg")
                    && Integer.parseInt(a.con.getValue().toString()) == receiverId) {
                return ActionWrapper.action(a);
            }
        }

        return null;
    }

    public String toString() {
        return toJSON();
    }

    // return the action that matches the given coyote action
    public static AbstractAction fromJson(String jsonStr) {

        try{
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(jsonStr, JsonObject.class);
            int senderId = root.get("senderId").getAsInt();
            int receiverId = root.get("receiverId").getAsInt();
            String message = root.get("message").getAsString();

            return new AbstractAction(senderId, receiverId, message);

        } catch (JsonSyntaxException e) {
            System.out.println("[CoyoteActionMapper] Invalid action");
        }

        return AbstractAction.UNKNOWN;
    }

    // return the action that matches the given coyote action
    public static List<AbstractAction> listFromJson(String jsonStr) {

        List<AbstractAction> actions = new ArrayList<>();
        try{
            Gson gson = new Gson();
            AbstractAction[] array = gson.fromJson(jsonStr, AbstractAction[].class);
            actions.addAll(Arrays.asList(array));

        } catch (JsonSyntaxException e) {
            System.out.println("[CoyoteActionMapper] Invalid action");
        }

        return actions;
    }

    // Will be implemented on the Coyote site:
    public String toJSON() {
        JsonObject root = new JsonObject();
        root.addProperty("senderId", senderId);
        root.addProperty("receiverId", receiverId);
        root.addProperty("message", message);
        return root.toString();
    }
}
