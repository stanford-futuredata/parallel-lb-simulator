//*******************************************************************
//  Server.java
//
// Manages computation for a single server and runs simulation.
// Algorithm for running simulation on a single machine:
//     1) Enqueue all shard accesses to relevant shards (see Shard.java)
//     2) For every core on machine, run the soonest query in all free shards
//     3) While there are shard accesses left to process:
//          a) Find shard access with soonest end date from busy shards
//          b) Mark shard as free and set current time as this task's 
//             end time
//          c) Find shard access with soonest start-time from all 
//             free shards
//          d) Assign shard access to shard and mark shard as busy
//*******************************************************************

import java.lang.Math;  
import java.util.HashMap;  
import java.util.Optional;

public class Server {

    public final int MACHINE_ID;
    private final int NUM_CORES;
    private HashMap<Integer, Shard> idToShard;
    private double prevEndTime = 0; // double to store end time of previous task

    public Server(int id, int cores) {
        MACHINE_ID = id;
        NUM_CORES = cores;
        idToShard  = new HashMap<Integer, Shard>();
    }

    public void insertShard(Shard shard) {
        idToShard.put(shard.shardId, shard);
    }

    public Shard getShard(int shardId) {
        if (!idToShard.containsKey(shardId)) {
            throw new RuntimeException("Error! Shard not found");
        } 
        return idToShard.get(shardId);
    }

    public Shard getShard(ShardAccess access) {
        return getShard(access.getShardId());
    }

    public int getNumShards() {
        return idToShard.size();
    }
    
    private Optional<ShardAccess> findNextProcessToRun() {
        // Find soonest query on a free shard 
        Optional<ShardAccess> nextTask = Optional.empty();
        for (Shard shard : idToShard.values()) {
            if (!shard.getIsBusy() && shard.numRemainingRequests() > 0) {
                ShardAccess currentAccess = shard.peekRequests();
                if (nextTask.isEmpty() || nextTask.get().start() > currentAccess.start()) {
                    nextTask = Optional.of(currentAccess);
                }
            }   
        }

        return nextTask;
    }

    private Optional<ShardAccess> findNextProcessToTerminate() {
        // Find soonest query on occupied shard to stop
        
        // TODO: throw assert to see num cores == num shards

        Optional<ShardAccess> nextTask = Optional.empty();
        for (Shard shard : idToShard.values()) {
            if (shard.getIsBusy()) {
                ShardAccess currentAccess = shard.getCurrentAccess().get(); // Know optional is not empty since shard is busy
                if (nextTask.isEmpty() || nextTask.get().end() > currentAccess.end()) { // TODO: clean up
                    nextTask = Optional.of(currentAccess);
                }
            }   
        }

        return nextTask;
    }

    public Boolean runNextProcess() {
        // TODO: can we avoid unecessary computation?
        Optional<ShardAccess> nextAccess = findNextProcessToRun();
        
        if (!nextAccess.isEmpty()) {
            ShardAccess nextAccessUnwrapped = nextAccess.get();
            nextAccessUnwrapped.setActualStartTime(Math.max(prevEndTime, nextAccessUnwrapped.start()));
            getShard(nextAccessUnwrapped.getShardId()).runShard(); 
            return true;
        }
        return false;
    }

    public void spawnAllProcesses(Boolean output) {
        // Spawn appropriate number of queries to start
        for (int i = 0; i < NUM_CORES; i++) {
            runNextProcess();
        }

        Optional<ShardAccess> finishedTask;
        while ((finishedTask = findNextProcessToTerminate()).isPresent()) {
            ShardAccess finishedTaskUnwrapped = finishedTask.get();
            finishedTaskUnwrapped.markComplete();

            if (output) System.out.println("Finished task: " + finishedTaskUnwrapped);

            prevEndTime = finishedTaskUnwrapped.end();
            getShard(finishedTaskUnwrapped.getShardId()).terminateShard();
            runNextProcess();
        }

    }

    public String toString() {
        String outputStr = "Machine " + MACHINE_ID + ":\n";
        for (Integer id : idToShard.keySet()) {
            outputStr += "\t" + idToShard.get(id).toString() + "\n";
        }
        return outputStr;
    }
}
