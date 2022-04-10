#!/usr/bin/env python3
from collections import Counter

NUM_SERVERS = 5
SHARDS_PER_SERVER = 53
QUERY_SIZE = 3

MIN_NUM_2_CLUSTERS = 45
MAX_NUM_2_CLUSTERS = 60
ENTRIES_PER_NUM_2_CLUSTER = 30

def count_all_clusters(shards):
    shards = sorted(shards)
    cluster_counts = Counter()
    for index, start_shard in enumerate(shards):
        current_distance = 1
        current_shard_index = index
        current_cluster_size = 1

        while current_distance <= QUERY_SIZE:
            cluster_counts[current_cluster_size] += 1

            prev_shard = shards[current_shard_index]

            current_shard_index += 1
            current_cluster_size += 1

            if current_shard_index >= len(shards):
                current_shard_index = 0
                current_distance += shards[0] + NUM_SERVERS * SHARDS_PER_SERVER - prev_shard
            else:
                current_distance += shards[current_shard_index] - prev_shard

    return cluster_counts

# Get number of two clusters from random shard placements
num_random_two_clusters = []
num_entries = 0
res = [0 for i in range(100)]
total = 0

for NUM_CLUSTER_SIZE in range(MIN_NUM_2_CLUSTERS, MAX_NUM_2_CLUSTERS):
    numPrinted = 0
    with open('testSetupts.txt') as f:
        for l in f:
            line = l.strip()

            server_shards = line.split("\t")
            cluster_counts = Counter ()
            for i in server_shards:
                cur_server = []
                for j in (i.split(" ")):
                    if j != "":
                        cur_server.append(int(j))
                cluster_counts += count_all_clusters(cur_server)
            
            res[cluster_counts[2]] += 1

            if cluster_counts[2] == NUM_CLUSTER_SIZE:
                # print(line)
                numPrinted += 1

            if numPrinted == 20:
                break

    total += numPrinted

for index, i in enumerate(res):
    print("Index", str(index), " has", str(i))
print("Total entries:", int(num_entries))


'''
all 8000 setups:
Index 17  has 11
Index 18  has 13
Index 19  has 32
Index 20  has 53
Index 21  has 97
Index 22  has 182
Index 23  has 240
Index 24  has 376
Index 25  has 505
Index 26  has 639
Index 27  has 729
Index 28  has 779
Index 29  has 892
Index 30  has 809
Index 31  has 764
Index 32  has 694
Index 33  has 584
Index 34  has 471
Index 35  has 394
Index 36  has 286
Index 37  has 168
Index 38  has 129
Index 39  has 89
Index 40  has 56
Index 41  has 35
Index 42  has 19
Index 43  has 11


'''

