#!/usr/bin/env python3
from collections import Counter
import seaborn as sns
import matplotlib.pyplot as plt
import scipy
import numpy as np
from statistics import mean


sns.set_theme()

NUM_SERVERS = 5
SHARDS_PER_SERVER = 53
QUERY_SIZE = 3

random_results = {i: {} for i in range(5)}
parallel_results = {i: {} for i in range(5)}
output_arr = random_results

randomTwoCluster = {}
parallelTwoCluster = {}
countCluster = None 
randomTwoClusterEntries = {}
parallelTwoClusterEntries = {}

randomEntries = {}
parallelEntries = {}
curOutput = randomEntries
count = 15

randomLatencyCdf = []
parallelLatencyCdf = []
    
def count_all_clusters(shards):
    shards = sorted(shards)
    cluster_counts = Counter()
    for index, start_shard in enumerate(shards):
        current_distance = 1
        current_shard_index = index
        current_cluster_size = 1
        
        while current_distance <= QUERY_SIZE:
            cluster_counts[current_cluster_size] += 1
            
            prev_shard = shards[current_shard_index]
            
            current_shard_index += 1
            current_cluster_size += 1
            
            
            
            if current_shard_index >= len(shards):
                current_shard_index = 0
                current_distance += shards[0] + NUM_SERVERS * SHARDS_PER_SERVER - prev_shard
            else:
                current_distance += shards[current_shard_index] - prev_shard
                
    return cluster_counts

# Get p99s for parallel shard placements
with open('queueSizeLog.txt') as f:
    for l in f:
        line = l.strip()

        components = line.strip().split(" ")
        if "Shard" in components:
            shard = int(components[components.index("shard") + 1])
            latency = int(components[components.index("latency") + 1])
            server = int(components[components.index("Server") + 1])
            tick = int(components[components.index("tick") + 1])
            queueSize = int(components[components.index("size") + 1])
            curOutput[queueSize] = curOutput.get(queueSize, []) + [latency]

            if curOutput is randomEntries:
                randomLatencyCdf.append(latency)
                randomTwoClusterEntries[randomTwoCluster[server]] = randomTwoClusterEntries.get(randomTwoCluster[server], []) + [queueSize]
            else:
                parallelLatencyCdf.append(latency)
                parallelTwoClusterEntries[parallelTwoCluster[server]] = parallelTwoClusterEntries.get(parallelTwoCluster[server], []) + [queueSize]
        elif "random" in components:
            curOutput = randomEntries
            count -= 1
            randomTwoCluster = {}
            countCluster = randomTwoCluster
        elif "parallel-maximising" in components:
            curOutput = parallelEntries
            parallelTwoCluster = {}
            countCluster = parallelTwoCluster
        elif countCluster is not None:
            server_shards = line.split("\t")
            for index, i in enumerate(server_shards):
                cur_server = []
                for j in (i.split(" ")):
                    if j != "":
                        cur_server.append(int(j))
                countCluster[index] = count_all_clusters(cur_server).get(2, 0)

            countCluster = None

        if count == 0:
            break


fig = plt.figure(figsize =(15, 15))

ax = fig.add_subplot(111)

for item in ([ax.title, ax.xaxis.label, ax.yaxis.label] +
             ax.get_xticklabels() + ax.get_yticklabels()):
    item.set_fontsize(27)

# plt.ylabel("Shard Access Latency (μs)")
# plt.xlabel("Server Queue Size")
# ax.scatter([i for i in randomEntries], [mean(randomEntries[i]) for i in randomEntries], label = "Naive Load Balanced Shard Placement", marker='x', s = 100)
# ax.scatter([i for i in parallelEntries], [mean(parallelEntries[i]) for i in parallelEntries], label = "Parallelism-Maximizing Shard Placement", s = 100)
# plt.legend(loc='upper left', prop={'size': 20})
# plt.show()
# fig.savefig('queueSizevsLatency.pdf', format='pdf')

# CDF graphing code
# fig = plt.figure(figsize =(15, 15))

# ax = fig.add_subplot(111)

# for item in ([ax.title, ax.xaxis.label, ax.yaxis.label] +
#              ax.get_xticklabels() + ax.get_yticklabels()):
#     item.set_fontsize(27)

# plt.ylabel("Shard Access Latency (μs)")
# plt.xlabel("Server Queue Size")
# sns.kdeplot(data = randomLatencyCdf, cumulative = True, label = "Naive Load Balanced Shard Placement")
# sns.kdeplot(data = parallelLatencyCdf, cumulative = True, label = "Parallelism-Maximizing Shard Placement")
# plt.legend(loc='lower right', prop={'size': 20})
# plt.show()
# fig.savefig('queueSizevsLatencyCDF.pdf', format='pdf')

plt.xlabel("Number of 2-clusters on server")
plt.ylabel("Average queue size")
x = [i for i in randomTwoClusterEntries]
y = [mean(randomTwoClusterEntries[i]) for i in randomTwoClusterEntries]
ax.scatter(x, y, label = "Naive Load Balanced Shard Placement", marker='x', s = 100)
ax.scatter([0], [0], label = "Parallelism-Maximizing Shard Placement")
x.extend([0])
y.extend([0]) #TODO: use actual vals
plt.plot(np.unique(x), np.poly1d(np.polyfit(x, y, 1))(np.unique(x)))

plt.legend(loc='upper left', prop={'size': 20})
plt.show()
fig.savefig('queueSizevsLatency.pdf', format='pdf')

#TodoL change how average us cimputed; output queue size every tick to actually get the average queue size
# scaling experiment
    # double cluster size until taking ~1 hour, and then use partition size of 8 to compute with POP
    # start at like 32 servers and scale up (probs be in 1000s)