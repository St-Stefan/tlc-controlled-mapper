package tlc2.controlled.protocol;

import java.util.*;
import tlc2.tool.TLCState;
import tlc2.value.IValue;
import util.UniqueString;

public class RaftStateAbstractor implements StateAbstractor{
    Map<String, String> params;

    public RaftStateAbstractor(Map<String, String> params) {
        this.params = params;
    }

    List<UniqueString> diff(TLCState one, TLCState two) {
        List<UniqueString> result = new ArrayList<>();
        Map<UniqueString, IValue> oneValues = one.getVals();
        Map<UniqueString, IValue> twoValues = two.getVals();
        for(Map.Entry<UniqueString,IValue>  val: oneValues.entrySet()) {
            if (!twoValues.containsKey(val.getKey())) {
                result.add(val.getKey());
            } else {
                IValue valOne = val.getValue();
                IValue valTwo = twoValues.get(val.getKey());
                if (valOne.compareTo(valTwo) != 0) {
                    result.add(val.getKey());
                }
                twoValues.remove(val.getKey());
            }
        }

        for (Map.Entry<UniqueString,IValue> val: twoValues.entrySet()) {
            result.add(val.getKey());
        }

        return result;
    }

    boolean isDifferent(TLCState cur, TLCState prev) {
        // If the difference is only in term numbers of non leaders then false
        List<UniqueString> diffKeys = diff(cur, prev);

        return false;
    }

    public List<TLCState> doAbstraction(List<TLCState> states) {
        List<TLCState> result = new ArrayList<>();
        if (states.size() == 0) {
            return states;
        }
        result.add(states.get(0));
        for(int i = 1; i < states.size(); i++) {
            TLCState cur = states.get(i);
            TLCState prev = states.get(i-1);

            // If cur is not different from previous then don't add current
            if (isDifferent(cur, prev)) {
                result.add(cur);
            }
        }
        return result;
    }
}
