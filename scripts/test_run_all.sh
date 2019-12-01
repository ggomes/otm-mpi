#!/bin/bash

n=2
# configfile=$OTMMPIHOME/config/50_nodes.xml						# OK
configfile=/home/gomes/Desktop/test/small_Miami.xml				# OK
# configfile=/home/gomes/code/otm/otm-tools/python/berkeley.xml		# OK
prefix=$OTMMPIHOME/test/5
duration=10
outdt=10
writeoutput=true
verbose=false

$OTMMPIHOME/scripts/run_all.sh $prefix $configfile $n $duration $writeoutput $outdt $verbose
