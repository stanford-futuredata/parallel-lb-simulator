//*******************************************************************
//  QueryGenerator.java
//
// Generates random queries given a set of constants (see constructor).
// Assumes the total number of shards is constant between different 
// server managers. It is possible to reuse the same QueryGenerator
// object across multiple server managers with the assignQueries()
// function. 
//
// USAGE:
// Create object and call generateRandomQueries(). Then assign these 
// queries to a server manager by using assignQueries(manager).
// Finally, you can call manager.startAllServers()
//*******************************************************************

import java.io.IOException;
import java.util.Vector;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;


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

    // Output results on all queries in current class once completed
    // Should only be run after manager.startAllServers() has finished
    public void outputStatistics(String graphTitle, String seriesName) throws IOException {
        Vector<PlotData> allLatencies = new Vector<PlotData>();
        for (Query i : allQueries) {
            i.populateLatencyVector(allLatencies);
        }

        // Commented out shard-load data
        // System.out.println("---------------------------------");
        // System.out.println("Shard load for " + seriesName);
        // System.out.println("---------------------------------");

        System.out.println("Finished running " + allQueries.size() + " queries. Displaying results for "
                + allLatencies.size() + " shard accesses.");

        // Output shard loads
        // System.out.println("Number of accesses per shard: ");
        // for (int i = 0; i < shard_load.length; i++) {
        //     System.out.println("Shard " + i + ") " + shard_load[i]);
        // }
        // System.out.println();

        PlotData.displayGraph(graphTitle, seriesName, allLatencies);
    }

    // Output shard load for a specific machine
    // Should only be run after manager.startAllServers() has terminated
    public void outputShardLoadForMachine(int serverId) {
        System.out.println("----------------------------");
        System.out.println("Shard latency for machine " + serverId);
        System.out.println("----------------------------");
        // Use a tree map since low number of keys and sorted output is easier to understand
        TreeMap<Integer, Double> totalShardLatency = new TreeMap<Integer, Double>();
        for (Query query : allQueries) {
            for (ShardAccess shard : query.shardAccessesForServer(serverId)) {
                // Set total latency in hash map
                int shardId = shard.getShardId();
                Double currentLatency = totalShardLatency.getOrDefault(shardId, 0.0); 
                totalShardLatency.put(shardId, currentLatency + shard.getLatency());
            }
        }

        for (Integer shard : totalShardLatency.keySet()) {
            System.out.println("Shard " + shard + ") " + totalShardLatency.get(shard));
        }
        System.out.println();
    }

    // Output statistics for a vector of queries
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
