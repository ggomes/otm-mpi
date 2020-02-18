package metagraph;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.toIntExact;

public class MyMetaGraph {

    public long myrank;
    public Graph mygraph;
    public List<Neighbor> neighbors;

    public MyMetaGraph(int myrank, Graph mygraph){
        this.myrank = myrank;
        this.mygraph = mygraph;
        neighbors = new ArrayList<>();
    }

    public MyMetaGraph(String jsonfile) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(new FileReader(jsonfile));
        myrank = (long) json.get("myrank");
        mygraph = new Graph((JSONObject)json.get("mygraph"));
        neighbors = new ArrayList<>();
        for(Object obj : (JSONArray) json.get("neighbors"))
            neighbors.add(new Neighbor((JSONObject) obj));
    }

    public void add_neighbor(MetaLink pair){

        boolean i_am_low = myrank==pair.low;
        long neighbor_rank = i_am_low ? pair.high : pair.low;
        List<Long> rel_sources = i_am_low ? pair.high_to_low : pair.low_to_high;
        List<Long> rel_sinks   = i_am_low ? pair.low_to_high : pair.high_to_low;

        neighbors.add(new Neighbor(neighbor_rank,rel_sources,rel_sinks));
    }

    public int [] get_neighbors(){
        return neighbors.stream().map(n->n.rank)
                .mapToInt(x->toIntExact(x))
                .toArray();
    }

    public void write_to_json(String filename){

        JSONObject json = new JSONObject();
        json.put("myrank", myrank);
        json.put("mygraph",mygraph.toJson());

        JSONArray ns = new JSONArray();
        json.put("neighbors", ns);
        for(Neighbor neighbor : neighbors)
            ns.add(neighbor.toJson());

        //Write JSON file
        try (FileWriter file = new FileWriter(filename)) {
            file.write(json.toJSONString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        String str = "--- MY METAGRAPH PRINTOUT ---\n";
        for(Neighbor neighbor : neighbors){
            str+= String.format("rank %d, neighbor %d, rel_sinks: %s\n",myrank,neighbor.rank,neighbor.rel_sinks);
            str+= String.format("rank %d, neighbor %d, rel_sources: %s\n",myrank,neighbor.rank,neighbor.rel_sources);
        }
        str += "--------------------------";
        return str;
    }
}
