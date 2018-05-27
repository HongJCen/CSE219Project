package clustering;

import algorithms.Clusterer;
import data.DataSet;
import dataprocessors.AppData;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Ritwik Banerjee
 */
public class KMeansClusterer extends Clusterer {

    private DataSet dataset;
    private List<Point2D> centroids;

    private final int           maxIterations;
    private final int           updateInterval;
    private final AtomicBoolean tocontinue;
    private ApplicationTemplate applicationTemplate;


    public KMeansClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean toContinue) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(toContinue);
    }

    @Override
    public int getMaxIterations() { return maxIterations; }

    @Override
    public int getUpdateInterval() { return updateInterval; }

    @Override
    public boolean tocontinue() { return tocontinue.get(); }

    @Override
    public void setApplicationTemplate(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void run() {
        initializeCentroids();
        int iteration = 0;
        double timePerTick = 1000000000;
        double delta;
        long now;
        long lastTime;
        while (iteration++ < maxIterations) {
            if (iteration % updateInterval == 0)
                Platform.runLater(() -> ((AppUI) applicationTemplate.getUIComponent()).getChart().getData().clear());
            delta = 0;
            lastTime = System.nanoTime();
            assignLabels();
            recomputeCentroids();
            System.out.print("Update " + iteration + " : ");
            if (iteration % updateInterval == 0) {
                System.out.println("Chart Updated");
                Platform.runLater(() -> {
                            ((AppData) applicationTemplate.getDataComponent()).getProcessor().setDataLabels(dataset.getLabels());
                            ((AppData) applicationTemplate.getDataComponent()).getProcessor().setDataPoints(dataset.getLocations());
                            ((AppData) applicationTemplate.getDataComponent()).displayData();
                        }
                );
                while (delta < 1) {
                    now = System.nanoTime();
                    delta += (now - lastTime) / timePerTick;
                    lastTime = now;
                }
                if (!tocontinue())
                    synchronized (Thread.currentThread()) {
                        try {
                            Thread.currentThread().wait();
                        }catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
            }
            else
                System.out.println("Not Updated");
        }
    }

    private void initializeCentroids() {
        Set<String>  chosen        = new HashSet<>();
        List<String> instanceNames = new ArrayList<>(dataset.getLabels().keySet());
        Random       r             = new Random();
        while (chosen.size() < numberOfClusters) {
            int i = r.nextInt(instanceNames.size());
            while (chosen.contains(instanceNames.get(i)))
                ++i;
            chosen.add(instanceNames.get(i));
        }
        centroids = chosen.stream().map(name -> dataset.getLocations().get(name)).collect(Collectors.toList());
    }

    private void assignLabels() {
        dataset.getLocations().forEach((instanceName, location) -> {
            double minDistance      = Double.MAX_VALUE;
            int    minDistanceIndex = -1;
            for (int i = 0; i < centroids.size(); i++) {
                double distance = computeDistance(centroids.get(i), location);
                if (distance < minDistance) {
                    minDistance = distance;
                    minDistanceIndex = i;
                }
            }
            dataset.getLabels().put(instanceName, Integer.toString(minDistanceIndex));
        });
    }

    private void recomputeCentroids() {
        IntStream.range(0, numberOfClusters).forEach(i -> {
            AtomicInteger clusterSize = new AtomicInteger();
            Point2D sum = dataset.getLabels()
                    .entrySet()
                    .stream()
                    .filter(entry -> i == Integer.parseInt(entry.getValue()))
                    .map(entry -> dataset.getLocations().get(entry.getKey()))
                    .reduce(new Point2D(0, 0), (p, q) -> {
                        clusterSize.incrementAndGet();
                        return new Point2D(p.getX() + q.getX(), p.getY() + q.getY());
                    });
            Point2D newCentroid = new Point2D(sum.getX() / clusterSize.get(), sum.getY() / clusterSize.get());
            if (!newCentroid.equals(centroids.get(i))) {
                centroids.set(i, newCentroid);
            }
        });
    }

    private static double computeDistance(Point2D p, Point2D q) {
        return Math.sqrt(Math.pow(p.getX() - q.getX(), 2) + Math.pow(p.getY() - q.getY(), 2));
    }

}