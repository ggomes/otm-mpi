#!/bin/bash

n=2
configfile=$OTMMPIHOME/config/50_nodes.xml
# configfile=/home/gomes/Desktop/test/small_Miami.xml
prefix=$OTMMPIHOME/test/50
duration=1000
outdt=10

$OTMMPIHOME/scripts/run_compare_veh.sh $n $prefix $configfile $outdt $duration
