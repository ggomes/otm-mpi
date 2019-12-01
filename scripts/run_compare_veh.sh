#!/bin/bash
# 1 int : number of processes
# 2 string : prefix
# 3 string : config file
# 4 float : out dt in seconds
# 5 float : duration in seconds
# 6 boolean : verbose
cd $OTMMPIHOME/out_javac
java -cp $OTMSIMJAR:. tests.CompareVeh $1 $2 $3 $4 $5 $6
