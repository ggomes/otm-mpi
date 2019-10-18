#!/bin/bash
cd $OTMMPIHOME/src/main/java
mpijavac -d $OTMMPIHOME/out_mpijavac -cp $OTMSIMJAR:$OTMMPIHOME/lib/* runner/Timer.java metis/*.java metagraph/*.java otm/*.java translator/*.java runner/RunnerMPI.java
