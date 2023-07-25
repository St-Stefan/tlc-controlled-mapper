package tlc2.controlled.protocol;

import java.util.List;
import java.util.Map;

import tlc2.tool.Action;
import tlc2.value.impl.IntValue;
import tlc2.value.impl.StringValue;
import tlc2.value.impl.Value;

public class RaftCrashesActionMapper extends BaseActionMapper{

    public RaftCrashesActionMapper(List<Action> enabledActions) {
        super(enabledActions);
    }

    protected Action mapAdd(int i) {
        if (!this.enabledActionMap.containsKey("Add")) {
            return null;
        }
        for (Action a : this.enabledActionMap.get("Add")) {
            Map<String, Value> params = a.getParams();
            if (params.containsKey("i")) {
                IntValue i_val = (IntValue) params.get("i");
                if (i_val.val == i) {
                    return a;
                }
            }
        }
        return null;
    }

    protected Action mapUpdateState(int i, String state) {
        if (!this.enabledActionMap.containsKey("UpdateState")) {
            return null;
        }
        for (Action a : this.enabledActionMap.get("UpdateState")) {
            Map<String, Value> params = a.getParams();
            if (params.containsKey("i") && params.containsKey("s")) {
                IntValue i_val = (IntValue) params.get("i");
                StringValue s_val = (StringValue) params.get("s");
                if (i_val.val == i && s_val.val.equals(state)) {
                    return a;
                }
            }
        }
        return null;
    }

    protected Action mapRemove(int i) {
        if (!this.enabledActionMap.containsKey("Remove")) {
            return null;
        }
        for (Action a : this.enabledActionMap.get("Remove")) {
            Map<String, Value> params = a.getParams();
            if (params.containsKey("i")) {
                IntValue i_val = (IntValue) params.get("i");
                if (i_val.val == i) {
                    return a;
                }
            }
        }
        return null;
    }

    public Action mapAction(AbstractAction abstractAction) {
        try {
            String name = abstractAction.name;
            Integer i = Integer.parseInt((String) abstractAction.params.get("i"));
            switch(name) {
                case "Add":
                    return this.mapAdd(i);
                case "UpdateState":
                    String state = (String) abstractAction.params.get("state");
                    return this.mapUpdateState(i, state);
                case "Remove":
                    return this.mapRemove(i);
            }
        } catch (Exception e) {

        }
        return null;
    }
}
