#!/bin/bash

n=2
configfile=$OTMMPIHOME/config/four_intersect.xml
prefix=$OTMMPIHOME/test/4
duration=1000
outdt=10
writeoutput=true

$OTMMPIHOME/scripts/run_all.sh $prefix $configfile $n $duration $writeoutput $outdt
