package otm;

import dispatch.Dispatcher;
import dispatch.EventMacroStateUpdate;
import dispatch.EventStopSimulation;
import error.OTMException;
import runner.RunParameters;
import runner.Scenario;
import translator.Translator;

public class OTMRunner {

    public static Double run(Scenario scenario, RunParameters runParams, Translator translator, mpi.GraphComm comm) throws OTMException {
        Double comm_time = 0d;
        Dispatcher dispatcher = new Dispatcher(runParams.start_time);
        scenario.initialize(dispatcher,runParams);
        advance(scenario,runParams.duration,dispatcher,translator,comm,comm_time);
        scenario.is_initialized = false;
        return comm_time;
    }

    private static void advance(Scenario scenario, float duration, Dispatcher dispatcher, Translator translator, mpi.GraphComm comm, Double comm_time) throws OTMException {

        dispatcher.set_continue_simulation(true);

        float now = dispatcher.current_time;

        // register stop the simulation
        dispatcher.set_stop_time(now+duration);
        dispatcher.register_event(new EventStopSimulation(scenario,dispatcher,now+duration));

        // register first models.ctm clock tick
        if(!scenario.network.macro_link_models.isEmpty()) {
            dispatcher.register_event(new EventMacroFlowUpdateMPI(dispatcher, now + scenario.sim_dt, scenario.network,translator,comm,comm_time));
            dispatcher.register_event(new EventMacroStateUpdate(dispatcher, now + scenario.sim_dt, scenario.network));
        }

        // process all events
        dispatcher.dispatch_events_to_stop();

    }

}
