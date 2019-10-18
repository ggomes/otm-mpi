#!/bin/bash
cd $OTMMPIHOME/src/test/java/tests
javac -d $OTMMPIHOME/out_javac -cp $OTMSIMJAR CompareVeh.java
