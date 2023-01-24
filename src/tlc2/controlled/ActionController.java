package tlc2.controlled;

import tlc2.controlled.protocol.ActionMapper;
import tlc2.tool.Action;
import tlc2.tool.TLCState;

public abstract class ActionController {

    protected ActionMapper mapper;
    protected Action[] actions;

    public ActionController(ActionMapper mapper, Action[] actions) {
        this.mapper = mapper;
        this.actions = actions;
    }

    // returns the next action in jason format
    public abstract Action getNextAction();

    // sets the current state of the execution
    public abstract void setCurrentState(TLCState state);
}
