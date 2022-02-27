#!/usr/bin/env python3

with open('randomBoxPlot.txt') as f:
	for line in f:
		components = line.strip().split(" ")
		if "p99:" in components:
			p99 = int(components[components.index("p99:") + 1][:-2])
			size = int(components[components.index("Size:") + 1])
			trialNo = int(components[components.index("No:") + 1])


			if size < 5:
				print(line.strip())



