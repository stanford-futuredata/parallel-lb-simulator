#!/usr/bin/env python3
from collections import Counter
import seaborn as sns
import matplotlib.pyplot as plt
import numpy as np
import scipy
from statistics import mean


sns.set_theme()


NUM_SERVERS = 5
SHARDS_PER_SERVER = 20
QUERY_SIZE = 3

found_experiment = False

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


two_cluster_latencies = {}
three_cluster_latencies = {}


# Get number of two clusters from random shard placements
num_random_two_clusters = []
with open('randomSolrPlacements.txt') as f:
	for l in f:
		line = l.strip()

		server_shards = line.split("\t")
		cluster_counts = Counter ()
		for i in server_shards:
			cur_server = []
			for j in (i.split(" ")):
				if j != "":
					cur_server.append(int(j))
			cluster_counts += count_all_clusters(cur_server)
		num_random_two_clusters.append(cluster_counts.get(2, 0))

# num_random_two_clusters = num_random_two_clusters[::-1]

# Get p99s for random shard placements
p99_random_two_clusters = []
with open('randomSolrResults.txt') as f:
    for l in f:
        line = l.strip()

        components = line.strip().split(" ")
        if "p99:" in components:
            p99 = int(components[components.index("p99:") + 1][:-2])
            rate = int(components[components.index("Rate:") + 1])
            trialNo = int(components[components.index("No:") + 1])

            if trialNo > 0:
                p99_random_two_clusters.append(p99)

# Get p99s for parallel shard placements
with open('parallelBoxPlot.txt') as f:
    for l in f:
        line = l.strip()

        components = line.strip().split(" ")
        if "p99:" in components:
            p99 = int(components[components.index("p99:") + 1][:-2])
            rate = int(components[components.index("Rate:") + 1])
            trialNo = int(components[components.index("No:") + 1])

            p99_random_two_clusters.append(p99)

            # Only append every second time, because random results have two experiments per cluster setup
            if trialNo % 2 == 0:
                num_random_two_clusters.append(0)

# Read results for parallelism-maximising results (only at throughput of 3000)

num_entries_per_cluster = int(len(p99_random_two_clusters) / len(num_random_two_clusters))

for i in range(len(p99_random_two_clusters)):
    two_cluster_latencies[num_random_two_clusters[int(i / num_entries_per_cluster)]] = two_cluster_latencies.get(num_random_two_clusters[int(i / num_entries_per_cluster)], []) + [p99_random_two_clusters[i]]

to_delete = set([i for i in two_cluster_latencies if len(two_cluster_latencies[i]) < 40])
for i in to_delete:
    del two_cluster_latencies[i]
    print(i)

# two_cluster_latencies = [(i, j) for i in two_cluster_latencies for j in two_cluster_latencies[i] ]
two_cluster_latencies = [(i, mean(two_cluster_latencies[i])) for i in two_cluster_latencies]
two_cluster_latencies.sort()

fig = plt.figure(figsize =(15, 15))

ax = fig.add_subplot(111)

for item in ([ax.title, ax.xaxis.label, ax.yaxis.label] +
             ax.get_xticklabels() + ax.get_yticklabels()):
    item.set_fontsize(27)

x = [i[0] for i in two_cluster_latencies]
y = [i[1] for i in two_cluster_latencies]
ax.scatter(x, y, marker='x', s = 100)

slope, intercept, r_value, p_value, std_err = scipy.stats.linregress([i[0] for i in two_cluster_latencies], [i[1] for i in two_cluster_latencies])
print(slope, intercept, r_value, p_value, std_err)

plt.ylabel("p99 Latency (μs)")
plt.xlabel("Percentage of Queries that Hit a 2-Cluster")

plt.show()
fig.savefig('solr2ClusterVsLatency.pdf', format='pdf')


# TODO:
# - keep current experiment running
# - will look into the shift of most likely cluster size 
# - see how slope is affected by the two outliers
# - see which queries are the slowest ones
#     - look at trends e.g if youre a 2 cluster youre x percent more likely 
# at any tick in simlator, record queue size of a server, and do analysis of this and view p99 of queue size is higher with random allocation

# graphs:
# - number of queries hitting server, queue size, latency