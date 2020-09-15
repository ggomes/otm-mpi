Provides MPI communication capabilities for [OTM](https://github.com/ggomes/otm-sim).

# Installation


## [Java]
```
$ java -version
java version "11.0.5" 2019-10-15 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.5+10-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.5+10-LTS, mixed mode)
```

## [Open MPI](https://www.open-mpi.org/)
+ [Download](https://www.open-mpi.org/software/ompi/)  
+ [Build](https://www.open-mpi.org/faq/?category=building#easy-build). When running `.configure`, use `--enable-mpi-java` ([this enables mpijavac]()), as shown below:

```
cd $HOME
gunzip -c openmpi-4.0.5.tar.gz | tar xf -
cd openmpi-4.0.5
./configure --enable-mpi-java --with-jdk-bindir=$JAVA_HOME/bin --with-jdk-headers=$JAVA_HOME/include --prefix=$HOME/openmpi-4.0.5   
make all install
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HOME/openmpi-4.0.5/lib
export PATH=$HOME/openmpi-4.0.5/bin:$PATH
```

## [Metis](http://glaros.dtc.umn.edu/gkhome/metis/metis/download).
+ Download, install, and add to PATH:
```
export PATH=/opt/apps/intel18/metis/5.0.2/bin:$PATH
```

## Build OTM-MPI

+ [Download the latest OTM jar file](https://mymavenrepo.com/repo/XtcMAROnIu3PyiMCmbdY/edu/berkeley/ucbtrans/otm-sim/1.0-SNAPSHOT/). This is the large jar file with the most recent date. 
+ Assign the location of the jar file to environment variable OTMSIMJAR. For example,
```
export OTMSIMJAR=$HOME/Downloads/otm-sim-1.0-20190924.222012-1-jar-with-dependencies.jar
```
+ Download the [source code](https://github.com/ggomes/otm-mpi) and build with [build_mpi.sh](https://github.com/ggomes/otm-mpi/blob/master/scripts/build_mpi.sh)

# Usage

We will assume you have an OTM scenario as an XML file. If you do not, then there are several ways to build one. See [this](https://github.com/ggomes/otm-tools). The process has two steps: scenario splitting and MPI runs. Both require [Java 11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html).


## Scenario splitting 

The first step is to split the network into parts for each compute process. This is done with the OTM MPI splitter, which is packaged with the OTM MPI jar file. You can obtain the jar file [here](https://mymavenrepo.com/repo/XtcMAROnIu3PyiMCmbdY/edu/berkeley/ucbtrans/otm-mpi/1.0-SNAPSHOT/) (download the most recent large jar file). Or you can build it from source using [Maven](http://maven.apache.org/). See [this](https://github.com/ggomes/otm-mpi/blob/master/scripts/build_splitter.sh). To build the jar you will need to install Metis following [these](#building-the-splitter-from-source) steps. 

This [script](https://github.com/ggomes/otm-mpi/blob/master/scripts/run_splitter.sh) shows how to run the splitter:
```
java -jar <OTM MPI jar file> [prefix] [config file] [number of pieces]
```

+ [prefix]: This is a string that is prepended to all output files. e.g. prefix=`/home/username/otm-mpi-output/myrun` will put all output files into the `/home/username/otm-mpi-output/` folder and prepend them with `myrun`.

+ [config file]: The absolute path and name for the OTM XML file. 

+ [number of pieces]: Number of separate scenarios to create, corresponding to the number of MPI processes.

Once this completes, you should see a series of XML files in the output folder, corresponding to each of the sub-scenarios. These should be made available to the machine that will run the MPI processes. 


## MPI run

### Run
The command for running OTM MPI is provided [here](https://github.com/ggomes/otm-mpi/blob/master/scripts/run_mpi.sh), and a test is provided [here](https://github.com/ggomes/otm-mpi/blob/master/scripts/test_run_mpi.sh).


# Sample results
The figure below show the scaling otm-mpi when simulating a synthetic grid network with 62500 nodes, 170000 Links, and 12500 origin-destination pairs. We observed a time reduction from 15,000 sec to 20 sec.

<p align="center">
<img src="https://github.com/ugirumurera/otm-mpi/blob/master/OTM_Scaling.png" width="50%">
</p>
