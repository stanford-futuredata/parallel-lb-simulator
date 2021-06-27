//*******************************************************************
//  Query.java
//
// Wrapper class for a single query. A query is defined as a consecutive
// range of shard accesses at a given start time. Object takes in which
// ServerManager query is assigned to so that it can be assigned to 
// the correct shard objects.
//*******************************************************************
import java.util.Vector;  

// Wrapper class for all shard access objects
public class Query {
    private double startTime;
    private double duration;
    private ShardAccess shards[]; // Data structure to store all shards needed for query
    private ServerManager manager;

    public Query(double start, double length) {
        startTime = start;
        shards = new ShardAccess[0];
        duration = length;
    }

    public Query(double start, ServerManager serverManager, double length) {
        startTime = start;
        manager = serverManager;
        shards = new ShardAccess[0];
        duration = length;
    }

    public void setManager(ServerManager newManager) {
        manager = newManager;
        assignShards(); // Assign shards to new server manager
    }

    // Get all shard accesses in query for a specific server
    public Vector<ShardAccess> shardAccessesForServer(int serverId) {
        Vector<ShardAccess> shardsForServer = new Vector<ShardAccess>();
        for (int i = 0; i < shards.length; i++) {
            if (manager.getServerForShard(shards[i]).MACHINE_ID == serverId) {
                shardsForServer.add(shards[i]);
            }
        }

        return shardsForServer;
    }

    // For storing output statistics in an input vector
    public void populateLatencyVector(Vector<PlotData> latencies) {
        for (ShardAccess i : shards) {
            PlotData newEntry = new PlotData(
                i.getLatency(), 
                manager.getServerForShard(i).MACHINE_ID, 
                i.getShardId()
            );
            latencies.add(newEntry);
        }
    }

    private void enqueueAccess(ShardAccess access) {
        Server accessedServer = manager.getServerForShard(access);
        Shard accessedShard = accessedServer.getShard(access);
        accessedShard.enqueueAccess(access);
    }

    public void assignShards() {
        for (int i = 0; i < shards.length; i++) {
            enqueueAccess(shards[i]);
        }
    }

    public void assignShards(int... allShardIds) { // Populate shards given any number of arguments
        shards = new ShardAccess[allShardIds.length];

        for (int i = 0; i < allShardIds.length; i++) {
            ShardAccess access = new ShardAccess(startTime, duration, allShardIds[i]);
            shards[i] = access;

            enqueueAccess(access);
        }
    }

    public void storeShards(Vector<Integer> allShardIds) {
        shards = new ShardAccess[allShardIds.size()];

        for (int i = 0; i < allShardIds.size(); i++) {
            ShardAccess access = new ShardAccess(startTime, duration, allShardIds.get(i));
            shards[i] = access;
        }
    }

    // Output info on all shard accesses
    public String toString() {
        String outputStr = "Query at time " + startTime + ": \n";
        int counter = 1;
        outputStr += "\tShard accesses: ";
        if (shards.length == 0) {
            outputStr += "None \n";
        } else {
            for (ShardAccess request : shards) {
                outputStr += "\n";
                outputStr += "\t\t" + counter++ + ") " + request.toString();
            }
        }
        return outputStr;
    }
}
