package xmlsplitter;

import jaxb.Link;
import metagraph.Graph;
import metagraph.MetaGraph;
import metagraph.MyMetaGraph;
import metagraph.Neighbor;
import metis.MetisManager;
import xml.JaxbLoader;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class XMLSplitter {

    public static String prefix;
    public static String config_file;
    public static int num_partitions;

    /**
     * 0 : String prefix
     * 1 : String config_file
     * 2 : int num_partitions
     */
    public static void main(String[] args) throws Exception {

        prefix = args[0];
        config_file = args[1];
        num_partitions = Integer.parseInt(args[2]);

        if(num_partitions==1){
            String config_name = (new File(config_file)).getName().replaceFirst("[.][^.]+$", "");
            Runtime.getRuntime().exec(String.format("cp %s %s_1_cfg_0.xml",
                    config_file,
                    Paths.get(Paths.get(prefix).getParent().toString(),config_name)));
            System.exit(0);
        }

        // read the scenario
        ScenarioWrapper base_scenario = new ScenarioWrapper(JaxbLoader.load_scenario(config_file, true));

        // create Metis manager
        MetisManager metis_manager = new MetisManager(prefix, num_partitions);

        // run metis
        metis_manager.run(base_scenario);

        // read the metagraph
        MetaGraph metagraph = new MetaGraph(base_scenario,metis_manager);

        // extract per rank
        for(int rank=0;rank<num_partitions;rank++){

            // extract and print my_metagraph
            MyMetaGraph my_metagraph = metagraph.carve_for_rank(rank);
            my_metagraph.write_to_json(String.format("%s_mg_%d.json",prefix,rank));

            // extract and print subscenario
            jaxb.Scenario sub_scenario = extract_subnetwork(my_metagraph, base_scenario);
            String out_file_name = String.format("%s_cfg_%d.xml",prefix,rank);
            write_to_xml(sub_scenario,out_file_name);
        }

    }

    private static jaxb.Scenario extract_subnetwork(MyMetaGraph my_metagraph, ScenarioWrapper base_scenario) {

        Graph mygraph = my_metagraph.mygraph;
        jaxb.Network base_network = base_scenario.scenario.getNetwork();

        // keep all nodes in the sub graph
        Set<Long> keep_nodes = new HashSet<>();
        keep_nodes.addAll(mygraph.nodes);
        Set<Long> keep_links = new HashSet<>();
        keep_links.addAll(mygraph.links);

        // additional nodes and links to keep
        for (Neighbor neighbor : my_metagraph.neighbors) {
            for (Long link_id : neighbor.rel_sources) {
                Link link = base_scenario.get_link_with_id(link_id);

                // start node for rel sources
                keep_nodes.add(link.getStartNodeId());

                // For relative sources all previous links, ie links that enter its start node.
                // This is to ensure that upstream road connections used in the decoder have
                // an upstream node to map to in Translator.rc2nodemodel.
                Set<Long> in_links = base_scenario.get_inlink_ids_for_node(link.getStartNodeId());
                keep_links.addAll(in_links);
                keep_nodes.addAll(in_links.stream()
                        .map(linkid->base_scenario.get_start_node_for_link(linkid))
                        .collect(Collectors.toSet()));

            }
            for (Long link_id : neighbor.rel_sinks) {
                jaxb.Link link = base_scenario.get_link_with_id(link_id);

                // end node for rel sinks
                keep_nodes.add(link.getEndNodeId());

                // For relative sinks all next links, ie links that leave its end node.
                // This is to ensure a) that lanegroups in relative sinks are created correcly,
                // and b) that split ratios on the end node make sense.
                Set<Long> out_links = base_scenario.get_outlink_ids_for_node(link.getEndNodeId());
                keep_links.addAll(out_links);
                keep_nodes.addAll(out_links.stream()
                        .map(linkid->base_scenario.get_end_node_for_link(linkid))
                        .collect(Collectors.toSet()));
            }
        }

        // scenario ............................................
        jaxb.Scenario subscenario = new jaxb.Scenario();

        // models ..............................................
        subscenario.setModels(base_scenario.scenario.getModels());

        // network ..............................................
        jaxb.Network subnetwork = new jaxb.Network();
        subscenario.setNetwork(subnetwork);

        // road geoms
        subnetwork.setRoadgeoms(base_network.getRoadgeoms());

        // nodes
        jaxb.Nodes nodes = new jaxb.Nodes();
        subnetwork.setNodes(nodes);
        for (Long node_id : keep_nodes)
            nodes.getNode().add(base_scenario.get_node_with_id(node_id));

        // links
        jaxb.Links links = new jaxb.Links();
        subnetwork.setLinks(links);
        for (Long link_id : keep_links)
            links.getLink().add(base_scenario.get_link_with_id(link_id));

        // road connections
        jaxb.Roadconnections road_connections = new jaxb.Roadconnections();
        subnetwork.setRoadconnections(road_connections);
        for (jaxb.Roadconnection rc : base_scenario.get_road_connections()) {
            if (keep_links.contains(rc.getInLink()) || keep_links.contains(rc.getOutLink())) {
                road_connections.getRoadconnection().add(rc);
            }
        }

        // road_params
        subnetwork.setRoadparams(base_network.getRoadparams());

        // commodities
        subscenario.setCommodities(base_scenario.scenario.getCommodities());

        // demands
        if (base_scenario.scenario.getDemands() != null) {
            jaxb.Demands demands = new jaxb.Demands();
            subscenario.setDemands(demands);
            for (jaxb.Demand demand : base_scenario.scenario.getDemands().getDemand())
                if (keep_links.contains(demand.getLinkId()))
                    demands.getDemand().add(demand);
        }

        // splits
        if (base_scenario.scenario.getSplits() != null) {
            jaxb.Splits splits = new jaxb.Splits();
            subscenario.setSplits(splits);
            for (jaxb.SplitNode splitnode : base_scenario.scenario.getSplits().getSplitNode())
                if (keep_nodes.contains(splitnode.getNodeId()))
                    splits.getSplitNode().add(splitnode);
        }

        return subscenario;
    }

    private static void write_to_xml(jaxb.Scenario sub_scenario,String filename)  {
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(sub_scenario.getClass().getPackage().getName());
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            File file = new File( filename );
            marshaller.marshal( sub_scenario, file );
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

}
