#!/bin/bash

prefix=$OTMMPIHOME/test/4
configfile=$OTMMPIHOME/config/four_intersect.xml
n=2

$OTMMPIHOME/scripts/run_splitter.sh $prefix $configfile $n
