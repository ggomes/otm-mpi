package otm;

import dispatch.AbstractEvent;
import dispatch.Dispatcher;
import error.OTMException;
import models.fluid.AbstractFluidModel;
import mpi.MPI;
import runner.Timer;
import translator.Translator;

public class EventMacroFlowUpdateMPI extends AbstractEvent {

    private final Translator translator;
    private final mpi.GraphComm comm;
    private Timer comm_timer;

    public EventMacroFlowUpdateMPI(Dispatcher dispatcher, float timestamp,AbstractFluidModel model, Translator translator, mpi.GraphComm comm, Timer comm_timer){
        super(dispatcher,1,timestamp,model);
        this.translator = translator;
        this.comm = comm;
        this.comm_timer = comm_timer;
    }

    @Override
    public void action(boolean verbose) throws OTMException {

        super.action(verbose);

        AbstractFluidModel model = (AbstractFluidModel)recipient;

        try {
            update_fluid_flux(model,timestamp);
        } catch (Exception e) {
            throw new OTMException(e.getMessage());
        }

        // register next clock tick
        float next_timestamp = timestamp + model.dt_sec;
        if(next_timestamp<=dispatcher.stop_time)
            dispatcher.register_event(new EventMacroFlowUpdateMPI(dispatcher,next_timestamp,model,translator,comm,comm_timer));

    }

    public void update_fluid_flux(AbstractFluidModel model, float timestamp) throws Exception {
        model.update_flow_I(timestamp);
        mpi_communicate();
        model.update_flow_II(timestamp);
    }

    private void mpi_communicate() throws Exception {
        if(translator==null)
            return;

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
