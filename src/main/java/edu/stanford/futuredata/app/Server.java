package edu.stanford.futuredata;

import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.*;

public class Server {
	public final int ID;

	private Queue<ShardAccess> pendingAccesses;
	private Queue<ShardAccess> currentTickAccesses;

	private int busyTime = 0;

	public Server(int id) {
		this.ID = id;
		this.pendingAccesses = new LinkedList<ShardAccess>();
		this.currentTickAccesses = new LinkedList<ShardAccess>();
	}

	public void addAccess(ShardAccess access) { 
		pendingAccesses.add(access);
	}

	public int getRemainingAccesses() {
		return pendingAccesses.size();
	}

	public int getBusyTime() {
		return busyTime;
	}

	public void processOneTick(Boolean output) {
		if (getRemainingAccesses() > 0) {
			busyTime += 1;
		}

		// Get next n tasks to process
		for (int i = 0; i < App.NUM_CORES_PER_MACHINE; i++) {
			if (getRemainingAccesses() == 0) {
				break;
			}

			currentTickAccesses.add(pendingAccesses.remove());
		}

		while (currentTickAccesses.size() > 0) {
			ShardAccess access = currentTickAccesses.remove();
			access.decrementTick();

			if (!access.isDone()) {
				pendingAccesses.add(access);
			} else if (output) {
				System.out.println(access + " is done at tick " + App.TICK + " with latency " + access.getLatency());
			}
		}
	}

	public String toString() {
		return "Server " + ID;
	}
}

