# otm-mpi
Open Traffic Models - MPI communication: This a parallized version of the [OTM](https://github.com/ggomes/otm-sim) simulation engine that uses Message Passing Interface (MPI) for multi-core communication on high performance computing (HPC) systems. It can exploit parallel computation and HPC power to significantly speed up large-scale traffic simulations.

# otm-mpi scaling
The figure below show the scaling otm-mpi when simulating a synthetic grid network with 62500 nodes, 170000 Links, and 12500 origin-destination pairs. We observed a time reduction from 15,000 sec to 20 sec.

<p align="center">
<img src="https://github.com/ugirumurera/otm-mpi/blob/master/OTM_Scaling.png" width="50%">
</p>

# Installation

## Java 11

Get it [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)
+ Assign JAVA_HOME
+ Add JAVA_HOME to your PATH

## cmake
```
sudo apt install software-properties-common
sudo add-apt-repository ppa:george-edison55/cmake-3.x
sudo apt update
sudo apt install cmake
```

## environment variables
```
export OTMMPIHOME=[otm-mpi root folder]
export OTMSIMJARNAME=otm-sim-1.0-20190924.222012-1-jar-with-dependencies.jar
export OTMSIMJAR=[wherever you wish to save the OTM jar file]
```

## OTM
Download the otm-sim jar file.
```
cd ~
wget https://mymavenrepo.com/repo/XtcMAROnIu3PyiMCmbdY/edu/berkeley/ucbtrans/otm-sim/1.0-SNAPSHOT/$OTMSIMJARNAME
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
cd $OTMMPIHOME
cd ..
git clone https://github.com/ggomes/otm-mpi.git
cd $OTMMPIHOME/scripts
./build_all.sh
```

## test 
```
./test_run_all.sh
```
