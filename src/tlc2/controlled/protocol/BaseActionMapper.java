package tlc2.controlled.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import tlc2.tool.Action;

public abstract class BaseActionMapper implements ActionMapper {
    
    protected final List<Action> enabledActions;

    public BaseActionMapper(List<Action> enabledActions) {
        this.enabledActions = enabledActions;
    }

    protected abstract Action mapAction(AbstractAction a);

    public ActionWrapper mapSingleAction(String actionString) {
        AbstractAction abstractAction = fromJson(actionString);
        if (abstractAction.isReset()) {
            return ActionWrapper.reset();
        }
        return ActionWrapper.action(mapAction(abstractAction));
    }

    public List<ActionWrapper> mapListOfActions(String actionString) {
        List<ActionWrapper> outList = new ArrayList<>();
        List<AbstractAction> abstractActions = listFromJson(actionString);
        for(AbstractAction a:abstractActions) {
            if(a.isReset()) {
                outList.add(ActionWrapper.reset());
            } else {
            outList.add(ActionWrapper.action(mapAction(a)));
            }
        }
        return outList;
    }

    protected AbstractAction fromJson(String jsonString) {
        try{
            Gson gson = new Gson();
            JsonObject root = gson.fromJson(jsonString, JsonObject.class);
            int senderId = root.get("senderId").getAsInt();
            int receiverId = root.get("receiverId").getAsInt();
            String message = root.get("message").getAsString();

            return new AbstractAction(senderId, receiverId, message, false);

        } catch (JsonSyntaxException e) {
            System.out.println("[CoyoteActionMapper] Invalid action");
        }
        return null;
    }

    protected List<AbstractAction> listFromJson(String listJsonString) {
        List<AbstractAction> actions = new ArrayList<>();
        try{
            Gson gson = new Gson();
            AbstractAction[] array = gson.fromJson(listJsonString, AbstractAction[].class);
            actions.addAll(Arrays.asList(array));

        } catch (JsonSyntaxException e) {
            System.out.println("[CoyoteActionMapper] Invalid action");
        }

        return actions;
    }


    protected class AbstractAction {
        public final int senderId;
        public final int receiverId;
        public final String message;
        public final boolean reset;

        public AbstractAction(int senderId, int receiverId, String message, boolean reset) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.message = message;
            this.reset = reset;
        }

        public boolean isReset() {
            return this.reset;
        } 
    }
}
