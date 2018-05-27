package dataprocessors;

import algorithms.Algorithm;
import algorithms.Classifier;
import classification.RandomClassifier;
import javafx.geometry.Point2D;
import javafx.scene.ImageCursor;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import ui.AppUI;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * The data files used by this data visualization applications follow a tab-separated format, where each data point is
 * named, labeled, and has a specific location in the 2-dimensional X-Y plane. This class handles the parsing and
 * processing of such data. It also handles exporting the data to a 2-D plot.
 * <p>
 * A sample file in this format has been provided in the application's <code>resources/data</code> folder.
 *
 * @author Ritwik Banerjee
 * @see XYChart
 */
public final class TSDProcessor {

    public static class InvalidDataNameException extends Exception {

        private static final String NAME_ERROR_MSG = "All data instance names must start with the @ character.";

        public InvalidDataNameException(String name) {
            super(String.format("Invalid name '%s'." + NAME_ERROR_MSG, name));
        }
    }

    public Map<String, String> getDataLabels() {
        return dataLabels;
    }

    public void setDataLabels(Map<String, String> dataLabels) {
        this.dataLabels = dataLabels;
    }

    public Map<String, Point2D> getDataPoints() {
        return dataPoints;
    }

    public void setDataPoints(Map<String, Point2D> dataPoints) {
        this.dataPoints = dataPoints;
    }

    private Map<String, String>  dataLabels;
    private Map<String, Point2D> dataPoints;
    private AtomicInteger instances = new AtomicInteger();

    public int getInstances() {
        return instances.get();
    }

    public TSDProcessor() {
        dataLabels = new HashMap<>();
        dataPoints = new HashMap<>();
    }

    public int getNumOfLabels(){
        ArrayList<String> labels = new ArrayList<>();
        int count = 0;
        for (String label : dataLabels.values())
        {
            if (!labels.contains(label) && !label.equals("null"))
            {
                labels.add(label);
                count++;
            }
        }
        return count;
    }
    public String getLabelNames(){
        ArrayList<String> labels = new ArrayList<>();
        for (String label : dataLabels.values())
        {
            if (!labels.contains(label) && !label.equals("null"))
                labels.add(label);
        }
        return labels.toString();
    }
    /**
     * Processes the data and populated two {@link Map} objects with the data.
     *
     * @param tsdString the input data provided as a single {@link String}
     * @throws Exception if the input string does not follow the <code>.tsd</code> data format
     */
    public void processString(String tsdString) throws Exception {
        ArrayList<String> names    = new ArrayList<>();
        AtomicInteger counter      = new AtomicInteger();
        AtomicBoolean hadAnError   = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        instances.set(0);

        Stream.of(tsdString.split("\n"))
              .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                  try {
                      counter.getAndIncrement();
                      String   name  = checkedname(list.get(0));
                      for (String n : names)
                          if (n.equals(name))
                          {
                              Exception e = new Exception("There are duplicate names.");
                              throw e;
                          }
                      names.add(name);
                      String   label = list.get(1);
                      instances.incrementAndGet();
                      String[] pair  = list.get(2).split(",");
                      Point2D  point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                      dataLabels.put(name, label);
                      dataPoints.put(name, point);
                  }
                  catch (ArrayIndexOutOfBoundsException e) {
                      errorMessage.setLength(0);
                      errorMessage.append("There is a missing number.");
                      errorMessage.append("\nThere is an error on line: ");
                      errorMessage.append(counter.get());
                      hadAnError.set(true);
                  }
                  catch (Exception e) {
                      errorMessage.setLength(0);
                      errorMessage.append(e.getMessage());
                      errorMessage.append("\nThere is an error on line: ");
                      errorMessage.append(counter.get());
                      hadAnError.set(true);
                  }
              });
        if (errorMessage.length() > 0)
            throw new Exception(errorMessage.toString());
    }

    /**
     * Exports the data to the specified 2-D chart.
     *
     * @param chart the specified chart
     */
    void toChartData(XYChart<Number, Number> chart) {
        Set<String> labels = new HashSet<>(dataLabels.values());
        Image image = new Image("properties/darling.png");
        for (String label : labels) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(label);
            dataLabels.entrySet().stream().filter(entry -> entry.getValue().equals(label)).forEach(entry -> {
                Point2D point = dataPoints.get(entry.getKey());
                series.getData().add(new XYChart.Data<>(point.getX(), point.getY()));
            });
            chart.getData().add(series);
        }
        chart.getData().forEach(numberNumberSeries -> {
            numberNumberSeries.getData().forEach(numberNumberData -> {
                Tooltip.install(numberNumberData.getNode(),
                        new Tooltip(getKey(new Point2D(numberNumberData.getXValue().doubleValue(), numberNumberData.getYValue().doubleValue()))));
                numberNumberData.getNode().setOnMouseEntered(event -> numberNumberData.getNode().setCursor(new ImageCursor(image, image.getWidth(), image.getHeight())));
            });
            numberNumberSeries.getNode().setId("daddy");
            }
        );
    }

    String getKey(Point2D point)
    {
        for (String key : dataPoints.keySet())
            if (point.equals(dataPoints.get(key)))
                return key;
        return "";
    }
    void clear() {
        dataPoints.clear();
        dataLabels.clear();
    }

    private String checkedname(String name) throws InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new InvalidDataNameException(name);
        return name;
    }

    public synchronized void addClassLine(XYChart<Number, Number> chart, Integer A, Integer B, Integer C)
    {

        double lowerX = ((Point2D) (dataPoints.values().toArray()[0])).getX();
        double upperX = ((Point2D) (dataPoints.values().toArray()[0])).getX();
        for (XYChart.Series<Number, Number> series : chart.getData())
        {
            for (XYChart.Data<Number, Number> data : series.getData())
            {
                if (lowerX > data.getXValue().doubleValue())
                    lowerX = data.getXValue().doubleValue();
                if (upperX < data.getXValue().doubleValue())
                    upperX = data.getXValue().doubleValue();
            }
        }
        XYChart.Series<Number, Number> clasLine = new XYChart.Series<>();
        clasLine.setName("Algorithm Line");
        clasLine.getData().add(new XYChart.Data<>(lowerX, ((C.doubleValue() * -1) - (A.doubleValue() * lowerX))/
                (B.doubleValue())));
        clasLine.getData().add(new XYChart.Data<>(upperX, ((C.doubleValue() * -1) - (A.doubleValue() * upperX))/
                (B.doubleValue())));
        if (chart.getData().get(chart.getData().size() - 1).getName().equals("Algorithm Line"))
        chart.getData().add(clasLine);
        chart.getData().get(chart.getData().size() - 1).getData().forEach(numberNumberData -> numberNumberData.getNode().setVisible(false));
    }
}
