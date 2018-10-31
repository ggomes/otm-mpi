package runner;

import api.APIopen;
import error.OTMException;
import mpi.MPI;
import mpi.MPIException;
import otm.OTMRunner;
import metagraph.MyMetaGraph;
import translator.Translator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RunnerMPI {

    private static final boolean run_mpi = false;

    public static String prefix;
    public static int repetition;
    public static float sim_dt;
    public static float duration;
    public static boolean writeoutput;

    public static double metagraph_load_time;
    public static double load_subscenario_time;
    public static double create_translator_time;
    public static double mpi_run_time;
    public static double comm_time;

    /**
     * 0 string : [prefix]
     * 1 int : [repetition]
     * 1 float : [sim_dt] sim dt in seconds
     * 2 float : [duration] sim duration in seconds
     * 3 boolean : [writeouput] true->write network state to files
     */
    public static void main(String[] args) throws Exception {

        prefix = args[0];
        repetition = Integer.parseInt(args[1]);
        sim_dt = Float.parseFloat(args[2]);
        duration = Float.parseFloat(args[3]);
        writeoutput = Boolean.valueOf(args[4]);

        Timer timer;
        float out_dt = sim_dt;

        // initialize mpi
        if(run_mpi)
            MPI.Init(args);

        int my_rank = run_mpi ? MPI.COMM_WORLD.getRank() : 0;
        int num_processes = run_mpi ? MPI.COMM_WORLD.getSize() : 1;

        File prefix_file = new File(prefix);
        String output_prefix = String.format("%s_%d_%d",prefix_file.getName(),my_rank,repetition);
        String output_folder = prefix_file.getParentFile().getAbsolutePath();

        // trivial case ...........................
        if(num_processes==1){

            timer = new Timer(run_mpi);
            APIopen api = new APIopen(OTM.load(String.format("%s_cfg_%d.xml",prefix,my_rank),sim_dt,true,"ctm"));
            OTM.initialize(api.scenario(), new RunParameters(null, null, null, 0f, duration));
            if(writeoutput)
                api.api.request_links_veh(output_prefix,output_folder, null, api.api.get_link_ids(), out_dt);
            load_subscenario_time = timer.get_total_time();

            timer = new Timer(run_mpi);
            RunParameters runParam = new RunParameters(null, null, null, 0f,duration);
            OTMRunner.run(api.scenario(), runParam);
            mpi_run_time = timer.get_total_time();

            write_output(output_folder,output_prefix,null);

            if(run_mpi)
                MPI.Finalize();

            System.exit(0);
        }

        // read my metagraph ......................
        timer = new Timer(run_mpi);
        MyMetaGraph my_metagraph = new MyMetaGraph(String.format("%s_mg_%d.json",prefix,my_rank));
        metagraph_load_time = timer.get_total_time();

        // extract the subscenario for this rank
        timer = new Timer(run_mpi);
        APIopen api = new APIopen(OTM.load(String.format("%s_cfg_%d.xml",prefix,my_rank),sim_dt,true,"ctm"));
        OTM.initialize(api.scenario(), new RunParameters(null, null, null, 0f, duration));
        if(writeoutput)
            api.api.request_links_veh(output_prefix,output_folder, null, api.api.get_link_ids(), out_dt);
        load_subscenario_time = timer.get_total_time();

        // create communicator and translator ...........................
        timer = new Timer(run_mpi);
        int [] neighbors = my_metagraph.get_neighbors();
        mpi.GraphComm comm = run_mpi ?
                MPI.COMM_WORLD.createDistGraphAdjacent(neighbors, neighbors, MPI.INFO_NULL, false) :
                null;
        Translator translator = new Translator(my_metagraph,api.scenario());
        create_translator_time = timer.get_total_time();

        // run ...................................
        timer = new Timer(run_mpi);
        RunParameters runParam = new RunParameters(null, null, null, 0f,duration);
        comm_time = OTMRunner.run(api.scenario(), runParam,translator,comm);
        mpi_run_time = timer.get_total_time();

        // write timers ...........................
        write_output(output_folder,output_prefix,translator);

        // finalize mpi
        if(run_mpi)
            MPI.Finalize();
    }

    private static void write_output(String output_folder,String output_prefix, Translator translator) {
        File file = Paths.get(output_folder,String.format("%s_timers.txt",output_prefix)).toFile();
        FileWriter fr = null;
        try{
            fr = new FileWriter(file);
            fr.write(String.format("metagraph_load\t%f\n",metagraph_load_time));
            fr.write(String.format("scenario_load\t%f\n",load_subscenario_time));
            fr.write(String.format("translator_load\t%f\n",create_translator_time));
            fr.write(String.format("run\t%f\n",mpi_run_time));
            fr.write(String.format("comm\t%f\n",comm_time));
            fr.write(String.format("double_send\t%d\n",translator==null ? 0 : translator.encoder.items.size()));
            fr.write(String.format("double_receive\t%d\n",translator==null ? 0 : translator.decoder.items.size()));
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            try {
                fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
