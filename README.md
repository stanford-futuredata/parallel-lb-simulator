# Data-Parallel System Simulator
A simulator built in Java that models the behaviour of a system serving data-parallel queries on a set of multi-core servers. We use this to compare the performance of Parallelism-Optimizing Data Placements (PODP) against Naive Load Balanced (NLB) Data Placements.

The simulator is primarily used in our paper for running experiments on very large clusters or involving very large numbers of clusters.

**Implementation**
Our simulator represents queries as sets of shard accesses. Each
shard access is issued to a server and requires a set number of ticks
(simulated discrete units of time) to complete. In each round of
computation, every server in our simulated setup retrieves a shard
access from a queue of pending shard accesses and decrements
its remaining ticks by one, simulating it being scheduled onto a
processor. To model multi-core servers, we may retrieve and decre-
ment multiple shard accesses per server per tick. Our simulator
accurately models the performance of real data-parallel systems
such as Solr. 

**Dependencies**
The simulator requires the latest version of [CPLEX](https://www.ibm.com/analytics/cplex-optimizer) to be installed. All other dependencies are handled using Maven.

**Running the Simulator**

Compile changes:
```
mvn package
```

Run the simulator:
```
java -Djava.library.path=PATH_TO_CPLEX_INSTALLATION -cp target/my-app-1.0-SNAPSHOT.jar:src/main/java/edu/stanford/futuredata/app/cplex.jar edu.stanford.futuredata.App
```

We also have a separate test file where you can run the load balancer in isolation. This can be run by compiling the changes and then doing:

```
java -Djava.library.path=PATH_TO_CPLEX_INSTALLATION -cp target/my-app-1.0-SNAPSHOT.jar:src/main/java/edu/stanford/futuredata/app/cplex.jar edu.stanford.futuredata.LoadBalancerTest
```
