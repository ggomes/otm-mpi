#!/bin/bash

n=2
configfile=$OTMMPIHOME/config/50_nodes.xml
prefix=$OTMMPIHOME/test/n2
verbose=true

$OTMMPIHOME/scripts/run_splitter.sh $prefix $configfile $n $verbose
