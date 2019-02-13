# otm-mpi
Open Traffic Models - MPI communication: This a parallized version of the [OTM](https://github.com/ggomes/otm-sim) simulation engine that uses Message Passing Interface (MPI) for multi-core communication on high performance computing (HPC) systems. It can exploit parallel computation and HPC power to significantly speed up large-scale traffic simulations.

## Installation

# Environment

1. export OTMSIMJAR=< path to otm-sim [jar file](https://mymavenrepo.com/repo/XtcMAROnIu3PyiMCmbdY/otm/otm-sim/1.0-SNAPSHOT/) >
2. export OTMMPIHOME=< path to otm-mpi >
3. chmod u+x scripts/*.sh

# Third-party libaries

4. [metis-5.1.0.tar.gz](http://glaros.dtc.umn.edu/gkhome/metis/metis). Follow the build instructions. Not a trivial step.
5. [open-mpi 3.1](https://www.open-mpi.org/software/ompi/v3.1/).

# Build with IntelliJ, no Maven

5. Import project > Create project from existing sources
6. Add otm-sim jar: Project Structure -> Modules -> Dependencies -> + JARs or directories...
7. Add openmpi source: Project Structure -> Modules -> Sources
   * Add Content Root: openmpi-3.1.0/ompi/mpi/java
   * Change the package prefix to "mpi"

# Build with IntelliJ, with Maven

6. Import project > Maven
7. sdg

## NOTES FOR LATER
1. Add lib/json.1.1.1.jar

