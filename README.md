# otm-mpi
Open Traffic Models - MPI communication: This a parallized version of the [OTM](https://github.com/ggomes/otm-sim) simulation engine that uses Message Passing Interface (MPI) for multi-core communication on high performance computing (HPC) systems. It can exploit parallel computation and HPC power to significantly speed up large-scale traffic simulations.

# Installation

```
openmpi
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

## Third party dependencies

+ Download [open-mpi 3.1](https://www.open-mpi.org/software/ompi/v3.1/).
+ Install Metis. Two methods:
  + [metis-5.1.0.tar.gz](http://glaros.dtc.umn.edu/gkhome/metis/metis/download). Follow the instructions in Install.txt. To build METIS you will need to have cmake. 
  + Ubuntu: sudo apt install metis

## Install otm-sim
```
git clone git@github.com:ggomes/otm-sim.git
cp otm-sim/settings.xml ~/.m2
cd otm-sim
git checkout remotes/origin/ctmrefac
mvn clean install -DskipTests
```
Alternatively you can download the otm-sim jar [here](https://mymavenrepo.com/repo/XtcMAROnIu3PyiMCmbdY/otm/otm-sim/1.0-SNAPSHOT/) 

## Build in IntelliJ


+ IntelliJ > Import project > otm-mpi > create project from existing sources ...
+ Add otm-sim and json jar files
```
Files > Project Structure > Project Settings > Modules
	> Dependencies > + JARs or directories
 		> otm-sim/target/otm-sim-1.0-SNAPSHOT-jar-with-dependencies.jar
   			check "Export" for otm-sim   
		> otm-mpi/lib/json-simple-1.1.1.jar
	> Sources > Add Content Root
		> openmpi-3.1.0/ompi/mpi/java
			Edit Root Properties > Package prefix : mpi
```

## Test in IntelliJ
* Run test.tests.XMLSplitterTest.main()
   
## Environment
```
export OTMSIMJAR=<path to otm-sim jar>
export OTMMPIHOME=<path to otm-mpi folder>
chmod u+x $OTMMPIHOME/scripts/*.sh
```

## Scripts

* ./run_splitter.sh : Test offline scenario splitting with a small network.
* ./compile_mpi.sh : Compile the program using mpijavac.
* ./compare_veh.sh : Run small example with 2 processes, and compare result to single-process run. 
* ./run_mpi.sh : Run small example with 4 processes.

