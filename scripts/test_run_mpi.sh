#!/bin/bash

n=2
prefix=$OTMMPIHOME/test/b
duration=6
writeoutput=true
outdt=10
verbose=true

$OTMMPIHOME/scripts/run_mpi.sh $n $prefix 1 $duration $writeoutput $outdt $verbose
