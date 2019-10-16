#!/bin/bash
cd $OTMMPIHOME/target
java -jar otm-mpi-1.0-SNAPSHOT-jar-with-dependencies.jar $OTMMPIHOME/test/50 /home/gomes/code/otm/otm-mpi/config/50_nodes.xml 2
# 0 : String prefix
# 1 : String config_file
# 2 : int num_partitions
