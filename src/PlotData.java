import java.awt.Dimension;
import java.io.IOException;
import java.util.Vector;
import java.io.*;

public class PlotData {

	private static final String FILE_NAME = "OUTPUT.txt";

	public static void displayGraph(String title, String seriesName, Vector<Double> values) throws IOException {
	    FileWriter fw = new FileWriter(FILE_NAME,true); 

	    fw.write("\t" + title + "\n");
	    fw.write("\t" + seriesName + "\n");
		for (Double i : values) {
			fw.write(Double.toString(i) + "\n");
		}

		fw.close();
	}
}
