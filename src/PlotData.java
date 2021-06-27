
//*******************************************************************
//  PlotData.java
//
// Saves the data to a file specified by the constant FILE_NAME. Must
// be used before grapher.py so that matplotlib can graph this text file.
//*******************************************************************
import java.io.IOException;
import java.util.Vector;
import java.io.*;
import java.util.Collections;

public class PlotData implements Comparable<PlotData> {

	public Double value;
	public int machineId;
	public int shardId;

	public PlotData(Double latency, int machineId, int shardId) {
		this.value = latency;
		this.machineId = machineId;
		this.shardId = shardId;
	}

	@Override
	public int compareTo(PlotData other) {
		// compareTo should return < 0 if this is supposed to be
		// less than other, > 0 if this is supposed to be greater than
		// other and 0 if they are supposed to be equal
		if (this.value < other.value) {
			return -1;
		} else if (this.value > other.value) {
			return 1;
		}
		return 0;
	}

	public String toString() {
		return value + " " + machineId + " " + shardId;
	}

	public static void displayGraph(String title, String seriesName, Vector<PlotData> values, FileWriter fw)
			throws IOException {

		if (seriesName != "Random") {
			fw.write(title + "\n");
		}

		fw.write(seriesName + " ");
		Collections.sort(values);

		int percentile_999th = (int) Math.ceil(99.9 / 100.0 * values.size());
		fw.write(values.get(percentile_999th).value.toString() + "\n");
		if (seriesName == "Random") {
			fw.write("\n");
		}
	}
}
