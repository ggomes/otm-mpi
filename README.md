# otm-mpi
Open Traffic Models - MPI communication: This a parallized version of the [OTM](https://github.com/ggomes/otm-sim) simulation engine that uses Message Passing Interface (MPI) for multi-core communication on high performance computing (HPC) systems. It can exploit parallel computation and HPC power to significantly speed up large-scale traffic simulations.

# Installation

```
% openmpi
wget https://download.open-mpi.org/release/open-mpi/v3.1/openmpi-3.1.0.tar.gz
tar -xvf openmpi-3.1.0.tar.gz
rm openmpi-3.1.0.tar.gz
cd ~/openmpi-3.1.0
./configure --enable-mpi-java --with-jdk-bindir=$JAVA_HOME/bin --with-jdk-headers=$JAVA_HOME/include --prefix=$HOME/openmpi-3.1.0
make all
make install
cd ~

% otm-mpi
cd ~
git clone https://github.com/ggomes/otm-mpi.git
echo 'export OTMMPIHOME=${HOME}/otm-mpi' >> ~/.bashrc
source ~/.bashrc

% otm-sim
cd ~
export OTMSIMJARNAME=otm-sim-1.0-20190724.201518-33-jar-with-dependencies.jar
echo 'export OTMSIMJAR=${OTMMPIHOME}/lib/otm-sim.jar' >> ~/.bashrc
source ~/.bashrc
wget https://mymavenrepo.com/repo/XtcMAROnIu3PyiMCmbdY/otm/otm-sim/1.0-SNAPSHOT/$OTMSIMJARNAME
mv $OTMSIMJARNAME $OTMSIMJAR

% compile otm-mpi
cd $OTMMPIHOME/src/main/java
mpijavac -d $OTMMPIHOME/out_mpijavac -cp $OTMSIMJAR:$OTMMPIHOME/lib/* runner/Timer.java metis/*.java xmlsplitter/*.java metagraph/*.java otm/*.java translator/*.java runner/RunnerMPI.java

% metis
module load metis
export PATH=/opt/apps/intel18/metis/5.0.2/bin:$PATH

% test splitter
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

