package metagraph;

import metagraph.Graph;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MetaGraphPair {

    public Graph glow;
    public Graph ghigh;
    public long low;
    public long high;

    // common links between the two subnetworks
    public List<Long> low_to_high = new ArrayList<>();
    public List<Long> high_to_low = new ArrayList<>();

    public MetaGraphPair(Graph glow, Graph ghigh, jaxb.Link first_link) throws Exception {
        this.glow = glow;
        this.ghigh = ghigh;
        this.low = glow.index;
        this.high = ghigh.index;
        add_link(glow,ghigh,first_link);
    }

    public MetaGraphPair(JSONObject jobj){
        glow = new Graph((JSONObject) jobj.get("glow"));
        ghigh = new Graph((JSONObject) jobj.get("ghigh"));
        low = Long.parseLong((String)jobj.get("low"));
        high = Long.parseLong((String)jobj.get("high"));
        for(Object obj : (JSONArray) jobj.get("low_to_high"))
            low_to_high.add((long) obj);
        for(Object obj : (JSONArray) jobj.get("high_to_low"))
            high_to_low.add((long) obj);
    }

    public void add_link(Graph glow, Graph ghigh,jaxb.Link link) throws Exception{

        // figure out whether this link goes from low to high, or high to low.
        Long start_node = link.getStartNodeId();
        Long end_node = link.getEndNodeId();

        boolean low_start = glow.nodes.contains(start_node);
        boolean low_end = glow.nodes.contains(end_node);
        boolean high_start = ghigh.nodes.contains(start_node);
        boolean high_end = ghigh.nodes.contains(end_node);

        // start node in only one graph
        if( !(low_start ^ high_start) )
            throw new Exception("start node in none or both graphs");

        if( !(low_end ^ high_end) )
            throw new Exception("end node in none or both graphs");

        // is this link low to high or high to low?
        boolean low2high = low_start && high_end;
        boolean high2low = high_start && low_end;

        if((!low2high && !high2low) || (low2high && high2low))
            throw new Exception(String.format("Error: link %d, low2high=%s, high2low=%s",link.getId(),low2high,high2low));

        if(low2high)
            low_to_high.add(link.getId());
        else
            high_to_low .add(link.getId());

    }

    public JSONObject toJson(){
        JSONObject obj = new JSONObject();
        obj.put("glow",glow.toJson());
        obj.put("ghigh",ghigh.toJson());
        obj.put("low",String.format("%d",low));
        obj.put("high",String.format("%d",high));

        JSONArray lh = new JSONArray();
        obj.put("low_to_high",lh);
        for(Long link_id : low_to_high)
            lh.add(link_id);

        JSONArray hl = new JSONArray();
        obj.put("high_to_low",hl);
        for(Long link_id : high_to_low)
            hl.add(link_id);

        return obj;
    }
}
