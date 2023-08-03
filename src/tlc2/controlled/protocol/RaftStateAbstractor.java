package tlc2.controlled.protocol;

import java.util.*;
import tlc2.tool.TLCState;
import tlc2.value.IValue;
import tlc2.value.impl.FcnRcdValue;
import tlc2.value.impl.IntValue;
import tlc2.value.impl.StringValue;
import util.UniqueString;

public class RaftStateAbstractor extends DefaultStateAbstractor implements StateAbstractor{
    Map<String, String> params;

    public RaftStateAbstractor(Map<String, String> params) {
        super();
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
            }
        }

        for (Map.Entry<UniqueString,IValue> val: twoValues.entrySet()) {
            result.add(val.getKey());
        }

        return result;
    }

    boolean isDifferent(TLCState cur, TLCState prev) {
        // If the difference is only in term numbers of non leaders then false
        FcnRcdValue currentTerms = (FcnRcdValue) cur.getVals().get(UniqueString.of("currentTerm"));
        FcnRcdValue prevTerms = (FcnRcdValue) prev.getVals().get(UniqueString.of("currentTerm"));

        FcnRcdValue curStates = (FcnRcdValue) cur.getVals().get(UniqueString.of("state"));
        FcnRcdValue prevStates = (FcnRcdValue) prev.getVals().get(UniqueString.of("state"));

        int curLeader = -1;
        for (int i = 0; i < curStates.values.length; i++) {
            StringValue s = (StringValue) curStates.values[i];
            if(s.val.equals("leader")) {
                curLeader = i;
                break;
            }
        }

        int prevLeader = -1;
        for (int i = 0; i < prevStates.values.length; i++) {
            StringValue s = (StringValue) prevStates.values[i];
            if(s.val.equals("leader")) {
                prevLeader = i;
                break;
            }
        }

        if (curLeader == -1 && prevLeader == -1) {
            return false;
        }

        if ( curLeader != -1 && prevLeader != -1) {
            IntValue curLeaderTerm = (IntValue) currentTerms.values[curLeader];
            IntValue prevLeaderTerm = (IntValue) prevTerms.values[prevLeader];

            if( curLeaderTerm.val == prevLeaderTerm.val) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<TLCState> doAbstraction(List<TLCState> states) {
        List<TLCState> superResult = super.doAbstraction(states);
        List<TLCState> result = new ArrayList<>();
        if (superResult.size() == 0) {
            return states;
        }
        result.add(superResult.get(0));
        int i = 0, j = 1;
        for(; j < superResult.size(); j++) {
            TLCState cur = superResult.get(j);
            TLCState prev = superResult.get(i);

            // If cur is not different from previous then don't add current
            if (isDifferent(cur, prev)) {
                result.add(cur);
                i = j;
            }
        }
        if (i == superResult.size() -1) {
            result.add(superResult.get(i));
        }
        return result;
    }
}
