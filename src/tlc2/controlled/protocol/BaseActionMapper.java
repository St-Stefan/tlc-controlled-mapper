package tlc2.controlled.protocol;

import java.util.*;

import com.google.gson.*;
import com.google.gson.annotations.*;

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
                Action mappedAction = mapAction(a);
                if (mappedAction != null) {
                    outList.add(ActionWrapper.action(mappedAction));
                }
            }
        }
        System.out.println("Mapped to "+outList.size()+" actions!");
        return outList;
    }

    protected AbstractAction fromJson(String jsonString) {
        try{
            Gson gson = new Gson();
            AbstractAction action = gson.fromJson(jsonString, AbstractAction.class);
            return action;
        } catch (JsonSyntaxException e) {
            System.out.println("[AbstractAction] Invalid action. Error: "+e.getMessage());
        }
        return null;
    }

    protected List<AbstractAction> listFromJson(String listJsonString) {
        List<AbstractAction> actions = new ArrayList<AbstractAction>();
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

        @SerializedName(value = "name", alternate = {"Name"})
        public final String name;

        @SerializedName(value = "params", alternate = {"Params"})
        public final HashMap<String, Object> params;


        @SerializedName(value = "reset", alternate = {"Reset"})
        public final boolean reset;

        public AbstractAction(String name, HashMap<String, Object> params, boolean reset) {
            this.name = name;
            this.params = params;
            this.reset = reset;
        }

        public boolean isReset() {
            return this.reset;
        }
    }
}
