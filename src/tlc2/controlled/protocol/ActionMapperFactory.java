package tlc2.controlled.protocol;

import java.util.List;
import java.util.Map;

import tlc2.tool.Action;

public class ActionMapperFactory {
    public static ActionMapper getMapper(Map<String, String> params, List<Action> enabledActions, String model) {
        String name = "";
        if(params.containsKey("name")) {
            name = params.get("name");
        }
        ActionMapper mapper;
        if (model.contains("TPC") || name.equalsIgnoreCase("tpc")) {
            boolean isAbstract = false;
            if(params.containsKey("abstract")) {
                isAbstract = true;
            }
            mapper = new TPCActionMapper(enabledActions, isAbstract);
        } else if (model.contains("RAFT_CRASHES") || name.equalsIgnoreCase("raft_crashes")) {
            mapper = new RaftCrashesActionMapper(enabledActions);
        } else if (model.contains("RAFT") || name.equalsIgnoreCase("raft")) {
            boolean isAbstract = false;
            if(params.containsKey("abstract")) {
                isAbstract = true;
            }
            mapper = new RaftActionMapper(enabledActions, isAbstract);
        } else if (model.contains("MB") || name.equalsIgnoreCase("mb")) {
            mapper = new MBActionMapper(enabledActions);
        } else if (model.contains("RecoveryAndCommitSpec") || name.equalsIgnoreCase("RECOVERYCOMMIT")) {
            mapper = new RecoveryCommitActionMapper(enabledActions);
        // TODO: FullSpecActionMapper (AccordSpec/ACCORDSPEC) not yet implemented
        // TODO: AccordActionMapper (OnlyCommitSpec/COMMITSPEC) not yet implemented
        } else {
            mapper = new DefaultActionMapper(enabledActions);
        }

        System.out.println("[ActionMapperFactory] Selected mapper: " + mapper.getClass().getSimpleName() + " (model=" + model + ", name=" + name + ")");
        return mapper;
    }
}