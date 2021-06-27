//*******************************************************************
//  ShardAccess.java
//
// Object to access a single shard on a query.
//*******************************************************************

public class ShardAccess {
    private int shardId;
    private double length;
    private double startTime;
    private double actualStartTime;
    private double endTime;
    private Boolean isComplete = false;

    public ShardAccess(double start, double duration, int assignedShard) {
        startTime = start;
        length = duration;
        shardId = assignedShard;
    }

    public void markComplete() {
        isComplete = true;
    }

    public void setActualStartTime(double time) {
        actualStartTime = time;
        endTime = actualStartTime + length;
    }

    // Getters
    public int getShardId() {
        return shardId;
    }

    public Boolean isComplete() {
        return isComplete;
    }

    public double start() {
        return startTime;
    }

    public double end() {
        return endTime;
    }

    public double getDuration() {
        return length;
    }

    public double getLatency() {
        if (!isComplete()) {
            return Double.MAX_VALUE;
        }
        return actualStartTime - startTime;
    }

    public String toString() {
        if (isComplete()) {
            return "Access for shard " + shardId + " at time " + startTime + " is complete at time " + endTime + " with latency " + getLatency() + ".";
        }
        return "Access for shard " + shardId + " at time " + startTime + " with length " + length + " is not complete.";
    }
}
