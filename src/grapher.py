#!/usr/bin/env python3

'''
grapher.py

Reads in the OUTPUT.txt file and graphs results.
Primarily scratch-work for data visualisation so file is rather messy...
'''

import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt

sns.set_theme()

# Mark where graph titles begin
DATA_DELIMITER = "\t"
NUM_BINS = 1000
graphTitle = None
seriesName = None
currentGraphData = []
currentMachineData = {}

# Variables for storing shard load
allShardAccesses = {}
allShardLatencies = {}
shardsForMachine = {}


# COunt for dynamically generated figures
currentPdfCount = 3

shardLoadGraph = plt.figure(1)
standardGraph = plt.figure(2)


with open('OUTPUT.txt') as f:
	for line in f:
		if line[0] == DATA_DELIMITER:
			if not graphTitle:
				graphTitle = line.strip()
				print("Reading data for", graphTitle)
			elif not seriesName:
				seriesName = line.strip()
			else:
				finalArray = np.array(currentGraphData)

				plt.figure(2)
				sns.ecdfplot(currentGraphData, label=seriesName)

				plt.figure(currentPdfCount)
				currentPdfCount += 1
				sns.histplot(currentGraphData)
				plt.suptitle("PDF: " + graphTitle)

				plt.figure(currentPdfCount)
				currentPdfCount += 1

				print("50th Percentile of", graphTitle, ":", np.percentile(finalArray, 50))
				print("99th Percentile of", graphTitle, ":", np.percentile(finalArray, 99))
				print("99.9th Percentile of", graphTitle, ":", np.percentile(finalArray, 99.9))

				allData = [currentMachineData[i] for i in currentMachineData]
				labels = [i for i in currentMachineData]
				plt.hist(allData, label=labels)
				plt.suptitle("Machine Latency: " + graphTitle)
				plt.legend(loc='upper right')
				plt.xlabel("Latency (unit time)")
				plt.ylabel("Count")

				# Graph total shard latency for current configuration
				plt.figure(currentPdfCount)
				currentPdfCount += 1
				x_axis = []
				y_axis = []
				for i in range(0, max(allShardLatencies) + 1):
					x_axis.append(i)
					if i in allShardLatencies:
						y_axis.append(allShardLatencies[i])
					else:
						y_axis.append(0)

				sns.lineplot(x = x_axis, y = y_axis)
				plt.ylim(ymin = 0)
				plt.suptitle("Total Shard Latency for " + seriesName)
				plt.xlabel("Shard ID")
				plt.ylabel("Total Shard Latency (unit time)")

				graphTitle = line.strip()
				print("Reading data for", graphTitle)
				seriesName = None
				currentGraphData = []
				currentMachineData = {}
				allShardAccesses = {}
				allShardLatencies = {}

		else:
			latency, machineId, shardId = line.strip().split(" ")
			shardId = int(shardId)
			latency = float(latency)
			currentGraphData.append(latency)
			if machineId not in currentMachineData:
				currentMachineData[machineId] = []
				shardsForMachine[machineId] = set()

			shardsForMachine[machineId].add(shardId)
			allShardAccesses[shardId] = allShardAccesses.get(shardId, 0) + 1
			allShardLatencies[shardId] = allShardLatencies.get(shardId, 0) + latency
			currentMachineData[machineId].append(latency)

# Graph final set of data
finalArray = np.array(currentGraphData)

# Graph shard accesses per shard
plt.figure(1)
x_axis = []
y_axis = []

for i in range(0, max(allShardAccesses) + 1):
	x_axis.append(i)
	if i in allShardAccesses:
		y_axis.append(allShardAccesses[i])
	else:
		y_axis.append(0)

sns.lineplot(x = x_axis, y = y_axis)
plt.ylim(ymin = 0)
plt.suptitle("Number of requests per shard")
plt.xlabel("Shard ID")
plt.ylabel("Number of shard accesses")

# Graph total shard latency for current configuration
plt.figure(currentPdfCount)
currentPdfCount += 1
x_axis = []
y_axis = []
for i in range(0, max(allShardLatencies) + 1):
	x_axis.append(i)
	if i in allShardLatencies:
		y_axis.append(allShardLatencies[i])
	else:
		y_axis.append(0)

sns.lineplot(x = x_axis, y = y_axis)
plt.ylim(ymin = 0)
plt.suptitle("Total Shard Latency for " + seriesName)
plt.xlabel("Shard ID")
plt.ylabel("Total Shard Latency (unit time)")


# Graph latency for each configuration
plt.figure(2)
sns.ecdfplot(currentGraphData, label=seriesName)

plt.figure(currentPdfCount)
currentPdfCount += 1
sns.histplot(currentGraphData)
plt.suptitle("PDF: " + graphTitle)
plt.xlabel("Latency (unit time)")
plt.ylabel("Proportion")

# Print percentiles
print("50th Percentile of", graphTitle, ":", np.percentile(finalArray, 50))
print("99th Percentile of", graphTitle, ":", np.percentile(finalArray, 99))
print("99.9th Percentile of", graphTitle, ":", np.percentile(finalArray, 99.9))


# Print final machine load fgraph
plt.figure(currentPdfCount)
currentPdfCount += 1

allData = [currentMachineData[i] for i in currentMachineData]
labels = [i for i in currentMachineData]
plt.hist(allData, label=labels)
plt.suptitle("Machine Load: " + graphTitle)
plt.legend(loc='upper right')
plt.xlabel("Latency (unit time)")
plt.ylabel("Count")

plt.figure(2)
plt.suptitle("Latency")
plt.xlabel("Latency (unit time)")
plt.ylabel("Count")
plt.legend()

plt.show(block = False)

print("\nQuery machines for random")
while True:
	machineId = input("Enter a machine id: ")
	print(sorted(shardsForMachine.get(machineId, None)))