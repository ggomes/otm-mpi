#!/bin/bash

prefix=$OTMMPIHOME/test/4
configfile=$OTMMPIHOME/config/four_intersect.xml
n=2
duration=1000
writeoutput=true
outdt=10

$OTMMPIHOME/scripts/run_mpi.sh $n $prefix 1 $duration $writeoutput $outdt
