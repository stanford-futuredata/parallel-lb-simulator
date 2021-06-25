#!/usr/bin/env python3

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
allShardData = {}

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

				print("99th Percentile of", graphTitle, ":", np.percentile(finalArray, 99))
				print("99.9th Percentile of", graphTitle, ":", np.percentile(finalArray, 99.9))

				allData = [currentMachineData[i] for i in currentMachineData]
				labels = [i for i in currentMachineData]
				plt.hist(allData, label=labels)
				plt.suptitle("Machine Latency: " + graphTitle)
				plt.legend(loc='upper right')
				plt.xlabel("Latency (unit time)")
				plt.ylabel("Count")


				graphTitle = line.strip()
				print("Reading data for", graphTitle)
				seriesName = None
				currentGraphData = []
				currentMachineData = {}

		else:
			value, machineId, shardId = line.strip().split(" ")
			shardId = int(shardId)
			currentGraphData.append(float(value))
			if machineId not in currentMachineData:
				currentMachineData[machineId] = []

			allShardData[shardId] = allShardData.get(shardId, 0) + 1
			currentMachineData[machineId].append(float(value))

# Graph final set of data
finalArray = np.array(currentGraphData)

# Graph shard load per shard
plt.figure(1)
x_axis = []
y_axis = []

for i in range(0, max(allShardData) + 1):
	x_axis.append(i)
	if i in allShardData:
		y_axis.append(allShardData[i])
	else:
		y_axis.append(0)

sns.lineplot(x = x_axis, y = y_axis)
plt.suptitle("Shard Load")
plt.xlabel("Shard ID")
plt.ylabel("Number of shard accesses")

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


plt.show()