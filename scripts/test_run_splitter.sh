#!/bin/bash

n=2
configfile=/home/gomes/code/otm/otm-tools/python/berkeley.xml
prefix=$OTMMPIHOME/test/b
verbose=true

$OTMMPIHOME/scripts/run_splitter.sh $prefix $configfile $n $verbose
