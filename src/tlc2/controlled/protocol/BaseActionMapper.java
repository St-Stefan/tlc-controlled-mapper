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

    public Action mapSingleAction(String actionString) {
        return mapAction(fromJson(actionString));
    }

    public List<Action> mapListOfActions(String actionString) {
        List<Action> outList = new ArrayList<>();
        List<AbstractAction> abstractActions = listFromJson(actionString);
        for(AbstractAction a:abstractActions) {
            outList.add(mapAction(a));
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

            return new AbstractAction(senderId, receiverId, message);

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

        public AbstractAction(int senderId, int receiverId, String message) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.message = message;
        }
    }
}
