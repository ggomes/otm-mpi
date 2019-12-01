#!/bin/bash

n=2
configfile=/home/gomes/code/otm/otm-tools/python/berkeley.xml
prefix=$OTMMPIHOME/test/b
duration=99
outdt=10
verbose=true

$OTMMPIHOME/scripts/run_compare_veh.sh $n $prefix $configfile $outdt $duration $verbose
