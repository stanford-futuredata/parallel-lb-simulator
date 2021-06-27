# parallel-lb-simulator
A simulator for distributed databases built in Java and Python. 

**Dependencies**

Python 3: numpy, seaborn, matplotlib.
These libraries can be installed with pip i.e 
```
pip3 install numpy
pip3 install seaborn
pip3 install matplotlib
```

**Running the simulator**

You will first need to clone the repo. There is a shell script at the root folder of the project which compiles, runs, and displays the results of the simulator. This shell script can be run with the following command:
```./run_sim.sh NUM_MACHINES NUM_CORES_PER_MACHINE NUM_SHARDS_PER_MACHINE NUM_QUERIES NUM_SHARD_ACCESS_PER_QUERY SECONDS_PER_ACCESS AVG_QUERIES_PER_SECOND USES_UNIFORM_SHARD_DISTRIBUTION```. An explanation for each of these parameters can be found below. An example run of the simulator would be:
```./run_sim.sh 15 3 10 10000 40 1 1 false```. This results in a run of the simulator with the following output:
```
Compiling Java program.
Running Java program.
---------------------------
SIMULATOR PARAMETERS
---------------------------
Number of machines: 15
Number of cores per machine: 3
Number of shards per machine: 10
Number of queries: 10000
Number of shard accesses per query: 40
Seconds for each shard access: 1.0
Average rate of queries: 1.0
Shard accesses are uniformly distributed: false
```


**Simulator parameters**

**NUM_MACHINES:**
The first argument to the `run_sim.sh` program is the number of servers that the simulator is simulating. Can be any integer >= 1.

**NUM_CORES_PER_MACHINE:**
The second argument is the number of cores that each server has. We assume that a single core can execute a single shard-access at once, so the number of cores in a machine is how many shard accesses can happen concurrently on the machine. Can be any integer >= 1.

**NUM_SHARDS_PER_MACHINE:**
This is the number of shards that each machine has. Regardless of whether we are storing shards randomly or round-robin, each machine has a fixed number of shards which is specified by this number. Can be any integer >= 1

**NUM_QUERIES:**
This is the number of queries that are generated and run by the simulator. A query represents a single consecutive range of shard accesses (for example, shard accesses 1-15 could represent a single query). Can be any integer >= 1

**NUM_SHARD_ACCESS_PER_QUERY:**
As mentioned before, a query is simply a single consecutive range of shard accesses. This parameter controls how many shard accesses are contained in a single query e.g 20 shard accesses per query means that each query accesses 20 consecutive shards. **You can choose to access a random number of shards per query by supplying -1 for this parameter.** Can be any integer -1 <= NUM_SHARD_ACCESS_PER_QUERY <= NUM_MACHINES * NUM_SHARDS_PER_MACHINE (i.e total number of shards).

You can see how shard-load is affected by the number of shard accesses per query by playing around with this graph https://www.desmos.com/calculator/kukqmg7ywr. In the attached graph, n is the total number of shards and t is the number of shard accesses per query.

**SECONDS_PER_ACCESS:**
This parameter represents how long each shard-access takes to run in a given unit time.

**AVG_QUERIES_PER_SECOND:**
This parameter represents the average number of queries that occur each second. The random query generator creates new queries at certain times based on a Poisson distribution, based around this parameter.

**USES_UNIFORM_SHARD_DISTRIBUTION:**
The query generator can generate shard accesses based around two different distributions. 

One distribution is a non-uniform shard distribution which means that queries cannot wrap around the total number of shards. For example, if we have 100 shards, we would be able to access shards 40-80 and 80-100 but not 90-20. This results in a shard-load distribution as below (for n=100):

![image](https://user-images.githubusercontent.com/7289955/123534648-65d78a00-d6d3-11eb-954b-4a0c074e8064.png)

To play around with how shard-load varies by the total number of shards (n), you can play around with this graph https://www.desmos.com/calculator/oxbblcd84q. To run a simulation with this shard distribution, set the argument to **false**.

The other distribution is a uniform shard distribution. This is created by allowing queries to wrap around the total number of shards. For example, if we have 10 shards, both 4-8 and 8-2 are valid shard access ranges. Every shard is equally likely to be accessed with this distribution. You can use this distribution by setting the argument to **true**.


