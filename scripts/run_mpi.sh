#!/bin/bash
cd $OTMMPIHOME/src/main/java
mpijavac -d $OTMMPIHOME/out_mpijavac -cp $OTMSIMJAR:$OTMMPIHOME/lib/* metis/*.java xmlsplitter/*.java metagraph/*.java otm/*.java translator/*.java runner/*.java
cd $OTMMPIHOME/out_mpijavac
mpirun -np 4 java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. runner/RunnerMPI $OTMMPIHOME/test/50 1 1000 true 10
#* 0 string : [prefix]
#* 1 int : [repetitions] 
#* 2 float : [duration] sim duration in seconds
#* 3 boolean : [writeouput] true->write network state to files
#* 4 float : [sim_dt] sim dt in seconds