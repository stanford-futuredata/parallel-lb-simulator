// Simulator for a distributed database.
// Compares the performance of NLB placements
// against PODP placements. Reports final latencies
// in ticks.
package edu.stanford.futuredata;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.*;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.Collections;

public class App {
    public static int NUM_MACHINES = 5;                 // Number of servers in cluster
    public static int NUM_CORES_PER_MACHINE = 4;        // Number of cores per server
    public static int NUM_SHARDS_PER_MACHINE = 20;      // Number of shards assigned to each machine
    public static int TICKS_PER_ACCESS = 10;             // How many ticks required for each shard access to be complete
    public static int QUERY_SIZE = 3;                   // Number of shards accessed by every query
    public static int SPLIT_FACTOR = 1;                 // Number of partitions for the load balancer

    public static double AVERAGE_QUERY_RATE = 0.5;      // Average rate of queries (queries/ticks)
    public static int NUM_TRIALS = 500000;              // Number of queries per simulation
    public static int NUM_SIMULATIONS = 100;            // How many different system setups to generate
 
    public static int TICK = 0;

    private static List<Entry<Query, Long>> generatedQueries;
    private static HashMap<Server, Integer> numShardsPerServer;

    public static void runQueries(HashMap<Integer, WeightedRandomBag<Server>> shardIdToServer) {
        Boolean existsPendingAccess = true;
        int currentQueryIndex = 0;

        while (existsPendingAccess || currentQueryIndex < generatedQueries.size()) {
            existsPendingAccess = false;
            TICK += 1;

            while (currentQueryIndex < generatedQueries.size() && generatedQueries.get(currentQueryIndex).getValue() <= TICK) {
                Query currentQuery = generatedQueries.get(currentQueryIndex).getKey();
                currentQueryIndex += 1;
                currentQuery.setStartTick();
                currentQuery.assignToServers(shardIdToServer);
            }

            for (Server server : numShardsPerServer.keySet()) {
                server.processOneTick(false);

                if (server.getRemainingAccesses() > 0) {
                    existsPendingAccess = true;
                }
            }

            if (!existsPendingAccess && currentQueryIndex < generatedQueries.size()) {
                TICK = (int) (generatedQueries.get(currentQueryIndex).getValue() - 1);
            }
        }
    }

    // Outputs latencies for queries 
    // Returns the p50, p99, p999 percentiles in a list
    public static List<Integer> outputResults(Boolean output, HashMap<Server, Integer> serverAssignments) {
        List<Integer> latencies = new ArrayList<Integer>();
        for (Entry<Query, Long> entry : generatedQueries) {
            latencies.add(entry.getKey().getLatency());
        }

        Collections.sort(latencies);
        List<Integer> percentiles = new ArrayList<Integer>();
        if (output) {
            System.out.println("50th Percentile: " + latencies.get((int) ((double) latencies.size() / 100 * 50)));
            System.out.println("99th Percentile: " + latencies.get((int) ((double) latencies.size() / 100 * 99)));
            System.out.println("99.9th Percentile: " + latencies.get((int) ((double) latencies.size() / 100 * 99.9)));

            System.out.println("Server utilisation (busy / total): ");
            double minUtilisation = 10000;
            double maxUtilisation = -1000;
            double avgUtilisation = 0;
            for (Server server : serverAssignments.keySet()) {
                minUtilisation = Math.min(minUtilisation, ((double) server.getBusyTime() / TICK) * 100);
                maxUtilisation = Math.max(maxUtilisation, ((double) server.getBusyTime() / TICK) * 100);
                avgUtilisation += ((double) server.getBusyTime() / TICK) * 100;
            }

            avgUtilisation /= serverAssignments.size();
            System.out.println("Minimum server utilisation: " + minUtilisation + "%");
            System.out.println("Average server utilisation: " + avgUtilisation + "%");
            System.out.println("Maximum server utilisation: " + maxUtilisation + "%");
        }

        percentiles.add(latencies.get((int) ((double) latencies.size() / 100 * 50)));
        percentiles.add(latencies.get((int) ((double) latencies.size() / 100 * 99)));
        percentiles.add(latencies.get((int) ((double) latencies.size() / 100 * 99.9)));

        return percentiles;
    }

    // Resets all queries to original state so that they can be used for additional simulations
    // Assumes that all servers have exhausted their access queues
    public static void resetState(HashMap<Integer, WeightedRandomBag<Server>> shardIdToServer) {
        shardIdToServer.clear();
        numShardsPerServer.clear();
        TICK = 0;

        for (Entry<Query, Long> entry : generatedQueries) {
            entry.getKey().reset();
        }
    }

    public static void main(String[] args) throws Exception {

        int numShards = NUM_MACHINES * NUM_SHARDS_PER_MACHINE;

        System.out.println("Num_machines: " + NUM_MACHINES + " Query_size: " + QUERY_SIZE + " Query_rate: " + AVERAGE_QUERY_RATE);

        // -------------------------------------
        // Specify hotspots for query workload.
        // Default query workload is uniform.
        // -------------------------------------
        HashMap<Integer, Integer> additionalQueryLoad = new HashMap<Integer, Integer>();

        if (false) { // Top 5% of shards get 5x more queries
            for (int startShard = 0; startShard < numShards / 20; startShard++) {
                additionalQueryLoad.put(startShard, 5);
            }
        }

        if (false) { // Add a spike for the 10% shards
            for (int startShard = 0; startShard < (numShards / 10); startShard++) {
                additionalQueryLoad.put(startShard, 2);   
            }
        }

        if (false) { // Top and bottom 2% of shards get 10x more queries
            for (int startShard = 0; startShard < (numShards / 100) * 2; startShard++) {
                additionalQueryLoad.put(startShard, 10);   
            }

            for (int startShard = numShards - 1; startShard >= numShards - (numShards / 100) * 2; startShard--) {
                additionalQueryLoad.put(startShard, 10);   
            }
        }

        if (false) { // Make 5 random shards very hot (20x more load)
            for (int i = 0; i < 5; i++) {
                additionalQueryLoad.put(ThreadLocalRandom.current().nextInt(0, numShards), 20);   
            }
        }

        if (false) { // every shard has a random load between 1 and 20
            for (int i = 0; i < numShards; i++) {
                additionalQueryLoad.put(i, ThreadLocalRandom.current().nextInt(1, 5));   
            }
        }

        if (false) { // Zipfian (first 20% of shards get 80% of load)
            for (int i = 0; i < 20; i++) {
                additionalQueryLoad.put(i, 16);   
            }
        }

        if (false) { // first 5 queries get 10x more accesses
            for (int startShard = 0; startShard < 5; startShard += 1) {
                additionalQueryLoad.put(startShard, 10);
            }
        }

        // Generate map of query frequencies and shard loads
        int[] shardLoads = new int[numShards];
        int[] shardMemoryUsages = new int[numShards];
        Map<Set<Integer>, Integer> sampleQueries = new HashMap<Set<Integer>, Integer>();

        for (int startShard = 0; startShard < numShards; startShard++) {

            int numHits = 1;
            if (additionalQueryLoad.containsKey(startShard)) {
                numHits = additionalQueryLoad.get(startShard);
            }

            Set<Integer> accessedShards = new HashSet<Integer>();
            for (int i = 0; i < QUERY_SIZE; i++) {
                int shardNum = (startShard + i) % numShards;
                accessedShards.add(shardNum);
                shardLoads[shardNum] = 1;
            }
            sampleQueries.put(accessedShards, numHits);
        }

        // Assign memory per shard
        for (int shardNum = 0; shardNum < numShards; shardNum++) {
            shardMemoryUsages[shardNum] = 1;
        }

        for (int simulationNum = 0; simulationNum < NUM_SIMULATIONS; simulationNum++) {
            // -------------------- 
            // Generate queries
            // --------------------
            double currentTime = 0;
            generatedQueries = new ArrayList<Entry<Query, Long>>();

            for (int trialNum = 0; trialNum < NUM_TRIALS; trialNum++) {
                int shardRangeStart = ThreadLocalRandom.current().nextInt(0, NUM_SHARDS_PER_MACHINE * NUM_MACHINES);
                int shardRangeEnd = ThreadLocalRandom.current().nextInt(0, NUM_SHARDS_PER_MACHINE * NUM_MACHINES);

                if (QUERY_SIZE != -1) {
                    shardRangeEnd = (shardRangeStart + QUERY_SIZE) % (NUM_SHARDS_PER_MACHINE * NUM_MACHINES);
                }

                Query query = new Query(shardRangeStart, shardRangeEnd);

                Entry<Query, Long> queryWithDelay = Map.entry(query, (long) currentTime);
                generatedQueries.add(queryWithDelay);
                currentTime += Math.log(1 - Math.random()) / (-AVERAGE_QUERY_RATE);
            }

            // Generate additional queries to add additional load
            for (int shardRangeStart : additionalQueryLoad.keySet()) {
                currentTime = 0;

                int shardRangeEnd = (shardRangeStart + QUERY_SIZE) % (NUM_SHARDS_PER_MACHINE * NUM_MACHINES);
                for (int i = 0; i < (additionalQueryLoad.get(shardRangeStart) - 1) * (NUM_TRIALS / numShards); i++) {
                    currentTime += Math.log(1 - Math.random()) / (-AVERAGE_QUERY_RATE);

                    Query query = new Query(shardRangeStart, shardRangeEnd);
                    Entry<Query, Long> queryWithDelay = Map.entry(query, (long) currentTime);
                    generatedQueries.add(queryWithDelay);
                }
            }
            generatedQueries.sort(Comparator.comparing(Entry::getValue));

            // -------------------------------------
            // Generate NLB data placement
            // -------------------------------------
            HashMap<Integer, WeightedRandomBag<Server>> shardIdToServer = new HashMap<Integer, WeightedRandomBag<Server>>();

            List<Integer> remainingServers = new ArrayList<>();
            HashMap<Integer, Server> serversByIndex = new HashMap<Integer, Server>();
            numShardsPerServer = new HashMap<Server, Integer>();

            // Generate new servers
            for (int i = 0; i < NUM_MACHINES; i++) {
                remainingServers.add(i);

                Server server = new Server(i);
                serversByIndex.put(i, server);
                numShardsPerServer.put(server, 0);
            }

            int[][] currentLocations = new int[NUM_MACHINES][numShards];

            // Place shards to servers randomly initially
            // The load balancer takes in this initial random placement and balances it
            for (int currentShardId = 0; currentShardId < NUM_MACHINES * NUM_SHARDS_PER_MACHINE; currentShardId++) {
                int serverArrayPosition = ThreadLocalRandom.current().nextInt(0, remainingServers.size());
                int serverId = remainingServers.get(serverArrayPosition);
                Server server = serversByIndex.get(serverId);

                numShardsPerServer.put(server, numShardsPerServer.get(server) + 1);
                currentLocations[serverId][currentShardId] = 1;

                if (numShardsPerServer.get(server) >= NUM_SHARDS_PER_MACHINE) {
                    remainingServers.remove(serverArrayPosition);
                }
            }       

            // Balance load of randomly-initialized shards
            List<double[]> randomCalculatedAssignments = new LoadBalancer().balanceLoad(numShards, 
                NUM_MACHINES, shardLoads, shardMemoryUsages, currentLocations, Collections.emptyMap(), 
                NUM_SHARDS_PER_MACHINE, SPLIT_FACTOR);

            System.out.println("Generated NLB placement: ");
            for (double[] serverShards : randomCalculatedAssignments) {
                int randomShardId = 0;

                for (double shardPercentage : serverShards) {
                    if (shardPercentage > 0.001) {
                        System.out.print(randomShardId + " ");
                    }
                    randomShardId += 1;
                }
                System.out.print("\t");
            }
            System.out.println();

            int serverId = 0;
            for (double[] x : randomCalculatedAssignments) {
                int shardId = 0;
                for (double probability : x) {
                    Server assignedServer = serversByIndex.get(serverId);

                    if (!shardIdToServer.containsKey(shardId)) {
                        WeightedRandomBag<Server> possibleServers = new WeightedRandomBag<>();
                        possibleServers.addEntry(assignedServer, probability);
                        shardIdToServer.put(shardId, possibleServers);
                    } else {
                        shardIdToServer.get(shardId).addEntry(assignedServer, probability);
                    }
                    shardId += 1;
                }
                serverId += 1;
            }

            runQueries(shardIdToServer);
            List<Integer> nlbPercentiles = outputResults(false, numShardsPerServer);
            System.out.println("NLB p50: " + nlbPercentiles.get(0));
            System.out.println("NLB p99: " + nlbPercentiles.get(1));
            System.out.println("Random p999: " + nlbPercentiles.get(2));
            Integer busyTime = 0;
            for (Integer id : serversByIndex.keySet()) {
                Server server = serversByIndex.get(id);
                busyTime += server.getBusyTime();
            }
            resetState(shardIdToServer);

            // -------------------------------------
            // Generate PODP shard placement
            // -------------------------------------
            List<double[]> calculatedAssignments = new LoadBalancer().balanceLoad(numShards, NUM_MACHINES, 
                shardLoads, shardMemoryUsages, new int[NUM_MACHINES][numShards], sampleQueries, 
                NUM_SHARDS_PER_MACHINE, SPLIT_FACTOR);

            HashMap<Integer, Server> serverIdToServer = new HashMap<Integer, Server>();

            for (int i = 0; i < NUM_MACHINES; i++) {
                Server server = new Server(i);
                serverIdToServer.put(i, server);
            }

            System.out.println("Generated PODP: ");
            serverId = 0;
            for (double[] currentServerAssignments : calculatedAssignments) {
                int shardId = 0;
                for (double probability : currentServerAssignments) {
                    if (probability > 0.01) { // To avoid rounding errors
                        Server assignedServer = serverIdToServer.get(serverId);
                        System.out.print(shardId + " ");

                        // Increment number of assigned shards for server
                        if (numShardsPerServer.containsKey(assignedServer)) {
                            numShardsPerServer.put(assignedServer, numShardsPerServer.get(assignedServer) + 1);
                        } else {
                            numShardsPerServer.put(assignedServer, 1);
                        }

                        // Add shard to server random bag
                        if (!shardIdToServer.containsKey(shardId)) {
                            WeightedRandomBag<Server> possibleServers = new WeightedRandomBag<>();
                            possibleServers.addEntry(assignedServer, probability);
                            shardIdToServer.put(shardId, possibleServers);
                        } else {
                            shardIdToServer.get(shardId).addEntry(assignedServer, probability);
                        }

                    }
                    shardId += 1;
                }
                System.out.print("\t");
                serverId += 1;
            }
            System.out.println();
            runQueries(shardIdToServer);
            List<Integer> podpPercentiles = outputResults(false, numShardsPerServer);
            System.out.println("PODP p50: " + podpPercentiles.get(0));
            System.out.println("PODP p99: " + podpPercentiles.get(1));
            System.out.println("PODP p999: " + podpPercentiles.get(2));
            resetState(shardIdToServer);
        }
    }
}


