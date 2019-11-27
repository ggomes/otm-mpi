#!/bin/bash

n=2
#configfile=$OTMMPIHOME/config/scenario_25_nodes.xml
configfile=$OTMMPIHOME/config/50_nodes.xml
#configfile=/home/gomes/Desktop/test/small_Miami.xml
#configfile=/home/gomes/code/otm/otm-tools/python/berkeley.xml
#configfile=/home/gomes/code/otm/otm-tools/python/berkeley2.xml
#configfile=/home/gomes/code/otm/otm-tools/python/anchorage.xml
prefix=$OTMMPIHOME/test/50
duration=10
outdt=10
writeoutput=true

$OTMMPIHOME/scripts/run_all.sh $prefix $configfile $n $duration $writeoutput $outdt
