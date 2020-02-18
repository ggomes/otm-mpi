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

    public List<Graph> meta_nodes;
    public Map<PairKey, MetaLink> meta_links;

    public MetaGraph(ScenarioWrapper scenario, MetisManager metis) throws Exception {
        meta_nodes = extract_graphs(scenario,metis);
        meta_links = build_pairs(scenario,metis);
    }

    public MetaGraph(String jsonfile) throws Exception {

        JSONParser parser = new JSONParser();

        JSONObject json = (JSONObject) parser.parse(new FileReader(jsonfile));

//        this.num = (long) json.get("num_graphs");

        meta_links = new HashMap<>();
        for(Object obj : (JSONArray) json.get("pairs")){
            JSONObject jobj = (JSONObject) obj;
            JSONArray jpairkey = (JSONArray) jobj.get("pairkey");
            JSONObject jmetagraphpair = (JSONObject) jobj.get("metagraphpair");
            meta_links.put(  new PairKey(jpairkey) ,
                        new MetaLink(jmetagraphpair) );
        }
    }

    ////////////////////////////////////////
    // public
    ////////////////////////////////////////

    public MyMetaGraph carve_for_rank(int myrank) {
        Graph mygraph = meta_nodes.get(myrank);
        MyMetaGraph my_metagraph = new MyMetaGraph(myrank,mygraph);
        for(MetaLink pair : meta_links.values()){
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

    private Map<PairKey, MetaLink> build_pairs(ScenarioWrapper scenario, MetisManager metis) throws Exception {

        Map<PairKey, MetaLink> pairs = new HashMap<>();
        if( meta_nodes.stream().anyMatch(x->x.isempty()))
            throw new Exception("Metis produced empty subgraphs.");

        // assign (boundary) links to graphs
        for(jaxb.Link link : scenario.get_links()){

            // the graphs that this link belongs to
            Set<Graph> mygraphs = meta_nodes.stream()
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
                pairs.put(key,new MetaLink(glow,ghigh,link));

        }
        return pairs;
    }

    private List<Graph> extract_graphs(ScenarioWrapper base_scenario, MetisManager metis){

        // MetisManager output
        Map<Integer,Long> index2nodeid = metis.read_nodemap();
        List<Integer> nodeindex2rank = metis.read_metis_output();

        List<Graph> graphs = new ArrayList<>();
        Long max_node_id = base_scenario.get_nodes().stream().mapToLong(node->node.getId()).max().getAsLong();

        for(int graph_index = 0; graph_index< metis.num_partitions; graph_index++){

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
