package translator;

import common.Link;
import common.RoadConnection;
import keys.KeyCommPathOrLink;
import metagraph.MyMetaGraph;
import metagraph.Neighbor;
import models.AbstractLaneGroup;
import models.fluid.Cell;
import models.fluid.FluidModel;
import models.fluid.LaneGroup;
import models.fluid.NodeModel;
import runner.Scenario;
import java.util.*;
import java.util.stream.Collectors;

public class Translator {

    public long myRank;

    // ... encoder
    public Map<Long, NodeModel> rc2nodemodel = new HashMap<>();
    public Map<Long, NodeModel> lg2nodemodel = new HashMap<>();

    // ... decoder
    public Map<Long, Link> rc2Link = new HashMap<>();
    public Map<Long, LaneGroup> lgid2lg = new HashMap<>();

    public Cypher encoder;
    public Cypher decoder;

    public Translator(MyMetaGraph my_metagraph, Scenario scenario){

        myRank = my_metagraph.myrank;

        // scenario maps ................................
        NodeModel start_node_model, end_node_model;
        for(Neighbor neighbor : my_metagraph.neighbors){

            // 1) RELATIVE SOURCES
            for(Long link_id : neighbor.rel_sources) {
                Link rel_source = scenario.network.links.get(link_id);
                start_node_model = ((FluidModel)rel_source.model).get_node_model_for_node(rel_source.start_node.getId());
                end_node_model   = ((FluidModel)rel_source.model).get_node_model_for_node(rel_source.end_node.getId());

                for (RoadConnection rc : get_ordered_road_connections_entering(rel_source)) {
                    rc2Link.put(rc.getId(),rel_source);
                    if(start_node_model!=null)
                        rc2nodemodel.put(rc.getId(), start_node_model);
                }
                for(AbstractLaneGroup lg : rel_source.lanegroups_flwdn.values()) {
                    lgid2lg.put(lg.id,(models.fluid.LaneGroup) lg);
                    if(end_node_model!=null)
                        lg2nodemodel.put(lg.id, end_node_model);
                }
            }


            // 2) RELATIVE SINKS
            for(Long link_id : neighbor.rel_sinks) {
                Link rel_sink = scenario.network.links.get(link_id);
                start_node_model = ((FluidModel)rel_sink.model).get_node_model_for_node(rel_sink.start_node.getId());
                end_node_model   = ((FluidModel)rel_sink.model).get_node_model_for_node(rel_sink.end_node.getId());

                for (RoadConnection rc : get_ordered_road_connections_entering(rel_sink)) {
                    rc2Link.put(rc.getId(),rel_sink);
                    if(start_node_model!=null)
                        rc2nodemodel.put(rc.getId(), start_node_model);
                }
                for(AbstractLaneGroup lg : rel_sink.lanegroups_flwdn.values()) {
                    lgid2lg.put(lg.id,(models.fluid.LaneGroup) lg);
                    if(end_node_model!=null)
                        lg2nodemodel.put(lg.id, end_node_model);
                }
            }
        }

        // encoder / decoder .............................
        encoder = new Cypher(my_metagraph);
        decoder = new Cypher(my_metagraph);

        // configure the encoder and decoder
        for(Neighbor neighbor : my_metagraph.neighbors){

            List<Link> rel_sources = neighbor.rel_sources.stream()
                    .map(x->scenario.network.links.get(x))
                    .collect(Collectors.toList());

            List<Link> rel_sinks = neighbor.rel_sinks.stream()
                    .map(x->scenario.network.links.get(x))
                    .collect(Collectors.toList());

            // receive message (decoder) .....................................................
            // + for all relative sources l, all road connections entering l (rc), and all states that use rc.
            // + for all relative sinks l, all lanegroups in l (lg), and all states that use lg.
            for(Link link : rel_sources)
                for (RoadConnection rc : get_ordered_road_connections_entering(link))
                    for (KeyCommPathOrLink key : get_ordered_states_for_road_connection(rc))
                        decoder.add_item(neighbor.rank, new MessageItemRC(rc.getId(), key));

            for(Link link : rel_sinks)
                for (AbstractLaneGroup lg : get_ordered_lanegroups_for_link(link))
                    for (KeyCommPathOrLink key : get_ordered_states_for_lanegroup(lg))
                        decoder.add_item(neighbor.rank, new MessageItemLG(lg, key));

            // send message (encoder) .............................................................
            // + for all relative sink l, all roadconnections entering l (rc), and all states that use rc.
            // + for all relative sources l, all lanegroups in l (lg), and all states that use lg.
            for(Link link : rel_sinks)
                for (RoadConnection rc : get_ordered_road_connections_entering(link))
                    for (KeyCommPathOrLink key : get_ordered_states_for_road_connection(rc))
                        encoder.add_item(neighbor.rank, new MessageItemRC(rc.getId(), key));

            for(Link link : rel_sources)
                for (AbstractLaneGroup lg : get_ordered_lanegroups_for_link(link))
                    for (KeyCommPathOrLink key : get_ordered_states_for_lanegroup(lg))
                        encoder.add_item(neighbor.rank, new MessageItemLG(lg, key));

        }

        encoder.compute_displacements();
        decoder.compute_displacements();
    }

    public double [] create_rcv_buffer(){
        return new double[decoder.items.size()];
    }

    /** read the state of the scenario corresponding to each of the items in the encoder.
     * Arrange those into the send buffer.
     * Timestamp is used for debugging only.
     */
    public double [] encode(float timestamp){

        double [] sendBuf = new double[encoder.items.size()];

        int index = 0;
        for( AbstractMessageItem item : encoder.items ){

            double value = 0d;

            if(item instanceof MessageItemRC){
                MessageItemRC xitem = (MessageItemRC) item;
                Long rc_id = xitem.rc_id;
                NodeModel node_model = rc2nodemodel.get(rc_id);
                models.fluid.RoadConnection rc = node_model.rcs.get(rc_id);
                value = rc.f_rs.get(xitem.key);
            }

            if(item instanceof MessageItemLG){
                MessageItemLG xitem = (MessageItemLG) item;
                if(xitem.lg.link.is_sink){
                    Cell lastCell = xitem.lg.cells.get(xitem.lg.cells.size()-1);
                    value = lastCell.demand_dwn.get(item.key);
                } else {
                    Long lg_id = xitem.lg.id;
                    NodeModel node_model = lg2nodemodel.get(lg_id);
                    models.fluid.UpLaneGroup ulg = node_model.ulgs.get(lg_id);
                    value = ulg.f_gs.get(xitem.key);
                }
            }

            sendBuf[index++] = value;
        }

        return sendBuf;
    }

    /** Read each item in the receive buffer and write it to each of the corresponding states in the scenario.
     * Timestamp is used for debugging only.
     */
    public void decode(double [] rcvBuf, float timestamp){

        for(int i=0;i<rcvBuf.length;i++){

            double value = rcvBuf[i];
            AbstractMessageItem item = decoder.items.get(i);

            if(item instanceof MessageItemRC){
                MessageItemRC xitem = (MessageItemRC) item;
                Long rc_id = xitem.rc_id;
                NodeModel node_model = rc2nodemodel.get(rc_id);
                models.fluid.RoadConnection rc = node_model.rcs.get(rc_id);
                rc.f_rs.put(xitem.key,value);
            }

            if(item instanceof MessageItemLG){
                MessageItemLG xitem = (MessageItemLG) item;
                if(xitem.lg.link.is_sink){
                    Cell lastCell = xitem.lg.cells.get(xitem.lg.cells.size()-1);
                    value = lastCell.demand_dwn.get(item.key);
                    lastCell.demand_dwn.put(item.key,value);
                } else {
                    Long lg_id = xitem.lg.id;
                    NodeModel node_model = lg2nodemodel.get(lg_id);
                    models.fluid.UpLaneGroup ulg = node_model.ulgs.get(lg_id);
                    ulg.f_gs.put(xitem.key,value);
                }
            }

        }
    }

    ////////////////////////////////////////////
    // private helpers
    ////////////////////////////////////////////

    private List<RoadConnection> get_ordered_road_connections_entering(Link link){
        List<RoadConnection> x = new ArrayList<>(link.get_roadconnections_entering());
        Collections.sort(x);
        return x;
    }

    private List<KeyCommPathOrLink> get_ordered_states_for_road_connection(RoadConnection rc){
        List<KeyCommPathOrLink> x = new ArrayList<>(rc.get_states());
        Collections.sort(x);
        return x;
    }

    private List<AbstractLaneGroup> get_ordered_lanegroups_for_link(Link link){
        List<AbstractLaneGroup> x = new ArrayList<>(link.lanegroups_flwdn.values());
        Collections.sort(x);
        return x;
    }

    private List<KeyCommPathOrLink> get_ordered_states_for_lanegroup(AbstractLaneGroup lg){
        List<KeyCommPathOrLink> x = new ArrayList<>(((models.fluid.LaneGroup) lg).states);
        Collections.sort(x);
        return x;
    }

}
