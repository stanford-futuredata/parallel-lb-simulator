#!/usr/bin/env python3
from collections import Counter
import seaborn as sns
import matplotlib.pyplot as plt

sns.set_theme()


NUM_SERVERS = 5
SHARDS_PER_SERVER = 20
QUERY_SIZE = 3


found_random = False

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


random_2_cluster_x = [] # Number of clusters
random_2_cluster_y = [] # P99 latencies seen


random_3_cluster_x = [] # Number of clusters
random_3_cluster_y = [] # P99 latencies seen

with open('scatterPlot.txt') as f:
	for l in f:
		line = l.strip()

		if "Generated random load balanced config:" in line:
			found_random = True
		elif found_random:
			server_shards = line.split("\t")
			random_cluster_counts = Counter ()
			for i in server_shards:
				cur_server = []
				for j in (i.split(" ")):
					if j != "":
						cur_server.append(int(j))
				random_cluster_counts += count_all_clusters(cur_server)

			found_random = False
		elif "Avg Random p99:" in line:
			components = line.strip().split(" ")
			p99 = int(components[components.index("p99:") + 1])
			random_2_cluster_x.append(random_cluster_counts.get(2, 0))
			random_2_cluster_y.append(p99)

			random_3_cluster_x.append(random_cluster_counts.get(3, 0))
			random_3_cluster_y.append(p99)

plt.scatter(random_2_cluster_x, random_2_cluster_y)

plt.show()
