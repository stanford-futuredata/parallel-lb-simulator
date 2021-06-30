#!/usr/bin/env python3

'''
grapher.py

Reads in the OUTPUT.txt file and graphs results.
Primarily scratch-work for data visualisation so file is rather messy...
'''

import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt

stats = None 
round_robin = None 
random = None
numMachines = None
all_vals = []



with open("src/testing.txt", "r") as f:
	for line in f:
		line = line.strip()
		
		if line == "":
			all_vals.append((1 - round_robin / random, numMachines))
			stats = None 
			round_robin = None 
			random = None
			numMachines = None

		tokens = line.split(" ")
		if tokens[0] == "Round-Robin":
			round_robin = float(tokens[1])
		elif tokens[0] == "Random":
			random = float(tokens[1])
		elif tokens[0] != "":
			stats = line.split(", ")
			numMachines = stats[0]

x_axis = [int(i[1]) for i in all_vals]
y_axis = [float(i[0]) for i in all_vals]

sns.scatterplot(x = x_axis, y = y_axis)
plt.xlabel("Number of Shards/Machine")
plt.ylabel("Percentage Reduction in Latency")
plt.suptitle("Number of Machines vs Percentage Latency Reduction (1000 shards/query, 2 cores/machine, 100 shards/machine)")
plt.ylim(ymin = 0)
plt.ylim(ymax = 1)



plt.show()