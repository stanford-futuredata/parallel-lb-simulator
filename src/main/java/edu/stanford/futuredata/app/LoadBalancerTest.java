package edu.stanford.futuredata;

// run with java -Djava.library.path=/Users/nirvikbaruah/cplex121/cplex/bin/x86-64_osx -cp target/my-app-1.0-SNAPSHOT.jar:src/main/java/edu/stanford/futuredata/app/cplex.jar edu.stanford.futuredata.App 

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
        /**
         * Generate an assignment of shards to servers.
         * @param numShards  Number of shards.
         * @param numServers  Number of servers.
         * @param shardLoads Amount of load on each shard.
         * @param shardMemoryUsages Memory usage of each shard.
         * @param currentLocations Entry [x][y] is 1 if server x has a copy of shard y and 0 otherwise.
         * @param sampleQueries A map from sets of shards to the number of queries that touched precisely that set.
         * @param maxMemory  Maximum server memory.
         * @return Entry [x][y] is the percentage of queries for shard y that should be routed to server x.
         * @throws IloException
         */

        int numShards = 100;
        int numServers = 5;
        int maxMemory = numShards / numServers;
        int querySize = 3;

        int[] shardLoads = new int[numShards];
        int[] shardMemoryUsages = new int[numShards];

        for (int i = 0; i < numShards; i++) {
            shardLoads[i] = 1;
            shardMemoryUsages[i] = 1;
        }

        int[][] currentLocations = new int[numServers][numShards];

        // Assign relevant shards to each server for round-robin
        for (int i = 0; i < numServers; i++) {            
            for (int shardId = i; shardId < numShards; shardId += numServers) {
                currentLocations[i][shardId] = 1;
            }
        }

        // Assign shards to servers randomly
        // List<Integer> remainingServers = new ArrayList<>();
        // HashMap<Integer, Integer> numShardsPerServer = new HashMap<Integer, Integer>();

        // for (int i = 0; i < numServers; i++) {
        //     remainingServers.add(i);
        //     numShardsPerServer.put(i, 0);
        // }

        // for (int currentShardId = 0; currentShardId < numShards; currentShardId++) {
        //     int serverArrayPosition = ThreadLocalRandom.current().nextInt(0, remainingServers.size());
        //     int serverId = remainingServers.get(serverArrayPosition);

        //     numShardsPerServer.put(serverId, numShardsPerServer.get(serverId) + 1);
        //     currentLocations[serverId][currentShardId] = 1;

        //     if (numShardsPerServer.get(serverId) >= maxMemory) {
        //         remainingServers.remove(serverArrayPosition);
        //     }
        // }       


        // Populate sampleQueries
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


        // System.out.println("Non-parallel load balanced shards: ");
        // List<double[]> calculatedAssignments = new LoadBalancer().balanceLoad(numShards, numServers, shardLoads, shardMemoryUsages, currentLocations, Collections.emptyMap(), maxMemory);

        // int serverId = 0;

        // System.out.print("{");
        // for (double[] serverShards : calculatedAssignments) {
        //     int shardId = 0;
        //     if (serverId > 0) System.out.print(", ");

        //     System.out.print("{");
        //     boolean printedFirst = false;
        //     for (double shardPercentage : serverShards) {
        //         if (shardPercentage > 0.01) {
        //             if (!printedFirst) {
        //                 printedFirst = true;
        //                 System.out.print(shardId);
        //             } else {
        //                 System.out.print(", " + shardId);
        //             }
        //         }
        //         shardId += 1;
        //     }
        //     System.out.println("}");
        //     serverId += 1;
        // }
        // System.out.print("}");

        System.out.println("Parallel load balanced shards: ");
        for (int i = 0; i < 400; i++) {
            List<double[]> calculatedAssignmentsParallel = new LoadBalancer().balanceLoad(numShards, numServers, shardLoads, shardMemoryUsages, new int[numServers][numShards], sampleQueries, maxMemory);

            int serverId = 0;

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
            System.out.println();
        }
        // System.out.println("Num shards: " + numShards);
        // System.out.println("Num servers: " + numServers);
        // System.out.println("max memory: " + maxMemory);
        // System.out.println("shards loads: ");
        // for (int i : shardLoads) {
        //     System.out.println(i);
        // }
        // System.out.println("\nshards memories: ");
        // for (int i : shardMemoryUsages) {
        //     System.out.println(i);
        // }

        // System.out.println("\nshard locations: ");
        // int s = 0;
        // for (int[] i : currentLocations) {
        //     int shardId = 0;
        //     System.out.println("Server " + s + ": ");
        //     for (int j : i) {
        //         if (j > 0) {
        //             System.out.println(shardId);
        //         }
        //         shardId += 1;
        //     }
        //     s += 1;
        // }

        // System.out.println("\nsample queries: ");
        // for (Set<Integer> i : sampleQueries.keySet()) {
        //     System.out.println(i + ": " + sampleQueries.get(i));
        // }
    }
}

