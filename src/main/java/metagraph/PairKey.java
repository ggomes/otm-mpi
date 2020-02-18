package metagraph;

import org.json.simple.JSONArray;

import java.util.Objects;

public class PairKey {

    // subscenario index = rank index = metagraph low or high
    public final int low;   // the lower of the two ranks (subscenarios) in the pair
    public final int high;  // the higher of the two ranks (subscenarios) in the pair

    public PairKey(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public PairKey(JSONArray jarr) {
        this.low = (int) jarr.get(0);
        this.high = (int) jarr.get(1);
    }

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
