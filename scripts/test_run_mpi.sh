#!/bin/bash

n=2
prefix=$OTMMPIHOME/test/50
duration=6
writeoutput=true
outdt=10

$OTMMPIHOME/scripts/run_mpi.sh $n $prefix 1 $duration $writeoutput $outdt
