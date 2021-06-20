import java.io.IOException;
import java.util.Vector;
import java.io.*;

public class PlotData {

	private static final String FILE_NAME = "OUTPUT.txt";

	public Double value;
	public int machineId;

	public PlotData(Double latency, int id) {
		value = latency;
		machineId = id;
	}

	public String toString() {
		return value + " " + machineId;
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
