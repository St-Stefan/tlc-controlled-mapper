package tlc2.controlled;

import tlc2.controlled.protocol.ActionMapper;
import tlc2.tool.Action;
import tlc2.tool.TLCState;

public abstract class ActionController {

    protected ActionMapper mapper;

    public ActionController(ActionMapper mapper) {
        this.mapper = mapper;
    }

    // returns the next action in jason format
    public abstract Action getNextAction(Action[] validActions);

    // sets the current state of the execution
    public abstract void setCurrentState(TLCState state);
}
