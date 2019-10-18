#!/bin/bash

n=2
configfile=$OTMMPIHOME/config/four_intersect.xml
prefix=$OTMMPIHOME/test/4

$OTMMPIHOME/scripts/run_splitter.sh $prefix $configfile $n
