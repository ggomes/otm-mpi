package otm;

import common.Network;
import dispatch.AbstractEvent;
import dispatch.Dispatcher;
import error.OTMException;
import mpi.MPI;
import runner.Timer;
import translator.Translator;

import java.util.Arrays;

public class EventMacroFlowUpdateMPI extends AbstractEvent {

    private final Translator translator;
    private final mpi.GraphComm comm;
    private Timer comm_timer;

    public EventMacroFlowUpdateMPI(Dispatcher dispatcher, float timestamp, Network network, Translator translator, mpi.GraphComm comm, Timer comm_timer){
        super(dispatcher,1,timestamp,network);
        this.translator = translator;
        this.comm = comm;
        this.comm_timer = comm_timer;
    }

    @Override
    public void action(boolean verbose) throws OTMException {

        super.action(verbose);

        Network network = (Network)recipient;

        // update the ctm state
        try {
            update_macro_flow(network,timestamp);
        } catch (Exception e) {
            throw new OTMException(e.getMessage());
        }

        // register next clock tick
        float next_timestamp = timestamp+network.scenario.sim_dt;
        if(next_timestamp<=dispatcher.stop_time)
            dispatcher.register_event(new EventMacroFlowUpdateMPI(dispatcher,next_timestamp,network,translator,comm,comm_timer));
    }

    public void update_macro_flow(Network network, float timestamp) throws Exception {
        network.update_macro_flow_part_I(timestamp);
        mpi_communicate();
        network.update_macro_flow_part_II(timestamp);
    }

    private void mpi_communicate() throws Exception {

        comm_timer.start();

        double [] rcvBuf = translator.create_rcv_buffer();

        // create message
        double [] send_message = translator.encode(timestamp);

        // All-to-all
        if(comm!=null)
            comm.neighborAllToAllv( send_message,
                                translator.encoder.neighbor_length,
                                translator.encoder.neighbor_disp,
                                MPI.DOUBLE,
                                rcvBuf,
                                translator.decoder.neighbor_length,
                                translator.decoder.neighbor_disp,
                                MPI.DOUBLE );



        translator.decode(rcvBuf,timestamp);

        comm_timer.stop();
    }

}
