#!/bin/bash
cd $OTMMPIHOME
mvn clean
mvn package -DskipTests