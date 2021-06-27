//*******************************************************************
//  PlotData.java
//
// Saves the data to a file specified by the constant FILE_NAME. Must
// be used before grapher.py so that matplotlib can graph this text file.
//*******************************************************************
import java.io.IOException;
import java.util.Vector;
import java.io.*;

public class PlotData {

	private static final String FILE_NAME = "OUTPUT.txt";

	public Double value;
	public int machineId;
	public int shardId;

	public PlotData(Double latency, int machineId, int shardId) {
		this.value = latency;
		this.machineId = machineId;
		this.shardId = shardId;
	}

	public String toString() {
		return value + " " + machineId + " " + shardId;
	}

	public static void displayGraph(String title, String seriesName, Vector<PlotData> values) throws IOException {
	    FileWriter fw = new FileWriter(FILE_NAME,true); 

	    fw.write("\t" + title + "\n");
	    fw.write("\t" + seriesName + "\n");
		for (PlotData i : values) {
			fw.write(i.toString() + "\n");
		}

		fw.close();
	}
}
