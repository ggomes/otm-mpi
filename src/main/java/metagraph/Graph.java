package metagraph;

import error.OTMException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Set;

public class Graph {
    public Integer index;
    public Set<Long> nodes = new HashSet<>();
    public Set<Long> links = new HashSet<>();

    public Graph(Integer index) {
        this.index = index;
    }

    public Graph(JSONObject obj){
        this.index = (Integer) obj.get("index");
        for(Object o : (JSONArray) obj.get("nodes"))
            nodes.add((Long) o);
        for(Object o : (JSONArray) obj.get("links"))
            links.add((Long) o);
    }

    public boolean isempty(){
        return nodes.isEmpty();
    }

    public void validate() throws OTMException {
        // no repeated links

        Set<Long> link_unique = new HashSet<>(links);
        if(link_unique.size() < links.size())
            throw new OTMException("Duplicate links (" + (links.size()-link_unique.size()) +")");

        // no repeated nodes
        Set<Long> node_unique = new HashSet<>(nodes);
        if(node_unique.size() < nodes.size())
            throw new OTMException("Duplicate nodes (" + (nodes.size()-node_unique.size()) +")");
    }

    public JSONObject toJson(){
        JSONObject obj = new JSONObject();
        obj.put("index",index);

        JSONArray jnodes = new JSONArray();
        obj.put("nodes",jnodes);
        for(Long node : nodes)
            jnodes.add(node);

        JSONArray jlinks = new JSONArray();
        obj.put("links",jlinks);
        for(Long link : links)
            jlinks.add(link);

        return obj;
    }

}
