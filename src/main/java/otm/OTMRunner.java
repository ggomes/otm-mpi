package otm;

import common.Scenario;
import dispatch.Dispatcher;
import dispatch.EventStopSimulation;
import error.OTMException;
import models.AbstractModel;
import models.fluid.AbstractFluidModel;
import models.fluid.EventFluidStateUpdate;
import mpi.MPIException;
import runner.Timer;
import translator.Translator;

import java.util.Set;
import java.util.stream.Collectors;

public class OTMRunner {

    public static double run(Scenario scenario, float start_time, float duration, Translator translator, mpi.GraphComm comm) throws OTMException, MPIException {
        Timer comm_timer = new Timer(true);
        Dispatcher dispatcher = new Dispatcher(start_time);
        scenario.initialize(dispatcher);
        run_mpi(scenario,duration,dispatcher,translator,comm,comm_timer);
        scenario.is_initialized = false;
        return comm_timer.get_total_time();
    }

    public static void run(Scenario scenario, float start_time, float duration) throws OTMException {
        Dispatcher dispatcher = new Dispatcher(start_time);
        scenario.initialize(dispatcher);
        run_non_mpi(scenario,duration,dispatcher);
        scenario.is_initialized = false;
    }

    private static void run_mpi(Scenario scenario, float duration, Dispatcher dispatcher, Translator translator, mpi.GraphComm comm, Timer comm_timer) throws OTMException {

        if(scenario.network.models.isEmpty())
            throw new OTMException("No models!");

        Set<AbstractModel> fluid_models = scenario.network.models.values().stream()
                .filter(m->m instanceof AbstractFluidModel)
                .collect(Collectors.toSet());

        if(fluid_models.size()!=1)
            throw new OTMException("This currently works only for a single fluid model. This one has " + fluid_models.size());

        AbstractFluidModel model = (AbstractFluidModel) fluid_models.iterator().next();

        dispatcher.set_continue_simulation(true);

        float now = dispatcher.current_time;

        // register stop the simulation
        dispatcher.set_stop_time(now+duration);
        dispatcher.register_event(new EventStopSimulation(scenario,dispatcher,now+duration));

        // register first models.ctm clock tick
        if(!scenario.network.models.isEmpty()) {
            dispatcher.register_event(new EventMacroFlowUpdateMPI(dispatcher,now + model.dt_sec,model,translator,comm,comm_timer));
            dispatcher.register_event(new EventFluidStateUpdate(dispatcher, now + model.dt_sec, model));
        }

        // process all events
        dispatcher.dispatch_events_to_stop();

    }

    private static void run_non_mpi(Scenario scenario, float duration, Dispatcher dispatcher) throws OTMException {

        if(scenario.network.models.isEmpty())
            throw new OTMException("No models!");

        if(scenario.network.models.size()!=1)
            throw new OTMException("This currently works only for a single model.");

        AbstractModel abs_model = scenario.network.models.values().iterator().next();

        if(!(abs_model instanceof AbstractFluidModel))
            throw new OTMException("Not a fluid model.");

        dispatcher.set_continue_simulation(true);

        float now = dispatcher.current_time;
        AbstractFluidModel model = (AbstractFluidModel) abs_model;

        // register stop the simulation
        dispatcher.set_stop_time(now+duration);
        dispatcher.register_event(new EventStopSimulation(scenario,dispatcher,now+duration));

        // register first models.ctm clock tick
        if(!scenario.network.models.isEmpty()) {
            dispatcher.register_event(new EventMacroFlowUpdateMPI(dispatcher, now + model.dt_sec, model,null,null,null));
            dispatcher.register_event(new EventFluidStateUpdate(dispatcher, now + model.dt_sec, model));
        }

        // process all events
        dispatcher.dispatch_events_to_stop();

    }


}
