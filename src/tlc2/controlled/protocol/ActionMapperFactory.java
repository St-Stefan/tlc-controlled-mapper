package tlc2.controlled.protocol;

import java.util.List;

import tlc2.tool.Action;

public class ActionMapperFactory {
    public static ActionMapper getMapper(List<Action> enabledActions, String model) {
        if (model.contains("TPC")) {
            System.out.println("Using TPC action mapper!");
            return new TPCActionMapper(enabledActions);
        } else if (model.contains("RAFT")) {
            return new RaftActionMapper(enabledActions);
        }
        return new DefaultActionMapper(enabledActions);
    }
}
