package clustering;

import algorithms.Clusterer;
import data.DataSet;
import dataprocessors.AppData;
import javafx.application.Platform;
import ui.AppUI;
import vilij.templates.ApplicationTemplate;

import java.util.concurrent.atomic.AtomicBoolean;

public class RandomClusterer extends Clusterer {

    private DataSet dataset;
    private final int maxIterations;
    private final int updateInterval;
    private final AtomicBoolean tocontinue;
    private ApplicationTemplate applicationTemplate;

    public RandomClusterer(DataSet dataset, int maxIterations, int updateInterval, int numberOfClusters, boolean toContinue) {
        super(numberOfClusters);
        this.dataset = dataset;
        this.maxIterations = maxIterations;
        this.updateInterval = updateInterval;
        this.tocontinue = new AtomicBoolean(toContinue);
    }

    @Override
    public int getMaxIterations() {
        return maxIterations;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public boolean tocontinue() {
        return tocontinue.get();
    }

    @Override
    public void setApplicationTemplate(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void run() {
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
            dataset.getLocations().forEach((instanceName, location) -> dataset.getLabels().put(instanceName, Integer.toString((int)Math.ceil(Math.random() * numberOfClusters))));
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
}
