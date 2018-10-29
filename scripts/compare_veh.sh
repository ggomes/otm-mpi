#!/usr/bin/env bash
cd ../src/main/java
mpijavac -d $OTMMPIHOME/out_mpijavac -cp $OTMSIMJAR otm/*.java metagraph/*.java metis/*.java translator/*.java runner/*.java xmlsplitter/*.java ../../test/java/tests/CompareVeh.java
cd $OTMMPIHOME/out_mpijavac
mpirun -np 2 java -cp $OTMSIMJAR tests/CompareVeh true true ../config/100.xml 1 10 3600 ../test/compareVeh/ ../test/compareVeh/_
# 0 boolean : [run metis] true -> run metis
# 1 boolean : [write_otm_output] true -> request and write otm output files
# 2 string : [config_file_name] config file name
# 3 float : [sim_dt] sim dt in seconds
# 4 float : [out_dt] out dt in seconds
# 5 float : [duration] sim duration in seconds
# 6 string : [output_folder] output folder for otm and runner output
# 7 string : [metis_prefix] prefix for metis output files