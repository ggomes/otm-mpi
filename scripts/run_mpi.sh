#!/bin/bash
# 1 int : number of processes
# 2 string : [prefix]
# 3 int : [repetitions]
# 4 float : [duration] sim duration in seconds
# 5 boolean : [writeouput] true->write network state to files
# 6 float : [out_dt] sim dt in seconds
cd $OTMMPIHOME/out_mpijavac
mpirun -np $1 java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. runner/RunnerMPI $2 $3 $4 $5 $6