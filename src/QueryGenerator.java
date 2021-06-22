import java.io.IOException;
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
    // When generating queries, use exponential distribution to model time between
    // new queries arriving
    public final double AVERAGE_QUERY_RATE;
    private final int NUM_QUERIES;
    private final int NUM_SHARD_ACCESS_PER_QUERY;

    // Variables to keep track of final statistics
    private Vector<Query> allQueries = new Vector<Query>();
    private int[] shard_load = new int[App.NUM_MACHINES * App.NUM_SHARDS_PER_MACHINE];

    public QueryGenerator(int queries, double accessTime, double queryRate, Boolean wrapAround) {
        NUM_QUERIES = queries;
        SHARD_ACCESS_TIME = accessTime;
        AVERAGE_QUERY_RATE = queryRate;
        NUM_SHARD_ACCESS_PER_QUERY = -1;
        generateRandomQueries(wrapAround);
    }

    public QueryGenerator(int queries, double accessTime, double queryRate, int numShards, Boolean wrapAround) {
        NUM_QUERIES = queries;
        SHARD_ACCESS_TIME = accessTime;
        AVERAGE_QUERY_RATE = queryRate;
        NUM_SHARD_ACCESS_PER_QUERY = numShards;
        generateRandomQueries(wrapAround);
    }

    public void generateRandomQueries(Boolean wrapAround) {
        double startTime = 0;
        int numShards = App.NUM_SHARDS_PER_MACHINE * App.NUM_MACHINES;
        for (int i = 0; i < NUM_QUERIES; i++) {
            // Generate query and a random range of shards to be accessed
            Query newQuery = new Query(startTime, SHARD_ACCESS_TIME);

            int shardRangeStart;
            int shardRangeEnd;
            if (NUM_SHARD_ACCESS_PER_QUERY == -1) {
                shardRangeStart = ThreadLocalRandom.current().nextInt(0,
                numShards);
                
                if (wrapAround) {
                    shardRangeEnd = ThreadLocalRandom.current().nextInt(0,
                    numShards);
                } else {
                    shardRangeEnd = ThreadLocalRandom.current().nextInt(shardRangeStart,
                    numShards);
                }
            } else if (NUM_SHARD_ACCESS_PER_QUERY == numShards) { // Prevent errors when want all shards accessed
                shardRangeStart = 1;
                shardRangeEnd = numShards;
            } else {
                if (wrapAround) {
                    shardRangeStart = ThreadLocalRandom.current().nextInt(0,
                    numShards);
                } else {
                    shardRangeStart = ThreadLocalRandom.current().nextInt(0,
                    numShards + 1 - NUM_SHARD_ACCESS_PER_QUERY);
                }

                shardRangeEnd = (shardRangeStart + NUM_SHARD_ACCESS_PER_QUERY - 1) % numShards;
            }

            Vector<Integer> accessedShards = new Vector<Integer>();
            
            Boolean seenValue = false;
            int j = shardRangeStart;
            while (!seenValue) { // Since start and end is inclusive, need this implementation
                accessedShards.add(j);
                shard_load[j] += 1;

                if (j == shardRangeEnd) {
                    seenValue = true;
                }

                j = (j + 1) % numShards;
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
        Vector<PlotData> allLatencies = new Vector<PlotData>();
        for (Query i : allQueries) {
            i.populateLatencyVector(allLatencies);
        }

        System.out.println("RESULTS\n---------------------------------");
        System.out.println("Finished running " + allQueries.size() + " queries. Displaying results for "
                + allLatencies.size() + " shard accesses.");

        // Output shard loads
        System.out.println("Number of accesses per shard: ");
        for (int i = 0; i < shard_load.length; i++) {
            System.out.println("Shard " + i + ") " + shard_load[i]);
        }

        PlotData.displayGraph(graphTitle, seriesName, allLatencies);
    }

    public static void outputStatistics(String graphTitle, String seriesName, Vector<Query> allQueries)
            throws IOException {
        Vector<PlotData> allLatencies = new Vector<PlotData>();

        for (Query i : allQueries) {
            i.populateLatencyVector(allLatencies);
        }

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
