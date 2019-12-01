package runner;

import api.OTM;
import api.OTMdev;
import mpi.MPI;
import otm.OTMRunner;
import metagraph.MyMetaGraph;
import translator.Translator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class RunnerMPI {

    private static final boolean run_mpi = true;

    public static String prefix;
    public static int repetition;
    public static float duration;
    public static boolean writeoutput;
    public static float out_dt;
    private static boolean verbose;

    public static double metagraph_load_time;
    public static double load_subscenario_time;
    public static double create_translator_time;
    public static double mpi_run_time;
    public static double comm_time;

    /**
     * 0 string : [prefix]
     * 1 int : [repetition]
     * 2 float : [duration] sim duration in seconds
     * 3 boolean : [writeouput] true->write network state to files
     * 4 float : [out_dt] sim dt in seconds
     * 5 boolean : verbose
     */
    public static void main(String[] args) {

        try {
            prefix = args[0];
            repetition = Integer.parseInt(args[1]);
            duration = Float.parseFloat(args[2]);
            writeoutput = Boolean.valueOf(args[3]);
            out_dt = Float.parseFloat(args[4]);
            verbose = args.length<=5 ? false : Boolean.parseBoolean(args[5]);

            Timer timer;

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
                OTMdev otm = new OTMdev(new OTM(String.format("%s_cfg_%d.xml",prefix,my_rank),false,false));
                otm.otm.initialize(0f);
                if(writeoutput)
                    otm.otm.output.request_links_veh(output_prefix,output_folder, null, otm.otm.scenario.get_link_ids(), out_dt);
                load_subscenario_time = timer.get_total_time();

                timer = new Timer(run_mpi);
                OTMRunner.run(otm.scenario, 0f,duration);
                mpi_run_time = timer.get_total_time();

                write_output(output_folder,output_prefix,null);

                if(run_mpi)
                    MPI.Finalize();

                System.exit(0);
            }

            // read my metagraph ......................
            print("Reading metagraph from JSON",my_rank);
            timer = new Timer(run_mpi);
            MyMetaGraph my_metagraph = new MyMetaGraph(String.format("%s_mg_%d.json",prefix,my_rank));
            metagraph_load_time = timer.get_total_time();

            // extract the subscenario for this rank
            print("Extracting subscenario",my_rank);
            timer = new Timer(run_mpi);
            OTMdev otm = new OTMdev(new OTM(String.format("%s_cfg_%d.xml",prefix,my_rank),false,false));
            otm.otm.initialize(0f);
            if(writeoutput)
                otm.otm.output.request_links_veh(output_prefix,output_folder, null, otm.otm.scenario.get_link_ids(), out_dt);
            load_subscenario_time = timer.get_total_time();

            // create communicator and translator ...........................
            print("Creating communicator and translator",my_rank);
            timer = new Timer(run_mpi);
            int [] neighbors = my_metagraph.get_neighbors();
            mpi.GraphComm comm = run_mpi ?
                    MPI.COMM_WORLD.createDistGraphAdjacent(neighbors, neighbors, MPI.INFO_NULL, false) :
                    null;

            Translator translator = new Translator(my_metagraph,otm.scenario);
            create_translator_time = timer.get_total_time();

            // run ...................................
            print("Running",my_rank);
            timer = new Timer(run_mpi);
            comm_time = OTMRunner.run(otm.scenario, 0f,duration,translator,comm);
            mpi_run_time = timer.get_total_time();

            // write timers ...........................
            print("Writing output",my_rank);
            write_output(output_folder,output_prefix,translator);

            // finalize mpi
            if(run_mpi)
                MPI.Finalize();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
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

    private static void print(String str,int my_rank){
        if(verbose)
            System.out.println("[MPI runner] (Rank " + my_rank + ") " + str);
    }

}
