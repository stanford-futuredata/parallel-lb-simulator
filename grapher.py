#!/usr/bin/env python3

import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt
from matplotlib.patches import Patch

sns.set_theme()


# Code for sim results
# random = [[], [], [], []]
# parallel = [[], [], [], []]
# with open('SimulatorBoxplot1.txt') as f:
# 	for line in f:
# 		components = line.strip().split(" ")
# 		if "Random" in components and "p99:" in components:
# 			p99 = int(components[components.index("p99:") + 1])
# 			random[0].append(p99)
# 		elif "LB" in components and "p99:" in components:
# 			p99 = int(components[components.index("p99:") + 1])
# 			parallel[0].append(p99)

# with open('SimulatorBoxplot2.txt') as f:
# 	for line in f:
# 		components = line.strip().split(" ")
# 		if "Random" in components and "p99:" in components:
# 			p99 = int(components[components.index("p99:") + 1])
# 			random[1].append(p99)
# 		elif "LB" in components and "p99:" in components:
# 			p99 = int(components[components.index("p99:") + 1])
# 			parallel[1].append(p99)

# with open('SimulatorBoxplot3.txt') as f:
# 	for line in f:
# 		components = line.strip().split(" ")
# 		if "Random" in components and "p99:" in components:
# 			p99 = int(components[components.index("p99:") + 1])
# 			random[2].append(p99)
# 		elif "LB" in components and "p99:" in components:
# 			p99 = int(components[components.index("p99:") + 1])
# 			parallel[2].append(p99)

# with open('SimulatorBoxplot4.txt') as f:
# 	for line in f:
# 		components = line.strip().split(" ")
# 		if "Random" in components and "p99:" in components:
# 			p99 = int(components[components.index("p99:") + 1])
# 			random[3].append(p99)
# 		elif "LB" in components and "p99:" in components:
# 			p99 = int(components[components.index("p99:") + 1])
# 			parallel[3].append(p99)


# COde for actual results
random = [[], [], [], [], []]
parallel = [[], [], [], [], []]
with open('randomQuerySweep.txt') as f:
	for line in f:
		components = line.strip().split(" ")
		if "p99:" in components:
			p99 = int(components[components.index("p99:") + 1][:-2])
			rate = int(components[components.index("Rate:") + 1])
			size = int(components[components.index("Size:") + 1])
			trialNo = int(components[components.index("No:") + 1])

			if size == 2 and trialNo > 0:
				random[0].append(p99)
			elif size == 3:
				random[1].append(p99)
			elif size == 5:
				random[2].append(p99)
			elif size == 7:
				random[3].append(p99)
			elif size == 10:
				random[4].append(p99)


with open('parallelQuerySweep.txt') as f:
	for line in f:
		components = line.strip().split(" ")
		if "p99:" in components:
			p99 = int(components[components.index("p99:") + 1][:-2])
			rate = int(components[components.index("Rate:") + 1])
			size = int(components[components.index("Size:") + 1])
			trialNo = int(components[components.index("No:") + 1])

			if size == 2:
				parallel[0].append(p99)
			elif size == 3:
				parallel[1].append(p99)
			elif size == 5:
				parallel[2].append(p99)
			elif size == 7:
				parallel[3].append(p99)
			elif size == 10:
				parallel[4].append(p99)


colors = ['#FF4040', '#40A0FF']

THROUGHPUTS = ['8000 - 12000', '18,000 - 22,000', '28,000 - 32,000', '38,000 - 42,000']
legend_elements = [Patch(facecolor=colors[0], edgecolor='black', label='Random'),
	                   Patch(facecolor=colors[1], edgecolor='black', label='Parallel-Aware')]

# for i in range(len(random)):


result = []

for i in range(len(random)):
	result.append(random[i])
	result.append(parallel[i])

fig = plt.figure(figsize =(10, 12))

ax = fig.add_subplot(111)

for item in ([ax.title, ax.xaxis.label, ax.yaxis.label] +
             ax.get_xticklabels() + ax.get_yticklabels()):
    item.set_fontsize(20)
 
# Creating axes instance
bp = ax.boxplot(result, patch_artist = True, vert = 1, notch = False, showfliers=False, whis = (5, 95))
 
# x-axis labels
ax.set_xticklabels(["2", "2", "3", "3", "5", "5", "7", "7", "10", "10"])
plt.ylabel("p99 Latency (μs)")
plt.xlabel("Query Size (Shards/Query)")
ax.set_ylim(ymin=0)

# TODO: do 100,000 trials and do p1 - p99
# parameter sweeps: 
	# fixed throughput w query length sweeped, (2 - 20 and expect shit for top end) (multiple experiments per query length exact same script but change query instaed fof throughput)
		# fix utilisation?
	# fixed throughput and query length and sweep shards/server (keep server constant but change num shards) (restart cluster with different shard size, 50, 100, 200, 250, 500, 1000)
	# graph of robustness to skew ()
	# scalability graph 
	# perform loads of trials with 100,000 random placemenets, print out best and worst ones and see if it fits with theory. 

index = 0

for patch in bp['boxes']:
    patch.set_facecolor(colors[index])
    if index == 0:
    	index = 1
    else:
    	index = 0

for median in bp['medians']:
    median.set(color ='black')


ax.legend(handles=legend_elements, loc='upper left')
# ax.set_yscale('log')


# Adding title
plt.title("Query Size vs p99 Latency; Uniform shard load in Solr")
 
# Removing top axes and right axes
# ticks
ax.get_xaxis().tick_bottom()
ax.get_yaxis().tick_left()

plt.show()

