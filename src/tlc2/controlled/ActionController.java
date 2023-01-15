package tlc2.controlled;

import tlc2.tool.Action;
import tlc2.tool.StateVec;
import tlc2.tool.TLCState;

import java.util.Collection;

public interface ActionController {

    // returns the actionId of the next action
    int getNextAction(Action[] actions);

    // sets the current state of the execution
    void setCurrentState(TLCState state);
}
