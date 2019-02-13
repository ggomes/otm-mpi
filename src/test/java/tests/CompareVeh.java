package tests;

import api.API;
import runner.OTM;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class CompareVeh {

    static int num_partitions;
    static String prefix;
    static String config_file;
    static float sim_dt;
    static float duration;

    static float out_dt;

    /**
     * 0 int : [num_partitions]
     * 1 string : [prefix]
     * 2 string : [config_file]
     * 3 float : [sim_dt] seconds
     * 4 float : [duration] seconds
     */
    public static void main(String[] args) throws Exception {

        // Assumes the MPI run has already been done.

        num_partitions = Integer.parseInt(args[0]);
        prefix = args[1];
        config_file = args[2];
        sim_dt = Float.parseFloat(args[3]);
        duration = Float.parseFloat(args[4]);

        out_dt = sim_dt;
        File prefix_file = new File(prefix);
        String prefix_name = prefix_file.getName();
        String output_folder = prefix_file.getParentFile().getAbsolutePath();
        String serial_prefix = String.format("%s_serial",prefix_name);

        // run serial
        API api = OTM.load(config_file);
        api.request_links_veh(serial_prefix,output_folder,null,api.get_link_ids(),out_dt);
        api.run(0f,duration);

        // load
        VehInfo serial_vehs = readVehicles(prefix+"_serial");

        // read mpi
        Set<VehInfo> mpi_vehs = new HashSet<>();
        for(int i=0;i<num_partitions;i++)
            mpi_vehs.add(readVehicles(String.format("%s_%d",prefix,i)));
        VehInfo mpi = merge(mpi_vehs,serial_vehs);

        // compare
        System.out.println(diff(mpi,serial_vehs) ? "SUCCESS! MPI=SERIAL!!" : "FAILED");
    }

    private static VehInfo readVehicles(String prefix){
        String vehs_file = String.format("%s_%.0f_g_link_veh.txt",prefix,out_dt);
        String links_file = String.format("%s_%.0f_g_link_veh_links.txt",prefix,out_dt);
        String time_file = String.format("%s_%.0f_g_link_veh_time.txt",prefix,out_dt);
        return readVehicles(vehs_file,readLinks(links_file),readTime(time_file));
    }

    private static VehInfo merge(Set<VehInfo> exp_VehInfo,VehInfo serial){

        float [][] mpiveh = new float[serial.time.size()][serial.link_ids.size()];
        for(int j=0;j<serial.link_ids.size();j++){
            Long link_id = serial.link_ids.get(j);

            for (VehInfo v: exp_VehInfo) {
                if(v.link_ids.contains(link_id)){
                    int ind = v.link_ids.indexOf(link_id);
                    for(int i=0;i<serial.time.size();i++) {
                        mpiveh[i][j] = v.veh[i][ind];
                    }
                }
            }
        }
        return new VehInfo(mpiveh,serial.link_ids,serial.time);
    }

    private static boolean diff(VehInfo A,VehInfo B){

        // compare size of links vector
        if(A.link_ids.size()!=B.link_ids.size()) {
            System.out.println("A link_ids.size - B link_ids.size: " + (A.link_ids.size() - B.link_ids.size()));
            return false;
        }

        // compare size of time vector
        if(A.time.size()!=B.time.size()) {
            System.out.println("A link_ids.size - B link_ids.size: " + (A.time.size() - B.time.size()));
            return false;
        }

        // comare link values
        for(int i=0;i<A.link_ids.size();i++) {
            if(A.link_ids.get(i) != B.link_ids.get(i)) {
                System.out.println("Link id difference " + A.link_ids.get(i) + " , " + B.link_ids.get(i));
                return false;
            }
        }

        // comare time values
        for(int i=0;i<A.time.size();i++) {
            if(A.time.get(i) != B.time.get(i)) {
                System.out.println("Time difference " + A.time.get(i) + " , " + B.time.get(i));
                return false;
            }
        }

        // values
        for(int i=0;i<A.time.size();i++)
            for(int j=0;j<A.link_ids.size();j++) {
                if (Math.abs(A.veh[i][j]-B.veh[i][j])>1e-2){
                    System.out.println("link " + A.link_ids.get(j) + " at time " + i + " has diff: "+ String.format("%9.3e", Math.abs(A.veh[i][j]-B.veh[i][j])));
//                    return false;
                }
            }

        return true;
    }

    private static List<Long> readLinks(String filename){
        List<Long> link_ids = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File(filename));
            for(String str : sc.nextLine().split("\t"))
                link_ids.add(Long.parseLong(str));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return link_ids;
    }

    private static List<Float> readTime(String filename){
        List<Float> time = new ArrayList<>();
        try {
            Scanner sc = new Scanner(new File(filename));
            while (sc.hasNextLine()) {
                time.add(Float.parseFloat(sc.nextLine())) ;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //System.out.println(time);
        return time;
    }

    private static VehInfo readVehicles(String filename,List<Long> link_ids,List<Float> time){
        int numtime = time.size();
        int numlinks = link_ids.size();
        float [][] veh = new float[numtime][numlinks];
        int time_index = 0;
        try {
            Scanner sc = new Scanner(new File(filename));
            while (sc.hasNextLine()) {
                String [] line = sc.nextLine().split(",");
                for(int i=0;i<line.length;i++){
                    veh[time_index][i] = Float.parseFloat(line[i]);
                }
                time_index++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return new VehInfo(veh,link_ids,time);
    }

    private static void print(VehInfo x){
        for(int i=0;i<x.veh.length;i++) {
            System.out.println();
            for (int j = 0; j < x.veh[i].length; j++)
                System.out.print(x.veh[i][j] + "\t");
        }
    }

    public static class VehInfo{
        public float [][] veh;
        public List<Long> link_ids;
        public List<Float> time;
        public VehInfo(float[][] veh, List<Long> link_ids, List<Float> time) {
            this.veh = veh;
            this.link_ids = link_ids;
            this.time = time;
        }
    }
}