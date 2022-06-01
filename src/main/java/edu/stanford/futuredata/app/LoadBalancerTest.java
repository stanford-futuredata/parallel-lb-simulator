// Program to run the load balancer in isolation
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

public class LoadBalancerTest {

    public static void main(String[] args) throws Exception {
        int numShards = 20;  
        int numServers = 5;
        int maxMemory = numShards / numServers;
        int querySize = 3;
        int SPLIT_FACTOR = 1;

        int[] shardLoads = new int[numShards];
        int[] shardMemoryUsages = new int[numShards];
        int[][] currentLocations = new int[numServers][numShards];

        for (int i = 0; i < numShards; i++) {
            shardLoads[i] = 1;
            shardMemoryUsages[i] = 1;
        }


        for (int i = 0; i < numShards; i++) {
            shardLoads[i] = 1;
            shardMemoryUsages[i] = 1;
        }

        // Randomly initialize shards
        List<Integer> remainingServers = new ArrayList<>();
        HashMap<Integer, Integer> numShardsPerServer = new HashMap<Integer, Integer>();

        for (int i = 0; i < numServers; i++) {
            remainingServers.add(i);
            numShardsPerServer.put(i, 0);
        }

        for (int currentShardId = 0; currentShardId < numShards; currentShardId++) {
            int serverArrayPosition = ThreadLocalRandom.current().nextInt(0, remainingServers.size());
            int serverId = remainingServers.get(serverArrayPosition);

            numShardsPerServer.put(serverId, numShardsPerServer.get(serverId) + 1);
            currentLocations[serverId][currentShardId] = 1;

            if (numShardsPerServer.get(serverId) >= maxMemory) {
                remainingServers.remove(serverArrayPosition);
            }
        }       


        // Adjust the query workload if necessary
        HashMap<Integer, Integer> additionalQueryLoad = new HashMap<Integer, Integer>();

        if (false) { // first 5 queries get 10x more accesses
            for (int startShard = 0; startShard < 5; startShard += 1) {
                additionalQueryLoad.put(startShard, 10);
            }
        }

        if (false) { // Zipfian (first 20% of shards get 80% of load)
            for (int i = 0; i < 20; i++) {
                additionalQueryLoad.put(i, 16);   
            }
        }

        Map<Set<Integer>, Integer> sampleQueries = new HashMap<Set<Integer>, Integer>();
        for (int startShard = 0; startShard < numShards; startShard++) {

            int numHits = 1;
            if (additionalQueryLoad.containsKey(startShard)) {
                numHits = additionalQueryLoad.get(startShard);
            }

            Set<Integer> accessedShards = new HashSet<Integer>();
            for (int i = 0; i < querySize; i++) {
                int shardNum = (startShard + i) % numShards;
                accessedShards.add(shardNum);
                shardLoads[shardNum] += numHits;
            }
            sampleQueries.put(accessedShards, numHits);
        }

        System.out.println("NLB shard placement: ");
        List<double[]> calculatedAssignments = new LoadBalancer().balanceLoad(numShards, numServers, shardLoads, shardMemoryUsages, currentLocations, Collections.emptyMap(), maxMemory, SPLIT_FACTOR);

        int serverId = 0;

        for (double[] serverShards : calculatedAssignments) {
            int shardId = 0;

            for (double shardPercentage : serverShards) {
                if (shardPercentage > 0.01) {
                    System.out.print(shardId + " ");
                }
                shardId += 1;
            }
            System.out.print("  ");
            serverId += 1;
        }
        System.out.println();


        System.out.println("PODP shard placement: ");
        long startTime = System.nanoTime();
        List<double[]> calculatedAssignmentsParallel = new LoadBalancer().balanceLoad(numShards, numServers, shardLoads, shardMemoryUsages, new int[numServers][numShards], sampleQueries, maxMemory, SPLIT_FACTOR);

        serverId = 0;
        for (double[] serverShards : calculatedAssignmentsParallel) {
            int shardId = 0;

            for (double shardPercentage : serverShards) {
                if (shardPercentage > 0.001) {
                    System.out.print(shardId + " ");
                }
                shardId += 1;
            }
            System.out.print("\t");
            serverId += 1;
        }
    }
}

