package tlc2.controlled.protocol;

import tlc2.tool.Action;

import java.util.List;

public class AbstractToTLAActionMapper implements ActionMapper {

    private final List<Action> actions;

    // maps an event to an event in the provided set of possible actions
    public AbstractToTLAActionMapper(List<Action> actions) {
        this.actions = actions;
    }

    // Takes an abstract action in the json form and maps it to Action in TLAChecker
    public Action map(String actionString) {

        return AbstractAction.fromJson(actionString).mapToAction(actions);
    }
}
