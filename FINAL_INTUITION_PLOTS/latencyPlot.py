#!/usr/bin/env python3
from collections import Counter
import seaborn as sns
import matplotlib.pyplot as plt
import scipy
from statistics import mean


sns.set_theme()


random_results = {i: {} for i in range(5)}
parallel_results = {i: {} for i in range(5)}
output_arr = random_results

randomZeroQueries = []
parallelZeroQueries = []
curCount = 0
output_counts = randomZeroQueries

# Get p99s for parallel shard placements
with open('outputAllQueries.txt') as f:
    for l in f:
        line = l.strip()

        components = line.strip().split(" ")
        if "Shard" in components:
            shard = int(components[components.index("shard") + 1])
            latency = int(components[components.index("latency") + 1])
            server = int(components[components.index("Server") + 1])
            tick = int(components[components.index("tick") + 1])
            # Only append every second time, because random results have two experiments per cluster setup
            output_arr[server][tick] = output_arr[server].get(tick, []) + [latency]
            output_counts.append((tick, curCount))
        elif "parallel-maximising" in components:
            output_arr = parallel_results
            output_counts = parallelZeroQueries
            curCount = 0
        elif "increment" in components:
            if components[0] == "0":
                curCount += 1
        elif "decrement" in components:
            if components[0] == "0":
                curCount -= 1



# Average both
for i in random_results:
    cur_subplot = [(j, mean(random_results[i][j])) for j in random_results[i]]
    cur_subplot.sort()
    plt.plot([j[0] for j in cur_subplot], [j[1] for j in cur_subplot], label = "random server 0")
    break

for i in parallel_results:
    cur_subplot = [(j, mean(parallel_results[i][j])) for j in parallel_results[i]]
    cur_subplot.sort()
    plt.plot([j[0] for j in cur_subplot], [j[1] for j in cur_subplot], label = "parallel server 0")
    break

plt.legend()
plt.show()

plt.plot([i[0] for i in randomZeroQueries], [i[1] for i in randomZeroQueries], label = "random query counts")
plt.plot([i[0] for i in parallelZeroQueries], [i[1] for i in parallelZeroQueries], label = "parallel query counts")
plt.legend()
plt.show()



# TODO:
# - see which queries are the slowest ones
#     - look at trends e.g if youre a 2 cluster youre x percent more likely 