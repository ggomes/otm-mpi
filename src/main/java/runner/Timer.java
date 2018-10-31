package runner;

import mpi.MPI;
import mpi.MPIException;

public class Timer {

    boolean run_mpi;
    boolean is_running;
    double last_start; // seconds
    double total_time; // seconds;

    public Timer(boolean run_mpi) {
        this.run_mpi = run_mpi;
        last_start = get_curr_time();
        is_running = true;
        total_time = 0d;
    }

    public void start(){
        if(!is_running) {
            last_start = get_curr_time();
            is_running = true;
        }
    }

    public void stop(){
        if(is_running){
            double curr_time = get_curr_time();
            total_time += curr_time - last_start;
            is_running = false;
        }
    }

    public double get_total_time() {
        stop();
        return total_time;
    }

    private double get_curr_time(){
        if(run_mpi) {
            try {
                return MPI.wtime();
            } catch (MPIException e) {
                e.printStackTrace();
                return 0d;
            }
        }
        else
            return System.nanoTime() / 1e9;
    }
}
