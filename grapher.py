#!/usr/bin/env python3

import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt
sns.set_theme()

currentTick = 100

random_99th = []

rr_99th = []




with open('zipfian5machines20shards.txt') as f:
	for line in f:
		if line.startswith("Avg LB 99th percentile latency"):
			components = line.strip().split(":")
			rr_99th.append(int(components[1]))

		if line.startswith("Avg Random 99th percentile latency"):
			components = line.strip().split(":")
			random_99th.append(float(components[1]))


counts, bin_edges = np.histogram([(random_99th[i] - rr_99th[i]) / random_99th[i] for i in range(len(random_99th))], bins=np.size(random_99th), normed=True)
plt.suptitle("CDF: 99th Percentile Latency Percentage Reductions Between Parallel and Non-Parallel shard placements for 5 hot shards")

# Now find the cdf
cdf = np.cumsum(counts*np.diff(bin_edges));
# And finally plot the cdf
plt.plot(bin_edges[1:], cdf, label = "Percentage Difference")

plt.xlabel("Percentage Difference")
plt.ylabel("Probability")

plt.show()

plt.figure(2)

plt.suptitle("CDF: 99th Percentile Latencies for 5 hot shards")
# sns.scatterplot(x = rr_throughput, y = rr_99th, label = "Round-Robin Sharding")
# sns.scatterplot(x = random_throughput, y = random_99th, label = "Random Sharding")
# Choose how many bins you want here
num_bins = 20

# Use the histogram function to bin the data
counts, bin_edges = np.histogram(random_99th, bins=np.size(random_99th), normed=True)
# Now find the cdf
cdf = np.cumsum(counts*np.diff(bin_edges));
# And finally plot the cdf
plt.plot(bin_edges[1:], cdf, label = "Random Load-Balanced Placement")

# # Use the histogram function to bin the data
counts, bin_edges = np.histogram(rr_99th, bins=np.size(rr_99th), normed=True)
# Now find the cdf
cdf = np.cumsum(counts*np.diff(bin_edges));
# And finally plot the cdf
plt.plot(bin_edges[1:], cdf, label = "Parallel-Aware Load-Balanced Placement")

plt.legend(loc='lower right')
plt.xlabel("p99 Latency (Ticks)")
plt.ylabel("Probability")

plt.show()

