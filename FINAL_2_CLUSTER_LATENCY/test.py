#!/usr/bin/env python3

# stack = []
# numLines = 0
# with open('generatedPlacements20to35.txt') as f:
# 	for l in f:
# 		line = l.strip()
# 		stack.append(line)

# for i in stack[::-1]:
# 	print(i)



with open('allSimulationConfigs.txt') as f:
	for l in f:
		line = l.strip()

		if "config:" in line and "random" in line:
			found_experiment = True
		elif found_experiment:
			print(line)

			found_experiment = False