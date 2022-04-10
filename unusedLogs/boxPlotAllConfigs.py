#!/usr/bin/env python3
from collections import Counter
import seaborn as sns
import matplotlib.pyplot as plt

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


two_cluster_latencies = [] 


three_cluster_x = [] # Number of clusters
three_cluster_y = [] # P99 latencies seen

test = {}

with open('allSimulationConfigs.txt') as f:
	for l in f:
		line = l.strip()

		if "config:" in line:
			found_experiment = True
		elif found_experiment:
			server_shards = line.split("\t")
			cluster_counts = Counter ()
			for i in server_shards:
				cur_server = []
				for j in (i.split(" ")):
					if j != "":
						cur_server.append(int(j))
				cluster_counts += count_all_clusters(cur_server)

			found_experiment = False
		elif "p99:" in line:
			components = line.strip().split(" ")
			p99 = int(components[components.index("p99:") + 1])

			num_2_clusters = cluster_counts.get(2, 0)
			num_3_clusters = cluster_counts.get(3, 0)

			test[num_3_clusters] = test.get(num_3_clusters, []) + [p99]

			three_cluster_x.append(cluster_counts.get(3, 0))
			three_cluster_y.append(p99)

x = [(i, test[i]) for i in test]
x.sort()

fig = plt.figure(figsize =(10, 12))
ax = fig.add_subplot(111)

for item in ([ax.title, ax.xaxis.label, ax.yaxis.label] +
             ax.get_xticklabels() + ax.get_yticklabels()):
    item.set_fontsize(20)
 
# Creating axes instance
bp = ax.boxplot([i[1] for i in x], showfliers=False, whis = (10, 90))
 
# x-axis labels
ax.set_xticklabels([i[0] for i in x])
plt.ylabel("p99 Latency (μs)")
plt.xlabel("Cluster Size")
plt.title("Simulated Graph: Number of 3-clusters vs p99 latency")

plt.show()
