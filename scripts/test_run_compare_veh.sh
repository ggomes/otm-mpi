#!/bin/bash

n=2
configfile=$OTMMPIHOME/config/50_nodes.xml
prefix=$OTMMPIHOME/test/n2
duration=3600
outdt=10
verbose=true

$OTMMPIHOME/scripts/run_compare_veh.sh $n $prefix $configfile $outdt $duration $verbose
