import java.util.Vector;

public class App {

    // Constants for server generation
    public static final int NUM_MACHINES = 50;
    public static final int NUM_CORES_PER_MACHINE = 1;
    public static final int NUM_SHARDS_PER_MACHINE = 50;

    // Constants for query generation
    public static final int NUM_QUERIES = 100000;
    public static final int NUM_SHARD_ACCESS_PER_QUERY = 3;
    public static final double SECONDS_PER_ACCESS = 1;
    public static final double AVG_QUERIES_PER_SECOND = 5;

    public static void main(String[] args) throws Exception {
        QueryGenerator randomQueries = new QueryGenerator(NUM_QUERIES, SECONDS_PER_ACCESS, AVG_QUERIES_PER_SECOND,
                NUM_SHARD_ACCESS_PER_QUERY, true);

        // Generate sequential servers
        // ServerManager manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE,
        //         ServerManager.ServerType.SEQUENTIAL);
        // randomQueries.assignQueries(manager);
        // manager.startAllServers(false);
        // randomQueries.outputStatistics("Latency for sequential shard storage (20,000 x random shard accesses).",
        //         "Sequential");

        ServerManager manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE,
                ServerManager.ServerType.ROUND_ROBIN);
        randomQueries.assignQueries(manager);
        manager.startAllServers(false);
        randomQueries.outputStatistics("Latency for round-robin shard storage (100,000 x 3 shard accesses).",
                "Round Robin");
                

        manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE,
                ServerManager.ServerType.RANDOM);
        randomQueries.assignQueries(manager);
        manager.startAllServers(false);
        randomQueries.outputStatistics("Latency for random shard storage (100000 x 3 shard accesses).",
                "Random");


        // Generate sequential servers
        // randomQueries = new QueryGenerator(NUM_QUERIES, SECONDS_PER_ACCESS, AVG_QUERIES_PER_SECOND,
        //         27);
        // manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE,
        //         ServerManager.ServerType.SEQUENTIAL);
        // randomQueries.assignQueries(manager);
        // manager.startAllServers(false);
        // randomQueries.outputStatistics("Latency for sequential shard storage with (5000 x 27 shard accesses).",
        //         "27");


        // randomQueries = new QueryGenerator(NUM_QUERIES, SECONDS_PER_ACCESS, AVG_QUERIES_PER_SECOND,
        //         29);
        // manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE,
        //         ServerManager.ServerType.SEQUENTIAL);
        // randomQueries.assignQueries(manager);
        // manager.startAllServers(false);
        // randomQueries.outputStatistics("Latency for sequential shard storage with (5000 x 29 shard accesses).",
        //         "29");

        // randomQueries = new QueryGenerator(NUM_QUERIES, SECONDS_PER_ACCESS, AVG_QUERIES_PER_SECOND,
        //         31);
        // manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE,
        //         ServerManager.ServerType.SEQUENTIAL);
        // randomQueries.assignQueries(manager);
        // manager.startAllServers(false);
        // randomQueries.outputStatistics("Latency for sequential shard storage with (5000 x 31 shard accesses).",
        //         "31");


        // randomQueries = new QueryGenerator(50, SECONDS_PER_ACCESS,
        // AVG_QUERIES_PER_SECOND);
        // manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE,
        // NUM_CORES_PER_MACHINE,
        // ServerManager.ServerType.SEQUENTIAL);
        // randomQueries.assignQueries(manager);
        // manager.startAllServers(false);
        // randomQueries.outputStatistics("Latency for sequential shard storage 50.",
        // "Sequential 50");

        // Generate round-robin servers
        // manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE,
        // NUM_CORES_PER_MACHINE,
        // ServerManager.ServerType.ROUND_ROBIN);
        // randomQueries.assignQueries(manager);
        // manager.startAllServers(false);
        // randomQueries.outputStatistics("Latency for round-robin shard storage with
        // (50,000 x 100 shard accesses).",
        // "Round-robin Storage");
    }

    // TESTCASES

    // 1) Simultaneous accesses on single-core machine should be sequential
    public static void serialQueries() throws Exception {
        Vector<Query> allQueries = new Vector<Query>();
        System.out.println("\nSTARTING TESTCASE 1\n----------------------");
        ServerManager manager = new ServerManager(1, /* numShards */ 5, /* numCores */ 1,
                ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 1f);
        newQuery.assignShards(1, 2, 3);
        allQueries.add(newQuery);

        newQuery = new Query(0, manager, 1f);
        newQuery.assignShards(4, 5);
        allQueries.add(newQuery);

        manager.startAllServers();
        QueryGenerator.outputStatistics("serialQueries", "Test Case 1", allQueries);
    }

    // 2) Simultaneous accesses with different lengths on single-core machine should
    // be sequential
    public static void serialQueriesVariableDuration() throws Exception {
        Vector<Query> allQueries = new Vector<Query>();
        System.out.println("\nSTARTING TESTCASE 2\n----------------------");
        ServerManager manager = new ServerManager(1, 5, 1, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with
                                                                                                 // 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 2, 3);
        allQueries.add(newQuery);

        newQuery = new Query(0, manager, 1.5f);
        newQuery.assignShards(3, 4, 5);
        allQueries.add(newQuery);

        manager.startAllServers();
        QueryGenerator.outputStatistics("serialQueriesVariableDuration", "Test Case 2", allQueries);
    }

    // 3) Simultaneous accesses possible if number of cores >= number of access
    public static void parallelQueriesSingleMachine() throws Exception {
        System.out.println("\nSTARTING TESTCASE 3\n----------------------");
        ServerManager manager = new ServerManager(1, 5, 5, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with
                                                                                                 // 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 2, 3, 4, 5);
        manager.startAllServers();
    }

    // 4) Simultaneous accesses not possible if number of cores < number of access
    public static void semiParallelQueriesSingleMachine() throws Exception {
        System.out.println("\nSTARTING TESTCASE 4\n----------------------");
        ServerManager manager = new ServerManager(1, 5, 5, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with
                                                                                                 // 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 2, 3, 4, 5, 5);
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