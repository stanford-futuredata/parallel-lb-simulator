package edu.stanford.futuredata;

import java.util.HashSet;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.Collection;

public class Query {

    private HashSet<ShardAccess> shardAccesses;

    public Query(int startShard, int endShard) {

        this.shardAccesses = new HashSet<ShardAccess>();

        int currentShard = startShard;

        while (currentShard != endShard) {
            shardAccesses.add(new ShardAccess(currentShard));

            currentShard += 1;

            if (currentShard == App.NUM_MACHINES * App.NUM_SHARDS_PER_MACHINE) {
                currentShard = 0;
            }
        }
    }

    public void setStartTick() {
        for (ShardAccess access : shardAccesses) {
            access.setStartTick();
        }
    }

    public void assignToServers(HashMap<Integer, WeightedRandomBag<Server>> shardToServer) {
        for (ShardAccess access : shardAccesses) {
            int assignedShard = access.getAssignedShard();

            if (!shardToServer.containsKey(assignedShard)) {
                throw new RuntimeException("Error! Can't find shard in servers map! Shard is " + assignedShard);
            }

            shardToServer.get(assignedShard).getRandom().addAccess(access);
        }
    }   

    public int getLatency() {
        int maxLatencySeen = -1;
        for (ShardAccess access : shardAccesses) {
            maxLatencySeen = Math.max(maxLatencySeen, access.getLatency());
        }

        return maxLatencySeen;
    }

    public void reset() {
        for (ShardAccess access : shardAccesses) {
            access.reset();
        }
    }

    public String toString() {
        String returnVal = "(";
        for (ShardAccess access : shardAccesses) {
            returnVal += "Shard" + access.getAssignedShard() + ",";
        }
        returnVal += ") latency " + getLatency();
        return returnVal;
    }
}
