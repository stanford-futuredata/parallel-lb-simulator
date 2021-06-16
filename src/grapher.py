#!/usr/bin/env python3

import numpy as np
import matplotlib.pyplot as plt

# Mark where graph titles begin
DATA_DELIMITER = "\t"
NUM_BINS = 1000
graphTitle = None
seriesName = None
currentGraphData = []

with open('OUTPUT.txt') as f:
	for line in f:
		if line[0] == DATA_DELIMITER:
			if not graphTitle:
				graphTitle = line.strip()
			elif not seriesName:
				seriesName = line.strip()
			else:
				count, bins_count = np.histogram(currentGraphData, bins=NUM_BINS)
				pdf = count / sum(count)
				cdf = np.cumsum(pdf)
				plt.plot(bins_count[1:], cdf, label=seriesName)

				graphTitle = line.strip()
				seriesName = None
				currentGraphData = []

		else:
			currentGraphData.append(float(line.strip()))

# Graph final set of data
count, bins_count = np.histogram(currentGraphData, bins=NUM_BINS)
pdf = count / sum(count)
cdf = np.cumsum(pdf)
plt.plot(bins_count[1:], cdf, label=seriesName)

graphTitle = None
seriesName = None
currentGraphData = []

plt.legend()
plt.ylim(ymin=0)
plt.xlim(xmin=0)
plt.show()