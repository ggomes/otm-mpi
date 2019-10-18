Open Traffic Models with MPI communication. This project provides MPI communication capabilities for the [OTM simulator](https://github.com/ggomes/otm-sim). It uses [Open MPI](https://www.open-mpi.org/) for multi-core communication on high performance computing systems.

# Usage

We will assume you have an OTM scenario as an XML file. If you do not, then there are several ways to build one. See [this](https://github.com/ggomes/otm-tools). The process has two steps: scenario splitting and MPI runs. Both require [Java 11](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html).
```
$ java -version
java version "11.0.5" 2019-10-15 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.5+10-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.5+10-LTS, mixed mode)
```

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

### Prerequisites

#### Install Open MPI
```
cd $HOME
wget https://download.open-mpi.org/release/open-mpi/v3.1/openmpi-3.1.0.tar.gz
tar -xvf openmpi-3.1.0.tar.gz
rm openmpi-3.1.0.tar.gz
cd $HOME/openmpi-3.1.0
./configure --enable-mpi-java --with-jdk-bindir=$JAVA_HOME/bin --with-jdk-headers=$JAVA_HOME/include --prefix=$HOME/openmpi-3.1.0
make all
make install
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HOME/openmpi-3.1.0/lib
export PATH=$HOME/openmpi-3.1.0/bin:$PATH
```

#### Clone OTM MPI
```
export OTMMPIHOME=[otm-mpi root folder]
cd $OTMMPIHOME/..
git clone https://github.com/ggomes/otm-mpi.git
```

#### OTM simulator
Obtain the OTM jar file [here](https://mymavenrepo.com/repo/XtcMAROnIu3PyiMCmbdY/edu/berkeley/ucbtrans/otm-sim/1.0-SNAPSHOT/). Download the latest large jar file. We will use the OTMSIMJAR environment variable to refer to this file. For example,
```
export OTMSIMJAR=/home/username/Downloads/otm-sim-1.0-20190924.222012-1-jar-with-dependencies.jar
```

#### Build OTM MPI
Run [build_mpi.sh](https://github.com/ggomes/otm-mpi/blob/master/scripts/build_mpi.sh)

### Run
The command for running OTM MPI is provided [here](https://github.com/ggomes/otm-mpi/blob/master/scripts/run_mpi.sh), and a test is provided [here](https://github.com/ggomes/otm-mpi/blob/master/scripts/test_run_mpi.sh).

# Building the splitter from source

To build the OTM MPI jar file from source you will need to install Metis, which in turn requires cmake. Below are instructions for doing this.

## cmake
```
sudo apt install software-properties-common
sudo add-apt-repository ppa:george-edison55/cmake-3.x
sudo apt update
sudo apt install cmake
```

## Metis
Download and install [Metis](http://glaros.dtc.umn.edu/gkhome/metis/metis/download).
Then add Metis to the PATH:
```
export PATH=/opt/apps/intel18/metis/5.0.2/bin:$PATH
```

# Sample results
The figure below show the scaling otm-mpi when simulating a synthetic grid network with 62500 nodes, 170000 Links, and 12500 origin-destination pairs. We observed a time reduction from 15,000 sec to 20 sec.

<p align="center">
<img src="https://github.com/ugirumurera/otm-mpi/blob/master/OTM_Scaling.png" width="50%">
</p>
