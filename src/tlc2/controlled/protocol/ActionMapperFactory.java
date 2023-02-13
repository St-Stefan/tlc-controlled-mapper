package tlc2.controlled.protocol;

import java.util.List;

import tlc2.tool.Action;

public class ActionMapperFactory {
    public static ActionMapper getMapper(List<Action> enabledActions, String model) {
        switch (model) {
            case "MC":
                return new TPCActionMapper(enabledActions);
            default:
                return new AbstractToTLAActionMapper(enabledActions);
        }
    }
}
