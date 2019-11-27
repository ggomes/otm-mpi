#!/bin/bash

n=2
configfile=$OTMMPIHOME/config/50_nodes.xml
#configfile=$OTMMPIHOME/config/scenario_25_nodes.xml
# configfile=/home/gomes/Desktop/test/small_Miami.xml
prefix=$OTMMPIHOME/test/50

$OTMMPIHOME/scripts/run_splitter.sh $prefix $configfile $n
