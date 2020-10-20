#!/bin/bash

n=2
prefix=$OTMMPIHOME/test/n2
duration=3600
writeoutput=true
outdt=10
verbose=true

$OTMMPIHOME/scripts/run_mpi.sh $n $prefix 1 $duration $writeoutput $outdt $verbose
