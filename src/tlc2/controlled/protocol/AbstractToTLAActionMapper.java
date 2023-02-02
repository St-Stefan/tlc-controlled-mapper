package tlc2.controlled.protocol;

import tlc2.tool.Action;

import java.util.ArrayList;
import java.util.List;

public class AbstractToTLAActionMapper implements ActionMapper {

    private final List<Action> enabledActions;

    // maps an event to an event in the provided set of possible actions
    public AbstractToTLAActionMapper(List<Action> enabledActions) {
        this.enabledActions = enabledActions;
    }

    // Takes an abstract action in the json form and maps it to Action of TLAChecker
    public Action mapSingleAction(String actionString) {

        AbstractAction abstractAction = AbstractAction.fromJson(actionString);
        return abstractAction.mapToAction(enabledActions);
    }

    // Takes a list of actions in the json form and maps it to List<Action>
    public List<Action> mapListOfActions(String actionsString) {

        List<AbstractAction> abstractActions = AbstractAction.listFromJson(actionsString);
        List<Action> actions = new ArrayList<>();

        for(AbstractAction a: abstractActions)
            actions.add(a.mapToAction(enabledActions));

        return actions;
    }
}
