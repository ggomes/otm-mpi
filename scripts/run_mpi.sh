#!/bin/bash
cd $OTMMPIHOME/src/main/java
mpijavac -d $OTMMPIHOME/out_mpijavac -cp $OTMSIMJAR:$OTMMPIHOME/lib/* metis/*.java xmlsplitter/*.java metagraph/*.java otm/*.java translator/*.java runner/RunnerMPI.java
cd $OTMMPIHOME/out_mpijavac
mpirun -np 4 java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. runner/RunnerMPI $OTMMPIHOME/test/50 2 1000 true
#* 0 string : [prefix]
#* 1 float : [sim_dt] sim dt in seconds
#* 2 float : [duration] sim duration in seconds
#* 3 boolean : [writeouput] true->write network state to files