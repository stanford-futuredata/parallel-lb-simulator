import java.io.IOException;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.ThreadLocalRandom;

// Store queries to be enqueued on multiple different server managers
// Assumes that all server managers have the same number of shards and machines and cores
// USAGE:
// Create object and call generateRandomQueries(). Then assign these queries to a server manager by using assignQueries(manager)
// Finally, you can call manager.startAllServers()
public class QueryGenerator {
    // Constants for random query generation
    public final double SHARD_ACCESS_TIME;
    // When generating queries, use exponential distribution to model time between new queries arriving
    public final double AVERAGE_QUERY_RATE;
    private final int NUM_QUERIES;

    // Variables to keep track of final statistics
    private Vector<Query> allQueries = new Vector<Query>();
    private int[] shard_load = new int[App.NUM_MACHINES * App.NUM_SHARDS_PER_MACHINE + 1];

    public QueryGenerator(int queries, double accessTime, double queryRate) {
        NUM_QUERIES = queries;
        SHARD_ACCESS_TIME = accessTime;
        AVERAGE_QUERY_RATE = queryRate;
        generateRandomQueries();
    }

    public void generateRandomQueries() {
        double startTime = 0;
        for (int i = 0; i < NUM_QUERIES; i++) {
            // Generate query and a random range of shards to be accessed
            Query newQuery = new Query(startTime, SHARD_ACCESS_TIME);

            int shardRangeStart = ThreadLocalRandom.current().nextInt(1, App.NUM_SHARDS_PER_MACHINE * App.NUM_MACHINES + 1);
            int shardRangeEnd   = ThreadLocalRandom.current().nextInt(shardRangeStart, App.NUM_SHARDS_PER_MACHINE * App.NUM_MACHINES + 1);
            Vector<Integer> accessedShards = new Vector<Integer>();
            for (int j = shardRangeStart; j <= shardRangeEnd; j++) {
                accessedShards.add(j);
                shard_load[j] += 1;
            }
            newQuery.storeShards(accessedShards);
            allQueries.add(newQuery);

            // Increment start time by using exponential distribution
            double startTimeOffset = Math.log(1 - Math.random()) / (-AVERAGE_QUERY_RATE);
            startTime += startTimeOffset;
        }
    }

    public void assignQueries(ServerManager manager) {
        for (Query i : allQueries) {
            i.setManager(manager);
        }
    }

    // Output results on all queries once completed
    // Should only be run after manager.startAllServers() has finished
    public void outputStatistics(String graphTitle, String seriesName) throws IOException {
        Vector<Double> allLatencies = new Vector<Double>();
        for (Query i : allQueries) {
            i.populateLatencyVector(allLatencies);
        }

        // Sort latencies to access 99 and 99.9 latencies
        Collections.sort(allLatencies);

        System.out.println("RESULTS\n---------------------------------");
        System.out.println("Finished running " + allQueries.size() + " queries. Displaying results for " + allLatencies.size() + " shard accesses.");

        // Index of latency percentiles
        int latency_99th = (int) Math.floor(allLatencies.size() * 0.99);
        int latency_999th = (int) Math.floor(allLatencies.size() * 0.999);

        System.out.println("99th percentile for latency: " + allLatencies.get(latency_99th) + " (index = " + latency_99th + ")");
        System.out.println("99.9th percentile for latency: " + allLatencies.get(latency_999th) + " (index = " + latency_999th + ")");

        // Output shard loads
        System.out.println("Number of accesses per shard: ");
        for (int i = 0; i < shard_load.length; i++) {
            System.out.println("Shard " + (i + 1) + ") " + shard_load[i]);
        }

        PlotData.displayGraph(graphTitle, seriesName, allLatencies);
    }

    public static void outputStatistics(String graphTitle, String seriesName, Vector<Query> allQueries) throws IOException {
        Vector<Double> allLatencies = new Vector<Double>();

        for (Query i : allQueries) {
            i.populateLatencyVector(allLatencies);
        }

        // Sort latencies to access 99 and 99.9 latencies
        Collections.sort(allLatencies);

        PlotData.displayGraph(graphTitle, seriesName, allLatencies);
    }

    public String toString() {
        String outputStr = "";
        for (Query i : allQueries) {
            outputStr += i.toString() + "\n";
        }
        return outputStr;
    }
}
