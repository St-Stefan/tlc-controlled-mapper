package tlc2.controlled.protocol;

import java.util.*;

import com.google.gson.*;

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
            JsonArray jsonArray = root.get("params").getAsJsonArray();

            List<String> params = new ArrayList<>();
            for(JsonElement e: jsonArray) {
                params.add(e.getAsString());
            }

            return new AbstractAction(senderId, receiverId, message, params, false);

        } catch (JsonSyntaxException e) {
            System.out.println("[AbstractAction] Invalid action. Error: "+e.getMessage());
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
            System.out.println("[AbstractAction] Invalid action. Error: "+e.getMessage());
        }

        return actions;
    }

    // TODO: Each model can have its own AbstractAction class (e.g., Raft has objects as message parameters instead of strings)
    protected class AbstractAction {
        public final int senderId;
        public final int receiverId;
        public final String message;
        public final List<String> params;
        public final boolean reset;

        public AbstractAction(int senderId, int receiverId, String message, List<String> params, boolean reset) {
            this.senderId = senderId;
            this.receiverId = receiverId;
            this.message = message;
            this.params = (params != null) ? new ArrayList<>(params) : new ArrayList<>();
            this.reset = reset;
        }

        public boolean isReset() {
            return this.reset;
        }

        // TODO: Param comparison depends on the model
        //  This method compares the parameters of type String (TwoPhaseCommit has messages of type string or int)
        public boolean hasParams(List<Object> params) {
            if(this.params.size() != params.size()) return false;

            for(int i = 0; i < this.params.size(); i++) {
                if(!this.params.get(i).equals(params.get(i).toString())) {
                    return false;
                }
            }
            return true;
        }
    }
}
