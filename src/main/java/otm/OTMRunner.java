package otm;

import dispatch.Dispatcher;
import dispatch.EventMacroFlowUpdate;
import dispatch.EventMacroStateUpdate;
import dispatch.EventStopSimulation;
import error.OTMException;
import mpi.MPIException;
import runner.RunParameters;
import runner.Scenario;
import runner.Timer;
import translator.Translator;

public class OTMRunner {

    public static double run(Scenario scenario, RunParameters runParams, Translator translator, mpi.GraphComm comm) throws OTMException, MPIException {
        Timer comm_timer = new Timer(true);
        Dispatcher dispatcher = new Dispatcher(runParams.start_time);
        scenario.initialize(dispatcher,runParams);
        run_mpi(scenario,runParams.duration,dispatcher,translator,comm,comm_timer);
        scenario.is_initialized = false;
        return comm_timer.get_total_time();
    }

    public static void run(Scenario scenario, RunParameters runParams) throws OTMException {
        Dispatcher dispatcher = new Dispatcher(runParams.start_time);
        scenario.initialize(dispatcher,runParams);
        run_non_mpi(scenario,runParams.duration,dispatcher);
        scenario.is_initialized = false;
    }

    private static void run_mpi(Scenario scenario, float duration, Dispatcher dispatcher, Translator translator, mpi.GraphComm comm, Timer comm_timer) throws OTMException {

        dispatcher.set_continue_simulation(true);

        float now = dispatcher.current_time;

        // register stop the simulation
        dispatcher.set_stop_time(now+duration);
        dispatcher.register_event(new EventStopSimulation(scenario,dispatcher,now+duration));

        // register first models.ctm clock tick
        if(!scenario.network.macro_link_models.isEmpty()) {
            dispatcher.register_event(new EventMacroFlowUpdateMPI(dispatcher, now + scenario.sim_dt, scenario.network,translator,comm,comm_timer));
            dispatcher.register_event(new EventMacroStateUpdate(dispatcher, now + scenario.sim_dt, scenario.network));
        }

        // process all events
        dispatcher.dispatch_events_to_stop();

    }


    private static void run_non_mpi(Scenario scenario, float duration, Dispatcher dispatcher) throws OTMException {

        dispatcher.set_continue_simulation(true);

        float now = dispatcher.current_time;

        // register stop the simulation
        dispatcher.set_stop_time(now+duration);
        dispatcher.register_event(new EventStopSimulation(scenario,dispatcher,now+duration));

        // register first models.ctm clock tick
        if(!scenario.network.macro_link_models.isEmpty()) {
            dispatcher.register_event(new EventMacroFlowUpdate(dispatcher, now + scenario.sim_dt, scenario.network));
            dispatcher.register_event(new EventMacroStateUpdate(dispatcher, now + scenario.sim_dt, scenario.network));
        }

        // process all events
        dispatcher.dispatch_events_to_stop();

    }


}
