package metagraph;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import metis.MetisManager;
import otm.ScenarioWrapper;

import java.io.FileReader;
import java.util.*;
import java.util.stream.Collectors;

public class MetaGraph {

    public long num_graphs;                      // number of nodes in the metagraph
    public Map<PairKey, MetaGraphPair> pairs;    // metagraph as a list of metagraph pairs

    public MetaGraph(ScenarioWrapper scenario, MetisManager metis) throws Exception {
        num_graphs = metis.num_partitions;
        pairs = build_pairs(scenario,metis);
    }

    public MetaGraph(String jsonfile) throws Exception {

        JSONParser parser = new JSONParser();

        JSONObject json = (JSONObject) parser.parse(new FileReader(jsonfile));

        this.num_graphs = (long) json.get("num_graphs");

        pairs = new HashMap<>();
        for(Object obj : (JSONArray) json.get("pairs")){
            JSONObject jobj = (JSONObject) obj;
            JSONArray jpairkey = (JSONArray) jobj.get("pairkey");
            JSONObject jmetagraphpair = (JSONObject) jobj.get("metagraphpair");
            pairs.put(  new PairKey(jpairkey) ,
                        new MetaGraphPair(jmetagraphpair) );
        }
    }

    ////////////////////////////////////////
    // public
    ////////////////////////////////////////

    public MyMetaGraph carve_for_rank(int myrank) {
        MyMetaGraph my_metagraph = new MyMetaGraph(myrank);
        for(MetaGraphPair pair : pairs.values()){
            if(myrank==pair.low || myrank==pair.high)
                my_metagraph.add_neighbor(pair);
        }

        return my_metagraph;
    }

    public void write_to_json(String filename){

        // TODO G REMOVED THIS

//        JSONObject json = new JSONObject();
//        json.put("num_graphs", num_graphs);
//
//        JSONArray jsonpairs = new JSONArray();
//        json.put("pairs", jsonpairs);
//        for(Map.Entry<PairKey, MetaGraphPair> e : pairs.entrySet()){
//            JSONObject pairobj = new JSONObject();
//            jsonpairs.add(pairobj);
//            pairobj.put("pairkey",e.getKey().toJson());
//            pairobj.put("metagraphpair",e.getValue().toJson());
//        }
//
//        //Write JSON file
//        try (FileWriter file = new FileWriter(filename)) {
//            file.write(json.toJSONString());
//            file.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    ////////////////////////////////////////
    // private helpers
    ////////////////////////////////////////

    private Map<PairKey, MetaGraphPair> build_pairs(ScenarioWrapper scenario, MetisManager metis) throws Exception {

        Map<PairKey, MetaGraphPair> pairs = new HashMap<>();

        // extract all graphs
        List<Graph> graphs = extract_graphs(scenario,metis);

        if( graphs.stream().anyMatch(x->x.isempty()))
            throw new Exception("Metis produced empty subgraphs.");

        // assign (boundary) links to graphs
        for(jaxb.Link link : scenario.get_links()){

            // the graphs that this link belongs to
            Set<Graph> mygraphs = graphs.stream()
                    .filter(graph->graph.links.contains(link.getId()))
                    .collect(Collectors.toSet());

            if(mygraphs.isEmpty())
                System.err.println(String.format("Link %d belongs to no graphs",link.getId()));

            if(mygraphs.size()==1)
                continue;

            if(mygraphs.size()>2)
                System.err.println(String.format("Link %d belongs to >2 graphs",link.getId()));

            // link belongs to exactly two graphs
            Iterator<Graph> it = mygraphs.iterator();
            Graph graphA = it.next();
            Graph graphB = it.next();

            // figure out low and high
            Graph glow,ghigh;
            if(graphA.index < graphB.index){
                glow = graphA;
                ghigh = graphB;
            } else {
                glow = graphB;
                ghigh = graphA;
            }

            // add to the correct pair
            PairKey key = new PairKey(glow.index,ghigh.index);
            if(pairs.containsKey(key))
                pairs.get(key).add_link(glow,ghigh,link);
            else
                pairs.put(key,new MetaGraphPair(glow,ghigh,link));

        }
        return pairs;
    }

    private List<Graph> extract_graphs(ScenarioWrapper base_scenario, MetisManager metis){

        // MetisManager output
        Map<Integer,Long> index2nodeid = metis.read_nodemap();
        List<Integer> nodeindex2rank = metis.read_metis_output();

        List<Graph> graphs = new ArrayList<>();
        Long max_node_id = base_scenario.get_nodes().stream().mapToLong(node->node.getId()).max().getAsLong();

        for(long graph_index=0;graph_index<num_graphs;graph_index++){

            Graph graph = new Graph(graph_index);
            graphs.add(graph);

            // gather network node ids
            for(Map.Entry<Integer,Long> e : index2nodeid.entrySet()) {

                if (nodeindex2rank.get(e.getKey()-1) != graph_index)
                    continue;

                Long node_id = e.getValue();
                jaxb.Node node = base_scenario.get_node_with_id(node_id);

                // add graph nodes
                graph.nodes.add(node_id);

                // add links incident on graph nodes
                Set<Long> link_ids = new HashSet<>();
                link_ids.addAll(base_scenario.get_outlink_ids_for_node(node.getId()));
                link_ids.addAll(base_scenario.get_inlink_ids_for_node(node.getId()));
                graph.links.addAll(link_ids);
            }

        }

        return graphs;
    }

}
