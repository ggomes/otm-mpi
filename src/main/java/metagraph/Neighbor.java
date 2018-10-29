package metagraph;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Neighbor {
    public long rank;
    public List<Long> rel_sources;
    public List<Long> rel_sinks;

    public Neighbor(long rank, List<Long> rel_sources, List<Long> rel_sinks) {
        this.rank = rank;
        this.rel_sources = rel_sources;
        this.rel_sinks = rel_sinks;
    }

    public Neighbor(JSONObject obj){
        this.rank = (long) obj.get("rank");
        rel_sources = new ArrayList<>();
        for(Object o : (JSONArray) obj.get("rel_sources"))
            rel_sources.add((long)o);
        rel_sinks = new ArrayList<>();
        for(Object o : (JSONArray) obj.get("rel_sinks"))
            rel_sinks.add((long)o);
    }

    public JSONObject toJson(){
        JSONObject obj = new JSONObject();
        obj.put("rank",rank);
        JSONArray a0 = new JSONArray();
        obj.put("rel_sources",a0);
        rel_sources.forEach(x->a0.add(x));
        JSONArray a1 = new JSONArray();
        obj.put("rel_sinks",a1);
        rel_sinks.forEach(x->a1.add(x));
        return obj;
    }
}
