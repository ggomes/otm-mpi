package metagraph;

//import com.google.gson.JsonArray;
import org.json.simple.JSONArray;

import java.util.Objects;

public class PairKey {

    // subscenario index = rank index = metagraph low or high
    public final long low;   // the lower of the two ranks (subscenarios) in the pair
    public final long high;  // the higher of the two ranks (subscenarios) in the pair

    public PairKey(long low, long high) {
        this.low = low;
        this.high = high;
    }

    public PairKey(JSONArray jarr) {
        this.low = (long) jarr.get(0);
        this.high = (long) jarr.get(1);
    }

//    public JsonArray toJson(){
//        JsonArray arr = new JsonArray();
//        arr.add(low);
//        arr.add(high);
//        return arr;
//    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairKey pairKey = (PairKey) o;
        return low == pairKey.low &&
                high == pairKey.high;
    }

    @Override
    public int hashCode() {
        return Objects.hash(low, high);
    }
}
