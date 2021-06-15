public class App {

    // Constants for server generation
    public static final int NUM_MACHINES = 4;
    public static final int NUM_CORES_PER_MACHINE = 10;
    public static final int NUM_SHARDS_PER_MACHINE = 10;

    // Constants for query generation
    public static final int NUM_QUERIES = 100000;
    public static final double SECONDS_PER_ACCESS = 5;
    public static final double AVG_QUERIES_PER_SECOND = 1;

    public static void main(String[] args) throws Exception {
        QueryGenerator randomQueries = new QueryGenerator(NUM_QUERIES, SECONDS_PER_ACCESS, AVG_QUERIES_PER_SECOND);

        // Generate sequential servers
        ServerManager manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE, ServerManager.ServerType.SEQUENTIAL);
        randomQueries.assignQueries(manager);
        manager.startAllServers(false);
        randomQueries.outputStatistics(
        "Latency for sequential shard storage.",
        "Latency for query length " + randomQueries.SHARD_ACCESS_TIME);

        // Generate round-robin servers
        manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE, ServerManager.ServerType.ROUND_ROBIN);
        randomQueries.assignQueries(manager);
        manager.startAllServers(false);
        randomQueries.outputStatistics(
                "Latency for round-robin shard storage.",
                "Latency for query length " + randomQueries.SHARD_ACCESS_TIME);
    }

    // TESTCASES

    // 1) Simultaneous accesses on single-core machine should be sequential
    public static void serialQueries() {
        System.out.println("\nSTARTING TESTCASE 1\n----------------------");
        ServerManager manager = new ServerManager(1, /* numShards */ 5, /* numCores */ 1, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 1f);
        newQuery.assignShards(1, 2, 3);

        newQuery = new Query(0, manager, 1f);
        newQuery.assignShards(3, 4, 5);

        manager.startAllServers();
    }

    // 2) Simultaneous accesses with different lengths on single-core machine should be sequential
    public static void serialQueriesVariableDuration() {
        System.out.println("\nSTARTING TESTCASE 2\n----------------------");
        ServerManager manager = new ServerManager(1,5,1, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 2, 3);

        newQuery = new Query(0, manager, 1.5f);
        newQuery.assignShards(3, 4, 5);

        manager.startAllServers();
    }

    // 3) Simultaneous accesses possible if number of cores >= number of access
    public static void parallelQueriesSingleMachine() {
        System.out.println("\nSTARTING TESTCASE 3\n----------------------");
        ServerManager manager = new ServerManager(1,5,5, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 2, 3, 4, 5);
        manager.startAllServers();
    }

    // 4) Simultaneous accesses not possible if number of cores < number of access
    public static void semiParallelQueriesSingleMachine() {
        System.out.println("\nSTARTING TESTCASE 4\n----------------------");
        ServerManager manager = new ServerManager(1,5,5, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 2, 3, 4, 5, 5);
        manager.startAllServers();
    }

    // 5) Parallel accesses possible on single-core machines over multiple machines
    public static void parallelQueriesMultipleMachines() {
        System.out.println("\nSTARTING TESTCASE 5\n----------------------");
        ServerManager manager = new ServerManager(4,4,1, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 5, 9, 13);
        manager.startAllServers();
    }

    // 6) Accesses are asynchronous
    public static void parallelQueriesAsync() {
        System.out.println("\nSTARTING TESTCASE 6\n----------------------");
        ServerManager manager = new ServerManager(1,4,2, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1);

        newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1);

        newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(2);

        manager.startAllServers();
    }
}