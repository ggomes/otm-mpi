package otm;

import java.util.*;

public class ScenarioWrapper {

    public jaxb.Scenario scenario;
    public Map<Long,MyNode> nodes = new HashMap<>();
    public Map<Long,MyLink> links = new HashMap<>();

    public ScenarioWrapper(jaxb.Scenario scenario){
        this.scenario = scenario;

        for(jaxb.Node node : scenario.getNetwork().getNodes().getNode())
            nodes.put(node.getId(),new MyNode(node));

        for(jaxb.Link link : scenario.getNetwork().getLinks().getLink()){
            links.put(link.getId(),new MyLink(link));
            nodes.get(link.getStartNodeId()).add_out_link(link.getId());
            nodes.get(link.getEndNodeId()).add_in_link(link.getId());
        }

        for(jaxb.Roadconnection rc : scenario.getNetwork().getRoadconnections().getRoadconnection())
            links.get(rc.getOutLink()).add_incoming_rc(rc);

    }

    public List<jaxb.Node> get_nodes(){
        return scenario.getNetwork().getNodes().getNode();
    }

    public List<jaxb.Link> get_links(){
        return scenario.getNetwork().getLinks().getLink();
    }

    public jaxb.Node get_node_with_id(long node_id){
        return nodes.get(node_id).node;
    }

    public jaxb.Link get_link_with_id(long link_id){
        return links.get(link_id).link;
    }

    public Set<Long> get_outlink_ids_for_node(long node_id){
        return nodes.get(node_id).outlinks;
    }

    public Set<Long> get_inlink_ids_for_node(long node_id){
        return nodes.get(node_id).inlinks;
    }

    public Long get_start_node_for_link(Long linkid){
        return links.containsKey(linkid) ? links.get(linkid).link.getStartNodeId() : null;
    }

    public Long get_end_node_for_link(Long linkid){
        return links.containsKey(linkid) ? links.get(linkid).link.getEndNodeId() : null;
    }

    public Set<jaxb.Roadconnection> get_incoming_rcs_for_link(long linkid){
        return links.containsKey(linkid) ? links.get(linkid).incoming_rcs : null;
    }

    public List<jaxb.Roadconnection> get_road_connections(){
        return scenario.getNetwork().getRoadconnections().getRoadconnection();
    }

    public class MyNode {
        public Set<Long> inlinks = new HashSet<>();
        public Set<Long> outlinks = new HashSet<>();
        public jaxb.Node node;
        public MyNode(jaxb.Node node){
            this.node = node;
        }
        public void add_out_link(long link_id){
            outlinks.add(link_id);
        }
        public void add_in_link(long link_id){
            inlinks.add(link_id);
        }
    }

    public class MyLink {
        Set<jaxb.Roadconnection> incoming_rcs = new HashSet<>();
        public jaxb.Link link;
        public MyLink(jaxb.Link link){this.link=link;}
        public void add_incoming_rc(jaxb.Roadconnection rc){incoming_rcs.add(rc);}
    }

}
