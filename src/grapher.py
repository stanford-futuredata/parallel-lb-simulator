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
currentPdfCount = 3

normalizedGraph = plt.figure(1)
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
				normalisedX = finalArray / max(currentGraphData)

				plt.figure(1)
				sns.ecdfplot(normalisedX, label=seriesName)

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


				graphTitle = line.strip()
				print("Reading data for", graphTitle)
				seriesName = None
				currentGraphData = []
				currentMachineData = {}

		else:
			value, machineId = line.strip().split(" ")
			currentGraphData.append(float(value))
			if machineId not in currentMachineData:
				currentMachineData[machineId] = []
			currentMachineData[machineId].append(float(value))

# Graph final set of data
finalArray = np.array(currentGraphData)
normalisedX = finalArray / max(currentGraphData)

plt.figure(1)
sns.ecdfplot(normalisedX, label=seriesName)
plt.figure(2)
sns.ecdfplot(currentGraphData, label=seriesName)
plt.figure(currentPdfCount)
currentPdfCount += 1
sns.histplot(currentGraphData)
plt.suptitle("PDF: " + graphTitle)
plt.xlabel("Latency (unit time)")
plt.ylabel("Proportion")

print("99th Percentile of", graphTitle, ":", np.percentile(finalArray, 99))
print("99.9th Percentile of", graphTitle, ":", np.percentile(finalArray, 99.9))


plt.figure(currentPdfCount)
currentPdfCount += 1

allData = [currentMachineData[i] for i in currentMachineData]
labels = [i for i in currentMachineData]
plt.hist(allData, label=labels)
plt.suptitle("Machine Load: " + graphTitle)
plt.legend(loc='upper right')

graphTitle = None
seriesName = None
currentGraphData = []


plt.figure(1)
plt.suptitle("Normalised latency")
plt.xlabel("Latency (unit time)")
plt.ylabel("Proportion")
plt.legend()

plt.figure(2)
plt.suptitle("Latency")
plt.xlabel("Latency (unit time)")
plt.ylabel("Count")
plt.legend()


plt.show()