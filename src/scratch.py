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
all_vals = []


with open("src/OUTPUT.txt", "r") as f:
	for line in f:
		line = line.strip()
		
		if line == "":
			all_vals.append((round_robin / random, round_robin, random, stats))
			stats = None 
			round_robin = None 
			random = None

		tokens = line.split(" ")
		if tokens[0] == "Round-Robin":
			round_robin = float(tokens[1])
		elif tokens[0] == "Random":
			random = float(tokens[1])
		else:
			stats = line

all_vals.sort()
for i in range(20):
	print(all_vals[i])
