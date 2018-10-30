#!/bin/bash
cd $OTMMPIHOME/src/main/java
mpijavac -d $OTMMPIHOME/out_mpijavac -cp $OTMSIMJAR:$OTMMPIHOME/lib/* metis/*.java xmlsplitter/*.java metagraph/*.java otm/*.java translator/*.java runner/RunnerMPI.java
