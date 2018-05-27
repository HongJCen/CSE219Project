package ui;

import actions.AppActions;
import algorithms.Algorithm;
import algorithms.Classifier;
import classification.RandomClassifier;
import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import data.DataSet;
import dataprocessors.AppData;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.Reflection;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import javax.xml.crypto.Data;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static vilij.settings.PropertyTypes.GUI_RESOURCE_PATH;
import static vilij.settings.PropertyTypes.ICONS_RESOURCE_PATH;

/**
 * This is the application's user interface implementation.
 *
 * @author Ritwik Banerjee
 */
public final class AppUI extends UITemplate {

    /** The application to which this class of actions belongs. */
    ApplicationTemplate applicationTemplate;


    @SuppressWarnings("FieldCanBeLocal")
    private Button                       scrnshotButton; // toolbar button to take a screenshot of the data
    private LineChart<Number, Number>    chart;          // the chart where data will be displayed
    private Button                       displayButton;  // workspace button to display data on the chart
    private TextArea                     textArea;       // text area for new data input
    private boolean                      hasNewText;     // whether or not the text area has any new data since last display
    private CheckBox                     cb;
    private GridPane                     pane;
    private Text                         stats;
    private Button                       clust;
    private Button                       classif;
    private VBox                         leftPanel;
    private Algorithm                    runAlg;
    private ArrayList<Algorithm>         algList;
    private VBox                         algOption;

    public Button getBack() {
        return back;
    }

    public Button getRun() {
        return run;
    }

    private Button                       back;
    private Button                       run;
    private ToggleGroup                  algChoices;
    private HashMap<String, Config>      settings;
    private Thread                       thread;
    private DataSet                      ds;

    public Thread getThread()
    {
        return this.thread;
    }

    public Button getDisplayButton(){
        return displayButton;
    }

    public Button getScrnshotButton() {
        return scrnshotButton;
    }


    public LineChart<Number, Number> getChart() { return chart; }


    public AppUI(Stage primaryStage, ApplicationTemplate applicationTemplate) {
        super(primaryStage, applicationTemplate);
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    protected void setResourcePaths(ApplicationTemplate applicationTemplate) {
        super.setResourcePaths(applicationTemplate);
    }

    @Override
    protected void setToolBar(ApplicationTemplate applicationTemplate) {
        super.setToolBar(applicationTemplate);
        PropertyManager manager = applicationTemplate.manager;
        String iconsPath = SEPARATOR + String.join(SEPARATOR,
                                                   manager.getPropertyValue(GUI_RESOURCE_PATH.name()),
                                                   manager.getPropertyValue(ICONS_RESOURCE_PATH.name()));
        String scrnshoticonPath = String.join(SEPARATOR,
                                              iconsPath,
                                              manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_ICON.name()));
        scrnshotButton = setToolbarButton(scrnshoticonPath,
                                          manager.getPropertyValue(AppPropertyTypes.SCREENSHOT_TOOLTIP.name()),
                                          true);
        toolBar.getItems().add(scrnshotButton);
    }

    @Override
    protected void setToolbarHandlers(ApplicationTemplate applicationTemplate) {
        applicationTemplate.setActionComponent(new AppActions(applicationTemplate));
        newButton.setOnAction(e -> applicationTemplate.getActionComponent().handleNewRequest());
        saveButton.setOnAction(e -> applicationTemplate.getActionComponent().handleSaveRequest());
        loadButton.setOnAction(e -> applicationTemplate.getActionComponent().handleLoadRequest());
        exitButton.setOnAction(e -> applicationTemplate.getActionComponent().handleExitRequest());
        printButton.setOnAction(e -> applicationTemplate.getActionComponent().handlePrintRequest());
    }

    @Override
    public void initialize() {
        layout();
        setWorkspaceActions();
    }

    @Override
    public void clear() {
        textArea.clear();
        chart.getData().clear();
    }

    public TextArea getTextArea() {
        return textArea;
    }
    public CheckBox getCb() {return cb;}
    public Button getSaveButton() {return saveButton;}
    public String getCurrentText() { return textArea.getText(); }

    private void layout() {
        PropertyManager manager = applicationTemplate.manager;
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        Font font = new Font("Comic Sans MS", 14);
        settings = new HashMap<>();
        algList = new ArrayList<>();
        algOption = new VBox();
        run = new Button("Run");
        clust = new Button("Clustering");
        classif = new Button("Classification");
        back = new Button("Back");
        run.setVisible(false);
        back.setVisible(false);
        stats = new Text();
        chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(manager.getPropertyValue(AppPropertyTypes.CHART_TITLE.name()));
        chart.setAnimated(false);
        xAxis.setTickLabelFont(font);
        yAxis.setTickLabelFont(font);

        font = new Font("Comic Sans MS", 36);
        applicationTemplate.getUIComponent().getPrimaryScene().getStylesheets().add("properties/cssProperties.css");
        leftPanel = new VBox(8);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPadding(new Insets(10));

        VBox.setVgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setMaxSize(windowWidth * 0.29, windowHeight * 0.6);
        leftPanel.setMinSize(windowWidth * 0.29, windowHeight * 0.6);

        Text leftPanelTitle = new Text(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLE.name()));
        String fontname = manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLEFONT.name());
        Double fontsize = Double.parseDouble(manager.getPropertyValue(AppPropertyTypes.LEFT_PANE_TITLESIZE.name()));
        leftPanelTitle.setFont(Font.font(fontname, fontsize));

        textArea = new TextArea();

        cb = new CheckBox();
        cb.setText(manager.getPropertyValue(AppPropertyTypes.CHECKBOX.name()));
        cb.setFont(font);
        cb.setTextFill(Color.FUCHSIA);
        cb.setAllowIndeterminate(false);
        HBox processButtonsBox = new HBox();
        displayButton = new Button(manager.getPropertyValue(AppPropertyTypes.DISPLAY_BUTTON_TEXT.name()));
        displayButton.setFont(font);
        displayButton.setTextFill(Color.FUCHSIA);
        File[] classifList = new File(Thread.currentThread().getContextClassLoader().getResource("classification").getFile()).listFiles();
        File[] clusterList = new File(Thread.currentThread().getContextClassLoader().getResource("clustering").getFile()).listFiles();
        for (File x : classifList)
        {
            try {
                Class c = Class.forName("classification" + "." + x.getName().split("\\.")[0]);
                Algorithm add =  (Algorithm) c.getConstructor(DataSet.class, int.class, int.class, boolean.class).newInstance(new DataSet(), 0, 0, true);
                algList.add(add);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (File x : clusterList)
        {
            try {
                Class c = Class.forName("clustering" + "." + x.getName().split("\\.")[0]);
                Algorithm add = (Algorithm) c.getConstructor(DataSet.class, int.class, int.class, int.class, boolean.class).newInstance(new DataSet(), 0, 0, 0, true);
                algList.add(add);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (Algorithm x : algList)
        {
            System.out.println(x.getClass());
            Config defaultConfig = new Config();
            defaultConfig.setIterations(0);
            defaultConfig.setUpdateInterval(0);
            defaultConfig.setLabels(0);
            defaultConfig.setCont(false);
            settings.put(x.getClass().getName(), defaultConfig);
        }
        Task task = new Task() {
            @Override
            protected Object call() throws Exception {
                while(true) {
                    if ((thread.getState().equals(Thread.State.TERMINATED) || thread.getState().equals(Thread.State.NEW) || thread.getState().equals(Thread.State.WAITING)) && !chart.getData().isEmpty())
                        scrnshotButton.setDisable(false);
                    else
                        scrnshotButton.setDisable(true);
                    if (thread.getState().equals(Thread.State.WAITING) || thread.getState().equals(Thread.State.NEW) || thread.getState().equals(Thread.State.TERMINATED)) {
                        run.setDisable(false);
                        back.setDisable(false);
                    }
                    else {
                        run.setDisable(true);
                        back.setDisable(true);
                    }
                }
            }
        };
        Thread checking = new Thread(task);
        checking.start();
        thread = new Thread();
        run.setOnAction(event -> {
            try {
                if (((AppActions)applicationTemplate.getActionComponent()).getDataFilePath() == null)
                    throw new SaveException();
                Config config = settings.get(algChoices.getSelectedToggle().getUserData().getClass().getName());
                if (config.getIterations() <= 0)
                    throw new Exception();
                if (config.getUpdateInterval() <= 0)
                    throw new Exception();
                if (algChoices.getSelectedToggle().getUserData().getClass().getName().split("\\.")[0].equals("clustering") && config.getLabels() <= 0)
                    throw new Exception();
                if (thread.getState().equals(Thread.State.NEW) || thread.getState().equals(Thread.State.TERMINATED))
                {
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    String processString = textArea.getText() + '\n';
                    for (String s : ((AppData)applicationTemplate.getDataComponent()).getExtraLines())
                        processString += s + '\n';
                    try {
                        dataComponent.loadData(processString);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dataComponent.displayData();
                    if (((AppActions)applicationTemplate.getActionComponent()).getDataFilePath() != null)
                        ds = DataSet.fromTSDFile(((AppActions)applicationTemplate.getActionComponent()).getDataFilePath());
                    if (algChoices.getSelectedToggle().getUserData().getClass().getName().split("\\.")[0].equals("classification"))
                        runAlg = (Algorithm) algChoices.getSelectedToggle().getUserData().getClass().getConstructor(DataSet.class, int.class, int.class, boolean.class).newInstance(ds, config.getIterations(), config.getUpdateInterval(), config.isCont());
                    if (algChoices.getSelectedToggle().getUserData().getClass().getName().split("\\.")[0].equals("clustering")) {
                        if (config.getLabels() <= 2 && 2 > ((AppData)applicationTemplate.getDataComponent()).getProcessor().getDataPoints().size()) {
                            ErrorDialog.getDialog().show("Invalid Number of Clusters", "Invalid Number of Clusters");
                            return;
                        }
                        else if (config.getLabels() >= 4 && 4 > ((AppData)applicationTemplate.getDataComponent()).getProcessor().getDataPoints().size()){
                            ErrorDialog.getDialog().show("Invalid Number of Clusters", "Invalid Number of Clusters");
                            return;
                        }
                            runAlg = (Algorithm) algChoices.getSelectedToggle().getUserData().getClass().getConstructor(DataSet.class, int.class, int.class, int.class, boolean.class).newInstance(ds, config.getIterations(), config.getUpdateInterval(), config.getLabels(), config.isCont());
                    }
                    runAlg.setApplicationTemplate(applicationTemplate);
                    thread = new Thread(runAlg);
                    thread.start();
                }
                else {
                        synchronized (thread){
                            thread.notifyAll();
                        }
                    }
                }
                catch (SaveException e){
                    ErrorDialog.getDialog().show("Save First", "Please save the data first");
                }
                catch(Exception e){
                    ErrorDialog.getDialog().show("Invalid Config", "Configuration not valid");
                }

        });
        HBox.setHgrow(processButtonsBox, Priority.ALWAYS);
        processButtonsBox.getChildren().add(displayButton);
        stats.setText("No Data");
        HBox algbuttn = new HBox();
        algbuttn.setAlignment(Pos.CENTER);
        algbuttn.getChildren().addAll(run, classif, clust, back);
        leftPanel.getChildren().addAll(leftPanelTitle, textArea, processButtonsBox, cb, algbuttn, stats, algOption);
        leftPanel.setMaxSize(windowWidth * 0.31, windowHeight);
        StackPane rightPanel = new StackPane(chart);
        rightPanel.setMaxSize(windowWidth * 0.69, windowHeight * 0.69);
        rightPanel.setMinSize(windowWidth * 0.69, windowHeight * 0.69);
        StackPane.setAlignment(rightPanel, Pos.CENTER);

        workspace = new HBox(leftPanel, rightPanel);
        HBox.setHgrow(workspace, Priority.ALWAYS);
        appPane.getChildren().add(workspace);
        VBox.setVgrow(appPane, Priority.ALWAYS);
        newButton.setDisable(false);
        pane = new GridPane();
        Text text = new Text();
        text.setText(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.PLOT_NAME.name()));
        pane.add(text, 0,0);
        pane.setAlignment(Pos.TOP_RIGHT);
        pane.setTranslateX(-300);
        pane.setTranslateY(-300);
        appPane.getChildren().add(pane);
        setAlgButtons();
        setVisible(false);
        displayAlgorithmTypeSelection(false);

    }

    private void setAlgButtons(){
        clust.setOnAction(event -> displayClust());
        classif.setOnAction(event -> displayClassif());
        back.setOnAction(event -> {
            clust.setVisible(true);
            classif.setVisible(true);
            back.setVisible(false);
            run.setVisible(false);
            algOption.getChildren().remove(0, algOption.getChildren().size());
        });
    }

    private void displayClassif() {
        clust.setVisible(false);
        classif.setVisible(false);
        back.setVisible(true);
        algOption.getChildren().remove(0, algOption.getChildren().size());
        algChoices = new ToggleGroup();
        for (Algorithm x : algList)
        {
            if (x.getClass().getName().split("\\.")[0].equals("classification"))
            {
                HBox newOption = new HBox();
                Button option = new Button("Options");
                option.setOnAction((ActionEvent event) -> {
                    Config config = settings.get(x.getClass().getName());
                    Stage stage = new Stage();
                    GridPane gp = new GridPane();
                    stage.setTitle(x.getClass().getName().split("\\.")[1]);
                    Scene scene = new Scene(gp,160,100);
                    stage.setScene(scene);
                    Text max = new Text("Max Iterations: ");
                    TextField maxIterations = new TextField();
                    Text update = new Text("Update Interval: ");
                    TextField updateField = new TextField();
                    Button save = new Button("Save");
                    CheckBox continuous = new CheckBox("Continuous? ");
                    maxIterations.setText(String.valueOf(config.getIterations()));
                    updateField.setText(String.valueOf(config.getUpdateInterval()));
                    continuous.setSelected(config.isCont());
                    gp.add(max, 0, 0);
                    gp.add(maxIterations, 1, 0);
                    gp.add(update, 0, 1);
                    gp.add(updateField, 1,1);
                    gp.add(continuous,0,2);
                    gp.add(save, 0,3);
                    save.setOnAction((ActionEvent event1) -> {
                        while (true) {
                            config.setCont(continuous.isSelected());
                            try {
                                config.setUpdateInterval(Integer.parseInt(updateField.getText()));
                                if (Integer.parseInt(updateField.getText()) <= 0)
                                    throw new Exception();
                                config.setIterations(Integer.parseInt(maxIterations.getText()));
                                if (Integer.parseInt(maxIterations.getText()) <= 0)
                                    throw new Exception();
                            }
                            catch (Exception e){
                                ErrorDialog.getDialog().show("Incorrect Entry", "Enter a valid number");
                            }
                            break;
                        }
                        stage.close();
                    });
                    stage.show();
                    });
                RadioButton choice = new RadioButton(x.getClass().getName().split("\\.")[1]);
                choice.setUserData(x);
                choice.setToggleGroup(algChoices);
                choice.setOnAction(event -> run.setVisible(true));
                newOption.getChildren().add(choice);
                newOption.getChildren().add(option);
                algOption.getChildren().add(newOption);
            }
        }
    }
    private void displayClust() {
        clust.setVisible(false);
        classif.setVisible(false);
        back.setVisible(true);
        algOption.getChildren().remove(0, algOption.getChildren().size());
        algChoices = new ToggleGroup();
        for (Object x : algList)
        {
            if (x.getClass().getName().split("\\.")[0].equals("clustering"))
            {
                HBox newOption = new HBox();
                Button option = new Button("Options");
                option.setOnAction(event -> {
                    Config config = settings.get(x.getClass().getName());
                    Stage stage = new Stage();
                    GridPane gp = new GridPane();
                    stage.setTitle(x.getClass().getName().split("\\.")[1]);
                    Scene scene = new Scene(gp,160,150);
                    stage.setScene(scene);
                    Text max = new Text("Max Iterations: ");
                    TextField maxIterations = new TextField();
                    Text update = new Text("Update Interval: ");
                    TextField updateField = new TextField();
                    Text clusters = new Text("Number of Clusters: ");
                    TextField clustField = new TextField();
                    CheckBox continuous = new CheckBox("Continuous? ");
                    Button save = new Button("Save");
                    maxIterations.setText(String.valueOf(config.getIterations()));
                    updateField.setText(String.valueOf(config.getUpdateInterval()));
                    clustField.setText(String.valueOf(config.getLabels()));
                    continuous.setSelected(config.isCont());
                    gp.add(max, 0, 0);
                    gp.add(maxIterations, 1, 0);
                    gp.add(update, 0, 1);
                    gp.add(updateField, 1,1);
                    gp.add(clusters, 0,2);
                    gp.add(clustField,1,2);
                    gp.add(continuous,0,3);
                    gp.add(save, 0, 4);
                    save.setOnAction((ActionEvent event1) -> {
                        while (true) {
                            config.setCont(continuous.isSelected());
                            try {
                                config.setUpdateInterval(Integer.parseInt(updateField.getText()));
                                if (Integer.parseInt(updateField.getText()) <= 0)
                                    throw new Exception();
                                config.setIterations(Integer.parseInt(maxIterations.getText()));
                                if (Integer.parseInt(maxIterations.getText()) <= 0)
                                    throw new Exception();
                                config.setLabels(Integer.parseInt(clustField.getText()));
                                if (Integer.parseInt(clustField.getText()) <= 0)
                                    throw new Exception();
                            }
                            catch (Exception e){
                                ErrorDialog.getDialog().show("Incorrect Entry", "Enter a valid number");
                            }
                            break;
                        }
                        stage.close();
                    });
                    stage.show();
                });
                RadioButton choice = new RadioButton(x.getClass().getName().split("\\.")[1]);
                choice.setUserData(x);
                choice.setToggleGroup(algChoices);
                newOption.getChildren().add(choice);
                choice.setOnAction(event -> run.setVisible(true));
                newOption.getChildren().add(option);
                algOption.getChildren().add(newOption);
            }
        }
    }

    public void setVisible(boolean visible){
        workspace.setVisible(visible);
        pane.setVisible(!visible);
    }

    public Button getNewButton() {
        return newButton;
    }

    private void setWorkspaceActions() {
        setTextAreaActions();
        setDisplayButtonActions();
        setCheckBoxActions();
        setScrnshotActions();
    }

    private void setScrnshotActions() {
        scrnshotButton.setOnAction(event -> {
            try {
                ((AppActions)applicationTemplate.getActionComponent()).handleScreenshotRequest();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void setCheckBoxActions() {
        cb.setOnAction(event -> {
            if (cb.isSelected()) {
                textArea.setDisable(true);
                try {
                    displayStats(true);
                } catch (Exception e) {
                    return;
                }
                displayAlgorithmTypeSelection(true);
            }
            else
            {
                textArea.setDisable(false);
                try {
                    displayStats(false);
                } catch (Exception e) {
                    return;
                }
                displayAlgorithmTypeSelection(false);
                algOption.getChildren().remove(0, algOption.getChildren().size());
                clust.setVisible(false);
                classif.setVisible(false);
                run.setVisible(false);
                back.setVisible(false);
            }
        }
        );
    }

    public void displayAlgorithmTypeSelection(boolean display) {
        classif.setVisible(display);
        if (((AppData)applicationTemplate.getDataComponent()).getProcessor().getNumOfLabels() != 2)
            classif.setDisable(true);
        else
            classif.setDisable(false);
        clust.setVisible(display);
        if (((AppData)applicationTemplate.getDataComponent()).getProcessor().getDataPoints().size() < 2)
            clust.setDisable(true);
        else
            clust.setDisable(false);
    }

    public void displayStats(boolean display) throws Exception {
        if (display)
        {
            applicationTemplate.getDataComponent().clear();
            try {
                String processString = ((AppUI)applicationTemplate.getUIComponent()).getCurrentText() + '\n';
                for (String s : ((AppData)applicationTemplate.getDataComponent()).getExtraLines())
                {
                    processString += s + "\n";
                }((AppData)applicationTemplate.getDataComponent()).getProcessor().processString(processString);
            } catch (Exception e) {
                stats.setText("There is an error in the text area.");
                throw new Exception();
            }
            String dataFilePath;
            if (((AppActions)applicationTemplate.getActionComponent()).getDataFilePath() == null)
                dataFilePath = "Not Saved";
            else
                dataFilePath = ((AppActions)applicationTemplate.getActionComponent()).getDataFilePath().toString();
            stats.setText(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.INSTANCES.name()) +
                    ((AppData)applicationTemplate.getDataComponent()).getProcessor().getInstances() + "\n" +
                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.LABELS.name()) +
                    ((AppData)applicationTemplate.getDataComponent()).getProcessor().getNumOfLabels() + "\n" +
                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.LABEL_NAMES.name()) +
                    ((AppData)applicationTemplate.getDataComponent()).getProcessor().getLabelNames() + "\n" +
                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.PATH.name()) +
                    dataFilePath);
        }
        else
            stats.setText("Please Check Read Only");
    }

    private void setTextAreaActions() {
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                if (!newValue.equals(oldValue)) {
                    saveButton.setDisable(false);
                    if (textArea.getText().split("\n").length < 10)
                        if(!((AppData) applicationTemplate.getDataComponent()).getExtraLines().isEmpty()) {
                            textArea.appendText(((AppData) applicationTemplate.getDataComponent()).getExtraLines().get(0) + "\n");
                            ((AppData) applicationTemplate.getDataComponent()).getExtraLines().remove(0);
                        }
                    if (!newValue.isEmpty()) {
                        ((AppActions) applicationTemplate.getActionComponent()).setIsUnsavedProperty(true);
                        if (newValue.charAt(newValue.length() - 1) == '\n')
                            hasNewText = true;
                        newButton.setDisable(false);
                        saveButton.setDisable(false);
                    } else {
                        hasNewText = true;
                        newButton.setDisable(true);
                        saveButton.setDisable(true);
                    }
                }
            } catch (IndexOutOfBoundsException e) {
                System.err.println(newValue);
            }
        });
    }

    private void setDisplayButtonActions() {
        displayButton.setOnAction(event -> {
                try {
                    chart.getData().clear();
                    AppData dataComponent = (AppData) applicationTemplate.getDataComponent();
                    dataComponent.clear();
                    String processString = textArea.getText() + '\n';
                    for (String s : ((AppData)applicationTemplate.getDataComponent()).getExtraLines())
                        processString += s + '\n';
                    dataComponent.loadData(processString);
                    dataComponent.displayData();
                    scrnshotButton.setDisable(false);
                } catch (Exception e) {
                    scrnshotButton.setDisable(true);
                    Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                    error.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DISPLAY_ERR_TITLE.name()),
                            e.getMessage().toString());
                }
        });
    }

    private class SaveException extends Throwable {
    }
}
