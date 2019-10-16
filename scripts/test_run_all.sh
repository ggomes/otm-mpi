#!/bin/bash

prefix=$OTMMPIHOME/test/4nrc
configfile=$OTMMPIHOME/config/four_intersect.xml
n=2
duration=1000
writeoutput=true
outdt=10

$OTMMPIHOME/scripts/run_all.sh $prefix $configfile $n $duration $writeoutput $outdt
