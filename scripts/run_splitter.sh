#!/bin/bash
# 1 : String prefix
# 2 : String config_file
# 3 : int num_partitions
# 4 : boolean verbose
java -jar $OTMMPIHOME/target/otm-mpi-1.0-SNAPSHOT-jar-with-dependencies.jar $1 $2 $3 $4
