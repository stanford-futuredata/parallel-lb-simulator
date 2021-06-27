//*******************************************************************
//  App.java
//
// The main entry point for the simulator. Parses user input from 
// command line and runs a simulation for both a round-robin and 
// random server configuration (the sequential configuration is 
// commented out). To run, call ./run_sim.sh with relevant args.
//*******************************************************************

import java.util.Vector;
import java.io.*;

public class App {

    // Constants for server generation
    public static int NUM_MACHINES;
    public static int NUM_CORES_PER_MACHINE;
    public static int NUM_SHARDS_PER_MACHINE;

    // Constants for query generation
    public static int NUM_QUERIES;
    public static int NUM_SHARD_ACCESS_PER_QUERY;
    public static double SECONDS_PER_ACCESS = 1;
    public static double AVG_QUERIES_PER_SECOND = 2;
    public static Boolean UNIFORM_SHARD_DISTRIBUTION;

    public static void main(String[] args) throws Exception {
        int totalNumShardAccesses = 1000000;
        UNIFORM_SHARD_DISTRIBUTION = false;
        FileWriter fw = new FileWriter("OUTPUT.txt", true);

        for (int machines = 2; machines < 50; machines += 10) {
            for (int shards = 1; shards < 50; shards += 10) {
                for (int cores = 1; cores < shards; cores += 5) {
                    for (int shard = 1; shard < machines * shards; shard += 20) {
                        NUM_MACHINES = machines;
                        NUM_CORES_PER_MACHINE = cores;
                        NUM_SHARDS_PER_MACHINE = shards;

                        NUM_SHARD_ACCESS_PER_QUERY = shard;
                        NUM_QUERIES = totalNumShardAccesses / NUM_SHARD_ACCESS_PER_QUERY;

                        System.out.println("---------------------------");
                        System.out.println("SIMULATOR PARAMETERS");
                        System.out.println("---------------------------");
                        System.out.println("Number of machines: " + NUM_MACHINES);
                        System.out.println("Number of cores per machine: " + NUM_CORES_PER_MACHINE);
                        System.out.println("Number of shards per machine: " + NUM_SHARDS_PER_MACHINE);
                        System.out.println("Number of queries: " + NUM_QUERIES);
                        System.out.println("Number of shard accesses per query: " + NUM_SHARD_ACCESS_PER_QUERY);
                        System.out.println("Seconds for each shard access: " + SECONDS_PER_ACCESS);
                        System.out.println("Average rate of queries: " + AVG_QUERIES_PER_SECOND);
                        System.out.println("Shard accesses are uniformly distributed: " + UNIFORM_SHARD_DISTRIBUTION);
                        System.out.println();

                        // Generate random queries given parameters
                        QueryGenerator randomQueries = new QueryGenerator(NUM_QUERIES, SECONDS_PER_ACCESS,
                                AVG_QUERIES_PER_SECOND, NUM_SHARD_ACCESS_PER_QUERY, UNIFORM_SHARD_DISTRIBUTION);

                        // Create a server manager for the round-robin configuration and assign queries
                        // to this server configuration
                        ServerManager manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE,
                                NUM_CORES_PER_MACHINE, ServerManager.ServerType.ROUND_ROBIN);
                        randomQueries.assignQueries(manager);
                        manager.startAllServers(false); // false indicates we don't output statistics everytime
                                                        // a shard access has finished
                        randomQueries.outputStatistics(NUM_MACHINES + ", " + NUM_CORES_PER_MACHINE + ", "
                                + NUM_SHARDS_PER_MACHINE + ", " + NUM_QUERIES + ", " + NUM_SHARD_ACCESS_PER_QUERY + ", "
                                + SECONDS_PER_ACCESS + ", " + AVG_QUERIES_PER_SECOND, "Round-Robin", fw);

                        // Create a server manager for the random configuration and assign queries to
                        // this server configuration
                        manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE,
                                ServerManager.ServerType.RANDOM);
                        randomQueries.assignQueries(manager);
                        manager.startAllServers(false); // false indicates we don't output statistics everytime
                                                        // a shard access has finished
                        randomQueries.outputStatistics("", "Random", fw);
                        System.out.println();
                    }
                }
            }
        }
        fw.close();
    }

    // TESTCASES (call functions to activate unit tests)
    // NOTE: Some of these may fail at the graph plotting stage. These are for
    // testing the simulator rather than the grapher
    // so focus on the simulator output rather than the Python graph errors

    // 1) Simultaneous accesses on single-core machine should be sequential
    public static void serialQueries() throws Exception {
        Vector<Query> allQueries = new Vector<Query>();
        System.out.println("\nSTARTING TESTCASE 1\n----------------------");
        ServerManager manager = new ServerManager(1, /* numShards */ 5, /* numCores */ 1,
                ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 1f);
        newQuery.assignShards(0, 1, 2);
        allQueries.add(newQuery);

        newQuery = new Query(0, manager, 1f);
        newQuery.assignShards(3, 4);
        allQueries.add(newQuery);

        manager.startAllServers();
        // QueryGenerator.outputStatistics("serialQueries", "Test Case 1", allQueries);
    }

    // 2) Simultaneous accesses with different lengths on single-core machine should
    // be sequential
    public static void serialQueriesVariableDuration() throws Exception {
        Vector<Query> allQueries = new Vector<Query>();
        System.out.println("\nSTARTING TESTCASE 2\n----------------------");
        ServerManager manager = new ServerManager(1, 5, 1, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with
                                                                                                 // 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(0, 1, 2);
        allQueries.add(newQuery);

        newQuery = new Query(0, manager, 1.5f);
        newQuery.assignShards(2, 3, 4);
        allQueries.add(newQuery);

        manager.startAllServers();
        // QueryGenerator.outputStatistics("serialQueriesVariableDuration", "Test Case
        // 2", allQueries);
    }

    // 3) Simultaneous accesses possible if number of cores >= number of access
    public static void parallelQueriesSingleMachine() throws Exception {
        System.out.println("\nSTARTING TESTCASE 3\n----------------------");
        ServerManager manager = new ServerManager(1, 5, 5, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with
                                                                                                 // 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(0, 1, 2, 3, 4);
        manager.startAllServers();
    }

    // 4) Simultaneous accesses not possible if number of cores < number of access
    public static void semiParallelQueriesSingleMachine() throws Exception {
        System.out.println("\nSTARTING TESTCASE 4\n----------------------");
        ServerManager manager = new ServerManager(1, 5, 5, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with
                                                                                                 // 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(0, 1, 2, 3, 4, 4);
        manager.startAllServers();
    }

    // 5) Parallel accesses possible on single-core machines over multiple machines
    public static void parallelQueriesMultipleMachines() throws Exception {
        System.out.println("\nSTARTING TESTCASE 5\n----------------------");
        ServerManager manager = new ServerManager(4, 4, 1, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with
                                                                                                 // 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 5, 9, 13);
        manager.startAllServers();
    }

    // 6) Accesses are asynchronous
    public static void parallelQueriesAsync() throws Exception {
        System.out.println("\nSTARTING TESTCASE 6\n----------------------");
        ServerManager manager = new ServerManager(1, 4, 2, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with
                                                                                                 // 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1);

        newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1);

        newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(2);

        manager.startAllServers();
    }
}