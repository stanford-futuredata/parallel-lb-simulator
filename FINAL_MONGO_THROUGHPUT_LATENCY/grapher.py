#!/usr/bin/env python3

import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt
from matplotlib.patches import Patch

sns.set_theme()

# COde for actual results
random = [[], [], [], [], [], [], [], []]
parallel = [[], [], [], [], [], [], [], []]
index = 0
with open('randomBoxPlot.txt') as f:
	for line in f:
		components = line.strip().split(" ")
		if "rate:" in components:
			rate = int(components[components.index("rate:") + 1])
			print(rate)
			if rate == 10:
				index = 0
			elif rate == 15:
				index = 1
			elif rate == 20:
				index = 2
			elif rate == 25:
				index = 3
			elif rate == 27:
				index = 4
			elif rate == 30:
				index = 5
			elif rate == 32:
				index = 6
			elif rate == 35:
				index = 7
		if "[SCAN" in components:
			p99 = components[26] #Found this by knowing index of p99
			
			if p99[:3] == "90=":
				p99 = int(p99[3:-1])
				random[index].append(p99)


with open('parallelBoxPlot.txt') as f:
	for line in f:
		components = line.strip().split(" ")
		if "rate:" in components:
			rate = int(components[components.index("rate:") + 1])

			if rate == 10:
				index = 0
			elif rate == 15:
				index = 1
			elif rate == 20:
				index = 2
			elif rate == 25:
				index = 3
			elif rate == 27:
				index = 4
			elif rate == 30:
				index = 5
			elif rate == 32:
				index = 6
			elif rate == 35:
				index = 7
		if "[SCAN" in components:
			p99 = components[26] #Found this by knowing index of p99
			
			if p99[:3] == "90=":
				p99 = int(p99[3:-1])
				parallel[index].append(p99)



colors = ['#FF4040', '#40A0FF']

THROUGHPUTS = ['8000 - 12000', '18,000 - 22,000', '28,000 - 32,000', '38,000 - 42,000']
legend_elements = [Patch(facecolor=colors[0], edgecolor='black', label='Naive Load Balanced Shard Placement'),
	                   Patch(facecolor=colors[1], edgecolor='black', label='Parallel-Aware Shard Placement')]

# for i in range(len(random)):


result = []

for i in range(4):
	result.append(random[i])
	result.append(parallel[i])

fig = plt.figure(figsize =(15, 15))

ax = fig.add_subplot(111)

for item in ([ax.title, ax.xaxis.label, ax.yaxis.label] +
             ax.get_xticklabels() + ax.get_yticklabels()):
    item.set_fontsize(27)
 
# Creating axes instance
bp = ax.boxplot(result, patch_artist = True, vert = 1, notch = False, showfliers=False, whis = (5, 95))
 
# x-axis labels
ax.set_xticklabels(["10", "10", "15", "15", "20", "20", "25", "25"])
plt.ylabel("p99 Latency (μs)")
plt.xlabel("Offered Throughput (Queries/Second)")
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


ax.legend(handles=legend_elements, loc='upper left', prop={'size': 20})
# ax.set_yscale('log')


# Adding title
# plt.title("Offered Throughput vs p99 Latency; Uniform shard load in Solr")
 
# Removing top axes and right axes
# ticks
ax.get_xaxis().tick_bottom()
ax.get_yaxis().tick_left()

plt.show()
fig.savefig('throughputVsLatencyMongo.pdf', format='pdf')


