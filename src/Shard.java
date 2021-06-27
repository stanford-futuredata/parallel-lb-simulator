//*******************************************************************
//  Shard.java
//
// Manages computation and shard accesses for a single shard. Stores
// all pending shard accesses in a deque for quick inserts/pops.
//*******************************************************************

import java.util.Deque;  
import java.util.ArrayDeque;  
import java.util.Optional;

public class Shard {
    public final int shardId;
    private Boolean isBusy = false;
    private Optional<ShardAccess> currentProcess;
    private Deque<ShardAccess> requests;  

    public Shard(int id) {
        shardId = id;
        currentProcess = Optional.empty();
        requests = new ArrayDeque<ShardAccess>();
    }

    public Boolean getIsBusy() {
        return isBusy;
    }

    public void runShard() {
        if (requests.size() == 0) {
            throw new RuntimeException("Error! No processes left to run");
        }
        currentProcess = Optional.of(requests.pop());
        isBusy = true;
    }

    public void terminateShard() {
        currentProcess = Optional.empty();
        isBusy = false;
    }

    public ShardAccess peekRequests() {
        if (requests.size() == 0) {
            throw new RuntimeException("Error! Peek from empty shard queue.");
        } 
        return requests.peekFirst();
    }

    public Optional<ShardAccess> getCurrentAccess() {
        return currentProcess;
    }

    public int numRemainingRequests() {
        return requests.size();
    }

    // IMPORTANT: assumes any enqueued ShardAccess has a start time >=
    //            all previous ShardAccesses enqueued
    public void enqueueAccess(ShardAccess access) {
        requests.addLast(access);
    }

    // Output all relevant info to a shard
    public String toString() {
        String outputStr = "Shard " + shardId + ":\n";
        outputStr +=  "\tIs Busy: " + getIsBusy() + "\n";
        
        if (currentProcess.isEmpty()) {
            outputStr += "\tCurrent Task: nil\n";
        } else {
            outputStr += "\tCurrent Task: " + currentProcess.toString() + "\n";
        }

        outputStr += "\tEnqueued processes: ";
        if (requests.size() == 0) {
            outputStr += "nil";
        } else {
            int counter = 1;
            for (ShardAccess request : requests) {
                outputStr += "\n";
                outputStr += "\t\t" + counter++ + ") " + request.toString();
            }
        }
        return outputStr + "\n";
    }
}
