package xmlsplitter;

import jaxb.Models;
import metagraph.Graph;
import metagraph.MetaGraph;
import metagraph.MyMetaGraph;
import metagraph.Neighbor;
import metis.MetisManager;
import org.xml.sax.SAXException;
import otm.ScenarioWrapper;
import utils.OTMUtils;
import xml.JaxbLoader;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.*;

import static java.util.stream.Collectors.toSet;

public class XMLSplitter {


    public static String prefix;
    public static String config_file;
    public static int num_partitions;
    private static boolean verbose;

    /**
     * 0 : String prefix
     * 1 : String config_file
     * 2 : int num_partitions
     * 3 : boolean verbose
     */
    public static void main(String[] args) throws Exception {

        prefix = args[0];
        config_file = args[1];
        num_partitions = Integer.parseInt(args[2]);
        verbose = args.length<=3 ? false : Boolean.parseBoolean(args[3]);

        if(num_partitions==1){
            String config_name = (new File(config_file)).getName().replaceFirst("[.][^.]+$", "");
            Runtime.getRuntime().exec(String.format("cp %s %s_1_cfg_0.xml",
                    config_file,
                    Paths.get(Paths.get(prefix).getParent().toString(),config_name)));
            System.exit(0);
        }

        // read the scenario
        print("Reading the scenario");
        ScenarioWrapper base_scenario = new ScenarioWrapper(JaxbLoader.load_scenario(config_file, true));

        // create Metis manager
        MetisManager metis_manager = new MetisManager(prefix, num_partitions);

        // run metis
        print("Running Metis");
        metis_manager.run(base_scenario);

        // read the metagraph
        print("Creating the metagraph");
        MetaGraph metagraph = new MetaGraph(base_scenario,metis_manager);

        // extract per rank
        for(int rank=0;rank<num_partitions;rank++){

            // extract and print my_metagraph
            print("Exporting JSON",rank);
            MyMetaGraph my_metagraph = metagraph.carve_for_rank(rank);
            my_metagraph.write_to_json(String.format("%s_mg_%d.json",prefix,rank));

            // extract and print subscenario
            print("Exporting XML",rank);
            jaxb.Scenario sub_scenario = extract_subnetwork(my_metagraph, base_scenario);
            String out_file_name = String.format("%s_cfg_%d.xml",prefix,rank);
            write_to_xml(sub_scenario,out_file_name);
        }

    }

    private static jaxb.Scenario extract_subnetwork(MyMetaGraph my_metagraph, ScenarioWrapper base_scenario) {

        Graph mygraph = my_metagraph.mygraph;
        jaxb.Network base_network = base_scenario.scenario.getNetwork();

        // dead nodes: no split ratio
        // dead links: no model
//        Map<Long,Boolean> node_is_live = new HashMap<>();
//        Map<Long,Boolean> link_is_live = new HashMap<>();

        // boundary links and node ....................
        Set<Long> links_live = new HashSet<>();
        Set<Long> nodes_live = new HashSet<>();
        Set<Long> links_dead = new HashSet<>();
        Set<Long> nodes_dead = new HashSet<>();

        nodes_live.addAll(mygraph.nodes);
        links_live.addAll(mygraph.links);

        for(Neighbor neighbor : my_metagraph.neighbors){

            // relative sources and their predecessors
            links_live.addAll(neighbor.rel_sources);
            for(Long linkid : neighbor.rel_sources ){
                jaxb.Link rel_source = base_scenario.links.get(linkid).link;
                Long start_node_id = rel_source.getStartNodeId();
                nodes_dead.add(start_node_id);  // start node is dead

                // predecessor nodes and links
                for(Long in_link_id : base_scenario.nodes.get(start_node_id).inlinks){
                    links_dead.add(in_link_id);
                    nodes_dead.add(base_scenario.links.get(in_link_id).link.getStartNodeId());
                }
            }

            // relative sinks and their successors
            links_live.addAll(neighbor.rel_sinks);
            for(Long linkid : neighbor.rel_sinks ){
                jaxb.Link rel_sink = base_scenario.links.get(linkid).link;
                Long end_node_id = rel_sink.getEndNodeId();
                nodes_live.add(end_node_id);    // end node is live

                // successor nodes and links
                for(Long out_link_id : base_scenario.nodes.get(end_node_id).outlinks){
                    links_dead.add(out_link_id);
                    nodes_dead.add(base_scenario.links.get(out_link_id).link.getEndNodeId());
                }
            }
        }

        // remove from dead nodes/links those that are alive
        links_dead.removeAll(links_live);
        nodes_dead.removeAll(nodes_live);

        // TEMPORARY: ALL LINKS ARE LIVE ================================================
        links_live.addAll(links_dead);
        nodes_live.addAll(nodes_dead);
        links_dead = new HashSet<>();
        nodes_dead = new HashSet<>();
        // ==============================================================================

        Set<Long> links_all = new HashSet<>();
        links_all.addAll(links_dead);
        links_all.addAll(links_live);

        Set<Long> nodes_all = new HashSet<>();
        nodes_all.addAll(nodes_dead);
        nodes_all.addAll(nodes_live);

        // scenario ............................................
        jaxb.Scenario subscenario = new jaxb.Scenario();

        // models ..............................................
        Models models = base_scenario.scenario.getModels();
        if(models!=null || (models.getModel().size()==1 && models.getModel().get(0).getType()=="none")) {
            jaxb.Models newmodels = clone(base_scenario.scenario.getModels());
            subscenario.setModels(newmodels);

            // add all dead links to none model
            if(!links_dead.isEmpty()){
                // remove the model for keep links
                for (jaxb.Model model : newmodels.getModel()) {
                    List<Long> links = OTMUtils.csv2longlist(model.getLinks());
                    if(model.isIsDefault())
                        links.addAll(mygraph.links);
                    model.setLinks(OTMUtils.comma_format(links));
                    model.setIsDefault(false);
                }
                jaxb.Model nonemodel = new jaxb.Model();
                newmodels.getModel().add(nonemodel);
                nonemodel.setName("none");
                nonemodel.setType("none");
                nonemodel.setLinks(OTMUtils.comma_format(links_dead));
            }
        }

        // network ..............................................
        jaxb.Network subnetwork = new jaxb.Network();
        subscenario.setNetwork(subnetwork);

        // road geoms
        subnetwork.setRoadgeoms(base_network.getRoadgeoms());

        // nodes
        jaxb.Nodes nodes = new jaxb.Nodes();
        subnetwork.setNodes(nodes);
        for (Long node_id : nodes_all) {
            jaxb.Node node = base_scenario.get_node_with_id(node_id);
//            node.setVsource(vsources.contains(node_id) ? true : null);
//            node.setVsink(vsinks.contains(node_id) ? true : null);
            nodes.getNode().add(node);
        }

        // links
        jaxb.Links links = new jaxb.Links();
        subnetwork.setLinks(links);
        for (Long link_id : links_all)
            links.getLink().add(base_scenario.get_link_with_id(link_id));

        // road connections
        jaxb.Roadconnections road_connections = new jaxb.Roadconnections();
        subnetwork.setRoadconnections(road_connections);
        for (jaxb.Roadconnection rc : base_scenario.get_road_connections()) {
            if (links_all.contains(rc.getInLink()) && links_all.contains(rc.getOutLink())) {
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
            demands.getDemand().addAll(
                    base_scenario.scenario.getDemands().getDemand().stream()
                    .filter(d->links_live.contains(d.getLinkId()))
                    .collect(toSet()) );
        }

        // splits
        if (base_scenario.scenario.getSplits() != null) {
            jaxb.Splits splits = new jaxb.Splits();
            subscenario.setSplits(splits);
            splits.getSplitNode().addAll(
                    base_scenario.scenario.getSplits().getSplitNode().stream()
                    .filter(s->nodes_live.contains(s.getNodeId()))
                    .collect(toSet())
            );
        }

        // subnetworks
        jaxb.Subnetworks subsubnets = new jaxb.Subnetworks();
        subscenario.setSubnetworks(subsubnets);
        jaxb.Subnetworks basesubnetworks = base_scenario.scenario.getSubnetworks();
        if (basesubnetworks != null) {
            Integer subnetid = 1;
            for (jaxb.Subnetwork basesubnet : basesubnetworks.getSubnetwork()){
                String basesubnetcontent = basesubnet.getContent();
                List<String> subnetlist = Arrays.asList(basesubnetcontent.split(","));
                Set<Long> linkset = subnetlist.stream().map(x -> Long.parseLong(x)).collect(toSet());

                // Only keep links that exist in the metis cut graph
                linkset.retainAll(links_all);

                String filtered_subnet_links = String.join(",", linkset.stream().map(x -> x.toString()).collect(toSet()));
                // System.out.println(filtered_subnet_links);

                jaxb.Subnetwork subsubnet = new jaxb.Subnetwork();
                subsubnet.setId(subnetid);
                subsubnet.setContent(filtered_subnet_links);
                subsubnets.getSubnetwork().add(subsubnet);
                subnetid += 1;
            }
        }

        return subscenario;
    }

    private static void write_to_xml(jaxb.Scenario sub_scenario,String filename)  {
        try {
            javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(sub_scenario.getClass().getPackage().getName());
            javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();

            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            InputStream resourceAsStream = JaxbLoader.class.getResourceAsStream("/otm.xsd");
            Schema schema = sf.newSchema(new StreamSource(resourceAsStream));
            marshaller.setSchema(schema);

            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
            marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            File file = new File( filename );
            marshaller.marshal( sub_scenario, file );
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }

    private static jaxb.Models clone(jaxb.Models x){
        if(x==null)
            return null;
        jaxb.Models y = new jaxb.Models();
        for(jaxb.Model m : x.getModel()){
            jaxb.Model newmodel = new jaxb.Model();
            y.getModel().add(newmodel);
            newmodel.setName(m.getName());
            newmodel.setType(m.getType());
            newmodel.setIsDefault(m.isIsDefault());
            newmodel.setProcess(m.getProcess());
            newmodel.setLinks(m.getLinks());
            if(m.getModelParams()!=null){
                jaxb.ModelParams newparams = new jaxb.ModelParams();
                newmodel.setModelParams(newparams);
                newparams.setSimDt(m.getModelParams().getSimDt());
                newparams.setContent(m.getModelParams().getContent());
                newparams.setMaxCellLength(m.getModelParams().getMaxCellLength());
            }
        }
        return y;
    }

    private static void print(String str){
        if(verbose)
            System.out.println("[XML Splitter] " + str);
    }

    private static void print(String str,int my_rank){
        if(verbose)
            System.out.println("[XML Splitter] (Rank " + my_rank + ") " + str);
    }

}
