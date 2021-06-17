import java.util.Vector;

public class App {

    // Constants for server generation
    public static final int NUM_MACHINES = 4;
    public static final int NUM_CORES_PER_MACHINE = 3;
    public static final int NUM_SHARDS_PER_MACHINE = 100;

    // Constants for query generation
    public static final int NUM_QUERIES = 50000;
    public static final double SECONDS_PER_ACCESS = 1;
    public static final double AVG_QUERIES_PER_SECOND = 0.5;

    public static void main(String[] args) throws Exception {
        QueryGenerator randomQueries = new QueryGenerator(NUM_QUERIES, SECONDS_PER_ACCESS, AVG_QUERIES_PER_SECOND);
        serialQueries();
        serialQueriesVariableDuration();

        // Generate sequential servers
        // ServerManager manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE, ServerManager.ServerType.SEQUENTIAL);
        // randomQueries.assignQueries(manager);
        // manager.startAllServers(false);
        // randomQueries.outputStatistics(
        // "Latency for sequential shard storage.",
        // "Sequential");

        // Generate round-robin servers
//        manager = new ServerManager(NUM_MACHINES, NUM_SHARDS_PER_MACHINE, NUM_CORES_PER_MACHINE, ServerManager.ServerType.ROUND_ROBIN);
//        randomQueries.assignQueries(manager);
//        manager.startAllServers(false);
//        randomQueries.outputStatistics(
//                "Latency for round-robin shard storage.",
//                "Latency for query length " + randomQueries.SHARD_ACCESS_TIME);
    }

    // TESTCASES

    // 1) Simultaneous accesses on single-core machine should be sequential
    public static void serialQueries() throws Exception {
        Vector<Query> allQueries = new Vector<Query>();
        System.out.println("\nSTARTING TESTCASE 1\n----------------------");
        ServerManager manager = new ServerManager(1, /* numShards */ 5, /* numCores */ 1, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 1f);
        newQuery.assignShards(1, 2, 3);
        allQueries.add(newQuery);

        newQuery = new Query(0, manager, 1f);
        newQuery.assignShards(4, 5);
        allQueries.add(newQuery);

        manager.startAllServers();
        QueryGenerator.outputStatistics("serialQueries", "Test Case 1", allQueries);
    }

    // 2) Simultaneous accesses with different lengths on single-core machine should be sequential
    public static void serialQueriesVariableDuration() throws Exception {
        Vector<Query> allQueries = new Vector<Query>();
        System.out.println("\nSTARTING TESTCASE 2\n----------------------");
        ServerManager manager = new ServerManager(1,5,1, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
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
        ServerManager manager = new ServerManager(1,5,5, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 2, 3, 4, 5);
        manager.startAllServers();
    }

    // 4) Simultaneous accesses not possible if number of cores < number of access
    public static void semiParallelQueriesSingleMachine() throws Exception {
        System.out.println("\nSTARTING TESTCASE 4\n----------------------");
        ServerManager manager = new ServerManager(1,5,5, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 2, 3, 4, 5, 5);
        manager.startAllServers();
    }

    // 5) Parallel accesses possible on single-core machines over multiple machines
    public static void parallelQueriesMultipleMachines() throws Exception {
        System.out.println("\nSTARTING TESTCASE 5\n----------------------");
        ServerManager manager = new ServerManager(4,4,1, ServerManager.ServerType.SEQUENTIAL); // Create 1 server with 5 shards and 1 core
        Query newQuery = new Query(0, manager, 2.5f);
        newQuery.assignShards(1, 5, 9, 13);
        manager.startAllServers();
    }

    // 6) Accesses are asynchronous
    public static void parallelQueriesAsync() throws Exception {
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