package tlc2.controlled.protocol;

import java.util.List;

import tlc2.tool.Action;

public class ActionMapperFactory {
    public static ActionMapper getMapper(List<Action> enabledActions, String model) {
        switch (model) {
            case "TPC":
                return new TPCActionMapper(enabledActions);
            default:
                return new DefaultActionMapper(enabledActions);
        }
    }
}
