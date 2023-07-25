package tlc2.controlled.protocol;

import java.util.*;
import tlc2.tool.TLCState;

public class DefaultStateAbstractor implements StateAbstractor {
    public List<TLCState> doAbstraction(List<TLCState> states) {
        return states;
    }
}
