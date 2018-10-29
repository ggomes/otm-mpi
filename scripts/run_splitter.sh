#!/bin/bash
cd $OTMMPIHOME/src/main/java
javac -d $OTMMPIHOME/out_javac -cp $OTMSIMJAR:$OTMMPIHOME/lib/* metis/*.java metagraph/*.java translator/*.java xmlsplitter/*.java
cd $OTMMPIHOME/out_javac
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/50 $OTMMPIHOME/config/50_nodes.xml 4
# 0 : String prefix
# 1 : String config_file
# 2 : int num_partitions
