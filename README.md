Provides MPI communication capabilities for [OTM](https://github.com/ggomes/otm-sim).

# Installation

## Java
```
$ java -version
java version "11.0.5" 2019-10-15 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.5+10-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.5+10-LTS, mixed mode)
```

## [Open MPI](https://www.open-mpi.org/)
+ [Download](https://www.open-mpi.org/software/ompi/)  
+ Build:

```
cd $HOME
gunzip -c openmpi-4.0.5.tar.gz | tar xf -
cd openmpi-4.0.5
./configure --enable-mpi-java --with-jdk-bindir=$JAVA_HOME/bin --with-jdk-headers=$JAVA_HOME/include --prefix=$HOME/openmpi-4.0.5   
make all install
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HOME/openmpi-4.0.5/lib
export PATH=$HOME/openmpi-4.0.5/bin:$PATH
```

## [Metis](http://glaros.dtc.umn.edu/gkhome/views/metis)
```
sudo apt update
sudo apt install metis
```

## Build OTM-MPI

+ [Download the latest OTM jar file](https://mymavenrepo.com/repo/XtcMAROnIu3PyiMCmbdY/edu/berkeley/ucbtrans/otm-sim/1.0-SNAPSHOT/). This is the large jar file with the most recent date. 
+ Assign the location of the jar file to environment variable OTMSIMJAR. For example,
```
export OTMSIMJAR=$HOME/Downloads/otm-sim-1.0-20190924.222012-1-jar-with-dependencies.jar
```
+ Download the [source code](https://github.com/ggomes/otm-mpi) and build with [build_mpi.sh](https://github.com/ggomes/otm-mpi/blob/master/scripts/build_mpi.sh)

# Usage

We will assume you have an OTM scenario as an XML file. If you do not, then there are several ways to build one. See [this](https://github.com/ggomes/otm-tools). The process has two steps: scenario splitting and MPI runs. 

## 1. Scenario splitting 

+ [Sample script](https://github.com/ggomes/otm-mpi/blob/master/scripts/test_run_splitter.sh)

This generates a series of XML files corresponding to each of the subnetworks. These should be made available to the machines that will run the MPI processes. 

## 2. MPI run

+ [Sample script](https://github.com/ggomes/otm-mpi/blob/master/scripts/test_run_mpi.sh).

