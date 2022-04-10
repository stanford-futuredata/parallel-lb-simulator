#!/usr/bin/env python3
from collections import Counter
import seaborn as sns
import matplotlib.pyplot as plt
import scipy
from statistics import median, mean


sns.set_theme()

random_all_queries = []
parallel_all_queries = []
output_arr = random_all_queries

random_shard_placements = {}
randomPlacementsNext = False

parallel_shard_placements = {}
parallelPlacementsNext = False

# Get p99s for parallel shard placements
with open('testAllQueries.txt') as f:
    for l in f:
        line = l.strip()
        components = line.strip().split(" ")
        if "latency" in components:
            shardString = components[0].split(",")
            shards = []
            for i in shardString:
                if len(i) == 1:
                    break
                token = ""
                if (i[-3]).isdigit():
                    token = i[-3:]
                elif (i[-2]).isdigit():
                    token = i[-2:]
                else:
                    token = i[-1:]

                if len(parallel_shard_placements) > 0:
                    shards.append((token, parallel_shard_placements[token]))
                else:
                    shards.append((token, random_shard_placements[token]))

            latency = int(components[components.index("latency") + 1])

            # Only append every second time, because random results have two experiments per cluster setup
            output_arr.append((latency, shards))

        elif "Generated" in components and "random" in components:
            randomPlacementsNext = True 
        elif randomPlacementsNext:
            randomPlacementsNext = False
            # get random shard placements
            serverId = 0
            server_placements = l.strip().split("\t")
            for server_placement in server_placements:
                shards = server_placement.split(" ")
                for i in shards:
                    random_shard_placements[i] = serverId
                serverId += 1

        elif "parallel-maximising" in components:
            output_arr = parallel_all_queries
            parallelPlacementsNext = True
        elif parallelPlacementsNext:
            parallelPlacementsNext = False
            # get parallel shard placements
            serverId = 0
            server_placements = l.strip().split("\t")
            for server_placement in server_placements:
                shards = server_placement.split(" ")
                for i in shards:
                    parallel_shard_placements[i] = serverId
                serverId += 1

random_all_queries.sort()
parallel_all_queries.sort()

clustersSeen = {}
prevLatency = 0

for latency, shards in random_all_queries[0:]:
    seenServers = set()
    for shard, server in shards:
        seenServers.add(server)

    clusterSize = 4 - len(seenServers) 
    clustersSeen[clusterSize] = clustersSeen.get(clusterSize, []) + [latency]
    print("Latency is " + str(latency) + " for (shards, servers):", shards)

    prevLatency = latency


# print("Average cluster latency in random:")
# for i in clustersSeen:
#     print("Cluster size: " + str(i) + " Avg Latency: " + str(median(clustersSeen[i])))

for i in clustersSeen:
    print("Num " + str(i) + "-clusters: " + str(len(clustersSeen[i])))

# plt.scatter([i[0] for i in parallel_results], [i[1] for i in parallel_results])
# plt.show()



# the size of the pending shard access queue = latency
# parallel is better than random because it means that single servers dont suddenty get hit by a bunch of queries which make the queue larger 


#TODO: use notion for all experiments

# at any tick in simlator, record queue size of a server, and do analysis of this and view p99 of queue size is higher with random allocation

# graphs:
# - number of queries hitting server, queue size, latency





# TODO:
# - see which queries are the slowest ones
#     - look at trends e.g if youre a 2 cluster youre x percent more likely 