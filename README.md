# otm-mpi
Open Traffic Models - MPI communication: This a parallized version of the [OTM](https://github.com/ggomes/otm-sim) simulation engine that uses Message Passing Interface (MPI) for multi-core communication on high performance computing (HPC) systems. It can exploit parallel computation and HPC power to significantly speed up large-scale traffic simulations.

# otm-mpi scaling
The figure below show the scaling otm-mpi when simulating a synthetic grid network with 62500 nodes, 170000 Links, and 12500 origin-destination pairs. We observed a time reduction from 15,000 sec to 20 sec.

<p align="center">
<img src="https://github.com/ugirumurera/otm-mpi/blob/master/OTM_Scaling.png" width="50%">
</p>

# Installation

## Java 8
```
sudo add-apt-repository ppa:webupd8team/java
sudo apt update
sudo apt install oracle-java8-installer
sudo update-alternatives --config java
export JAVA_HOME=/usr/lib/jvm/java-8-oracle
cd ~
```
Add Java to your path:
```
export PATH=/usr/lib/jvm/java-8-oracle/bin:$PATH
```

## cmake
```
sudo apt install software-properties-common
sudo add-apt-repository ppa:george-edison55/cmake-3.x
sudo apt update
sudo apt install cmake
```

## environment variables
```
export OTMMPIHOME=$HOME/otm-mpi
export OTMSIMJARNAME=otm-sim-1.0-20190725.163255-36-jar-with-dependencies.jar
export OTMSIMJAR=$OTMMPIHOME/lib/otm-sim.jar
```

## OTM
Download the otm-sim jar file.
```
cd ~
wget https://mymavenrepo.com/repo/XtcMAROnIu3PyiMCmbdY/otm/otm-sim/1.0-SNAPSHOT/$OTMSIMJARNAME
mv $OTMSIMJARNAME $OTMSIMJAR
```

## OpenMPI
```
cd ~
wget https://download.open-mpi.org/release/open-mpi/v3.1/openmpi-3.1.0.tar.gz
tar -xvf openmpi-3.1.0.tar.gz
rm openmpi-3.1.0.tar.gz
cd ~/openmpi-3.1.0
./configure --enable-mpi-java --with-jdk-bindir=$JAVA_HOME/bin --with-jdk-headers=$JAVA_HOME/include --prefix=$HOME/openmpi-3.1.0
make all
make install
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:$HOME/openmpi-3.1.0/lib
export PATH=$HOME/openmpi-3.1.0/bin:$PATH
```

## Metis
Download and install [Metis](http://glaros.dtc.umn.edu/gkhome/metis/metis/download).
Then add Metis to the PATH:
```
export PATH=/opt/apps/intel18/metis/5.0.2/bin:$PATH
```
Alternatively you may only need to activate the Metis module. 
```
module load metis
```

## clone and build otm-mpi
```
cd ~
git clone https://github.com/ggomes/otm-mpi.git
cd $OTMMPIHOME/src/main/java
mpijavac -d $OTMMPIHOME/out_mpijavac -cp $OTMSIMJAR:$OTMMPIHOME/lib/* runner/Timer.java metis/*.java xmlsplitter/*.java metagraph/*.java otm/*.java translator/*.java runner/RunnerMPI.java
```

## test 
```
cd $OTMMPIHOME/src/main/java
javac -d $OTMMPIHOME/out_javac -cp $OTMSIMJAR:$OTMMPIHOME/lib/* metis/*.java metagraph/*.java translator/*.java xmlsplitter/*.java
cd $OTMMPIHOME/out_javac
java -cp $OTMSIMJAR:$OTMMPIHOME/lib/*:. xmlsplitter.XMLSplitter $OTMMPIHOME/test/50 $OTMMPIHOME/config/50_nodes.xml 4
```

# Scripts
* ./run_splitter.sh : Test offline scenario splitting with a small network.
* ./compile_mpi.sh : Compile the program using mpijavac.
* ./compare_veh.sh : Run small example with 2 processes, and compare result to single-process run. 
* ./run_mpi.sh : Run small example with 4 processes.

