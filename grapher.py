#!/usr/bin/env python3

import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt
sns.set_theme()

currentTick = 100

random_throughput = []
random_99th = []

rr_throughput = []
rr_99th = []

with open('differentTicks.txt') as f:
	for line in f:
		if line.startswith("Avg Random Throughput"):
			components = line.strip().split(":")
			random_throughput.append(float(components[1]) * 50)
			random_99th.append(float(components[-1]))

		if line.startswith("Avg LB Throughput"):
			components = line.strip().split(":")
			rr_throughput.append(float(components[1]) * 50)
			rr_99th.append(float(components[-1]))

# random_utilisation = all_utilisations[::2]
# rr_utilisation = all_utilisations[1::2]
# sns.scatterplot(x = x_axis, y = diff_99)
# plt.suptitle("Percentage Difference vs Ticks")
# plt.legend(loc='upper right')
# plt.xlabel("Average Query Rate")
# plt.ylabel("Percentage Difference")

# plt.figure(2)

plt.suptitle("Average Machine Throughput vs 99th Latency")
sns.scatterplot(x = rr_throughput, y = rr_99th, label = "Round-Robin Sharding")
sns.scatterplot(x = random_throughput, y = random_99th, label = "Random Sharding")
plt.legend(loc='upper right')
plt.yscale('log')
plt.xlabel("Throughput")
plt.ylabel("p99 Latency (μs)")

plt.show()
