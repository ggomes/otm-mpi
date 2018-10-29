#!/bin/bash
cd $OTMMPIHOME/src/test/java/tests
javac -d $OTMMPIHOME/out_javac -cp $OTMSIMJAR CompareVeh.java
cd $OTMMPIHOME/out_javac
java -cp $OTMSIMJAR:. tests.CompareVeh 4 $OTMMPIHOME/test/50 $OTMMPIHOME/config/50_nodes.xml 2 1000
# 0 int : [num_partitions]
# 1 string : [prefix]
# 2 string : [config_file]
# 3 float : [sim_dt] seconds
# 4 float : [duration] seconds
