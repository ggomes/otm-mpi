#!/bin/bash

n=2
configfile=$OTMMPIHOME/config/four_intersect.xml
prefix=$OTMMPIHOME/test/4
duration=1000
outdt=10

$OTMMPIHOME/scripts/run_compare_veh.sh $n $prefix $configfile $outdt $duration
