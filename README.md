# otm-mpi
Open Traffic Models - MPI communication: This a parallized version of the [OTM](https://github.com/ggomes/otm-sim) simulation engine that uses Message Passing Interface (MPI) for multi-core communication on high performance computing (HPC) systems. It can exploit parallel computation and HPC power to significantly speed up large-scale traffic simulations.

# otm-mpi scaling
The figure below show the scaling otm-mpi when simulating a synthetic grid network with 62500 nodes, 170000 Links, and 12500 origin-destination pairs. We observed a time reduction from 15,000 sec to 20 sec.

<img src="https://github.com/ugirumurera/otm-mpi/blob/master/OTM_Scaling.png" width="100"

# Reference
Ugirumurera, Juliette, and Gomes, Gabriel. OTM-MPI (Open Traffic Models - MPI). Computer Software. https://github.com/ggomes/otm-mpi. USDOE. 25 Oct. 2018. Web. doi:10.11578/dc.20181030.1.

# Installation

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

