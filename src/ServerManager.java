import java.util.HashMap;  
import java.util.concurrent.ThreadLocalRandom;


public class ServerManager {

    public enum ServerType {
        SEQUENTIAL,
        ROUND_ROBIN,
        RANDOM
    }

    private final int NUM_MACHINES;
    private final int SHARDS_PER_MACHINE;
    private final int NUM_CORES_PER_MACHINE;
    private HashMap<Integer, Server> shardIdToServer;
    private Server servers[];

    public ServerManager(int numMachines, int numShards, int numCores, ServerType serverConfiguration) {
        NUM_MACHINES = numMachines;
        SHARDS_PER_MACHINE = numShards;
        NUM_CORES_PER_MACHINE = numCores;
        servers = new Server[NUM_MACHINES];

        // Generate all machines and shards depending on configuration
        if (serverConfiguration == ServerType.SEQUENTIAL) {
            assignShardsSequential();
        } else if (serverConfiguration == ServerType.ROUND_ROBIN) {
            assignShardsRoundRobin();
        } else if (serverConfiguration == ServerType.RANDOM) {
            assignShardsRandom();
        } else {
            throw new RuntimeException("Error! Unknown server configuration");
        }
    }

    // Assign shards sequentially i.e machine 1 has shards 1, 2, 3 etc.
    private void assignShardsSequential() {
        int currentShardId = 0;
        shardIdToServer = new HashMap<Integer, Server>();
        for (int i = 0; i < NUM_MACHINES; i++) {
            Server newMachine = new Server(i, NUM_CORES_PER_MACHINE);
            servers[i] = newMachine;
            for (int j = 0; j < SHARDS_PER_MACHINE; j++) {
                Shard newShard = new Shard(currentShardId);
                newMachine.insertShard(newShard);
                shardIdToServer.put(currentShardId, newMachine);
                currentShardId++;
            }
        }
    }

    // Assign shards round robin i.e shard 1 on machine 1, shard 2 on machine 2 etc.
    private void assignShardsRoundRobin() {
        shardIdToServer = new HashMap<Integer, Server>();

        for (int i = 0; i < NUM_MACHINES; i++) {
            Server newMachine = new Server(i, NUM_CORES_PER_MACHINE);
            servers[i] = newMachine;
        }

        for (int currentShardId = 0; currentShardId < NUM_MACHINES * SHARDS_PER_MACHINE; currentShardId++) {
            Shard newShard = new Shard(currentShardId);
            int serverIndex = currentShardId % NUM_MACHINES;
            servers[serverIndex].insertShard(newShard);
            shardIdToServer.put(currentShardId, servers[serverIndex]);
        }
    }

    private void assignShardsRandom() {
        shardIdToServer = new HashMap<Integer, Server>();

        for (int i = 0; i < NUM_MACHINES; i++) {
            Server newMachine = new Server(i, NUM_CORES_PER_MACHINE);
            servers[i] = newMachine;
        }

        for (int currentShardId = 0; currentShardId < NUM_MACHINES * SHARDS_PER_MACHINE; currentShardId++) {
            Shard newShard = new Shard(currentShardId);

            int serverIndex = ThreadLocalRandom.current().nextInt(0, NUM_MACHINES);
            while (servers[serverIndex].getNumShards() >= SHARDS_PER_MACHINE) {
                serverIndex = ThreadLocalRandom.current().nextInt(0, NUM_MACHINES); // TODO: make this more efficient
            }
            servers[serverIndex].insertShard(newShard);
            shardIdToServer.put(currentShardId, servers[serverIndex]);
        }
    }

    public Server getServerForShard(int shardId) {
        if (!shardIdToServer.containsKey(shardId)) {
            throw new RuntimeException("Error! Server not found for shard " + shardId);
        }
        return shardIdToServer.get(shardId);
    }

    // Start all servers with/without output
    public void startAllServers() {
        for (Server i : servers) {
            i.spawnAllProcesses(true);
        }
    }

    public void startAllServers(Boolean output) {
        for (Server i : servers) {
            i.spawnAllProcesses(output);
        }
    }

    public Server getServerForShard(ShardAccess access) {
        return getServerForShard(access.getShardId());
    }

    public String toString(int serverNum) {
        if (serverNum < servers.length) {
            return servers[serverNum].toString();
        }
        return "";
    }
    
    public String toString() {
        String outputStr = "";
        for (int i = 0; i < servers.length; i++) {
            outputStr += toString(i) + "\n";
        }
        return outputStr;
    }
}
