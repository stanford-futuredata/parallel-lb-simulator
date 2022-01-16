package edu.stanford.futuredata;

import java.util.HashMap;
import java.util.concurrent.*;
import java.util.Collection;

public class ShardAccess {
	private int assignedShard;
	private int numTicksLeft;

	private int startTick;
	private int endTick = -1;

	public ShardAccess(int shardId) {
		if (shardId >= App.NUM_MACHINES * App.NUM_SHARDS_PER_MACHINE) {
			throw new RuntimeException("Error! Assigned shard is not valid!");
		}

		this.assignedShard = shardId;
		this.numTicksLeft = App.TICKS_PER_ACCESS;
	}

	public Boolean isDone() {
		return numTicksLeft == 0;
	}

	public void decrementTick() {
		numTicksLeft -= 1;

		if (isDone()) {
			this.endTick = App.TICK;
		}
	}

	public int getAssignedShard() {
		return assignedShard;
	}

	public void setStartTick() {
		this.startTick = App.TICK;
	}

	public int getLatency() {
		if (endTick == -1) {
			throw new RuntimeException("Error! Can't find latency for " + numTicksLeft + " if endTick is not set!");
		}

		return endTick - startTick + 1;
	}

	public String toString() {
		if (isDone()) {
			return "Shard access on shard " + assignedShard + " completed with latency " + getLatency();
		}
		return "Shard access on shard " + assignedShard + " has " + numTicksLeft + " ticks left";
	}

	// Restore shard access to original state
	public void reset() {
		startTick = 0;
		numTicksLeft = App.TICKS_PER_ACCESS;
		endTick = -1;
	}
}
