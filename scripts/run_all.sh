#!/bin/bash
# 1 prefix
# 2 configfile
# 3 n
# 4 duration
# 5 writeoutput
# 6 outdt

$OTMMPIHOME/scripts/run_splitter.sh $1 $2 $3
$OTMMPIHOME/scripts/run_mpi.sh $3 $1 1 $4 $5 $6
$OTMMPIHOME/scripts/run_compare_veh.sh $3 $1 $2 $6 $4