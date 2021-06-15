import java.awt.Dimension;
import java.util.Vector;

import org.jfree.chart.*;
import org.jfree.data.statistics.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

public class PlotHistogram extends ApplicationFrame{

  private final int WIDTH = 1000;
  private final int HEIGHT = 500;
  private final int NUM_BUCKETS = 300;

  private PlotHistogram(String title, String seriesName, double[] values) {
    super(title);

    HistogramDataset dataset = new HistogramDataset();
    dataset.setType(HistogramType.FREQUENCY);
    dataset.addSeries(seriesName, values, NUM_BUCKETS);

    String xTitle = "Latency (unit time)";
    String yTitle = "Frequency";
    PlotOrientation orientation = PlotOrientation.VERTICAL;
    boolean show = true;
    boolean toolTips = true;
    boolean urls = false;
    JFreeChart chart =
            ChartFactory.createHistogram(
                    title, xTitle, yTitle, dataset, orientation, true, true, false);

    ChartPanel chartPanel = new ChartPanel(chart, false);
    chartPanel.setBackground(null);
    chartPanel.setFillZoomRectangle(true);
    chartPanel.setMouseWheelEnabled(true);
    chartPanel.setDismissDelay(Integer.MAX_VALUE);
    chartPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
    setContentPane(chartPanel);
  }

  public static void displayGraph(String title, String seriesName, Vector<Double> values) {
    double[] staticValues = new double[values.size()];
    for (int i = 0; i < staticValues.length; i++) {
      staticValues[i] = values.get(i);
    }

    PlotHistogram plot = new PlotHistogram(title, seriesName, staticValues);
    plot.pack();
    RefineryUtilities.centerFrameOnScreen(plot);
    plot.setVisible(true);
  }
}
