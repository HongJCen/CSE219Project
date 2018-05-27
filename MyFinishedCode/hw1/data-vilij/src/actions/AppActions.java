package actions;

import dataprocessors.AppData;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.ActionComponent;
import vilij.components.ConfirmationDialog;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;
import vilij.templates.UITemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static vilij.settings.PropertyTypes.SAVE_WORK_TITLE;
import static vilij.templates.UITemplate.SEPARATOR;

/**
 * This is the concrete implementation of the action handlers required by the application.
 *
 * @author Ritwik Banerjee
 */
public final class AppActions implements ActionComponent {

    /** The application to which this class of actions belongs. */
    private ApplicationTemplate applicationTemplate;

    /** Path to the data file currently active. */
    Path dataFilePath;

    /** The boolean property marking whether or not there are any unsaved changes. */
    SimpleBooleanProperty isUnsaved;

    public AppActions(ApplicationTemplate applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
        this.isUnsaved = new SimpleBooleanProperty(false);
    }

    public Path getDataFilePath() { return dataFilePath; }
    public void setIsUnsavedProperty(boolean property) { isUnsaved.set(property); }

    @Override
    public void handleNewRequest() {
        try {
            ((AppUI) applicationTemplate.getUIComponent()).setVisible(true);
            if (!isUnsaved.get() || promptToSave()) {
                applicationTemplate.getDataComponent().clear();
                applicationTemplate.getUIComponent().clear();
                isUnsaved.set(false);
                dataFilePath = null;
                ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
                ((AppUI)applicationTemplate.getUIComponent()).getNewButton().setDisable(true);
                ((AppUI)applicationTemplate.getUIComponent()).getDisplayButton().setVisible(true);
                ((AppUI)applicationTemplate.getUIComponent()).getCb().setVisible(true);
            }
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handleSaveRequest() {
        try {

            if (dataFilePath == null) {
                ((AppData) applicationTemplate.getDataComponent()).loadData((((AppUI) applicationTemplate.getUIComponent()).getCurrentText()));
                FileChooser fc = new FileChooser();
                File file;
                fc.setInitialDirectory(new File("/"));
                fc.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_WORK_TITLE.name()));
                String description = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*%s)", description, extension),
                        String.format("*%s", extension));
                fc.getExtensionFilters().add(extFilter);
                if ((file = fc.showSaveDialog(new Stage())) == null)
                    return;
                dataFilePath = file.toPath();
            }
            try {
                ((AppData) applicationTemplate.getDataComponent()).loadData(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
            } catch (Exception e) {
                Dialog err = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                err.show(
                        applicationTemplate.manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                        e.getMessage().toString()
                );
                return;
            }
            applicationTemplate.getDataComponent().saveData(dataFilePath);
            ((AppUI) applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
            isUnsaved.set(false);
        } catch (Exception e) {
            Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            error.show(
                    applicationTemplate.manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                    e.getMessage().toString()
            );
        }
    }


    @Override
    public void handleLoadRequest() {
        FileChooser fc = new FileChooser();
        File        file;
        fc.setInitialDirectory(new File("/"));
        String description = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
        String extension = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*%s)", description, extension),
                String.format("*%s", extension));
        fc.getExtensionFilters().add(extFilter);
        if ((file = fc.showOpenDialog(new Stage())) == null)
            return;
        ((AppUI)applicationTemplate.getUIComponent()).getChart().getData().clear();
        applicationTemplate.getDataComponent().loadData(file.toPath());
        dataFilePath = file.toPath();
        ((AppUI)applicationTemplate.getUIComponent()).getSaveButton().setDisable(true);
        ((AppUI)applicationTemplate.getUIComponent()).getScrnshotButton().setDisable(true);
        ((AppUI)applicationTemplate.getUIComponent()).getCb().setSelected(true);
        ((AppUI)applicationTemplate.getUIComponent()).setVisible(true);
        ((AppUI)applicationTemplate.getUIComponent()).getTextArea().setDisable(true);
        try {
            ((AppUI)applicationTemplate.getUIComponent()).displayStats(true);
        } catch (Exception e) {
            return;
        }
        ((AppUI)applicationTemplate.getUIComponent()).displayAlgorithmTypeSelection(true);
        ((AppUI)applicationTemplate.getUIComponent()).getCb().setVisible(false);
        ((AppUI)applicationTemplate.getUIComponent()).getDisplayButton().setVisible(false);
        ((AppUI)applicationTemplate.getUIComponent()).getRun().setVisible(false);
        ((AppUI)applicationTemplate.getUIComponent()).getBack().setVisible(false);
    }

    @Override
    public void handleExitRequest() {
        try {
            ConfirmationDialog dialog = ConfirmationDialog.getDialog();
            if (((AppUI) applicationTemplate.getUIComponent()).getThread().isAlive())
            {
                dialog.show("Algorithm is Running", "An Algorithm is Running, Would You Like To Exit?");
                if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES))
                    System.exit(0);
                else
                    return;
            }
            if (!isUnsaved.get() || promptToSave())
                System.exit(0);
        } catch (IOException e) { errorHandlingHelper(); }
    }

    @Override
    public void handlePrintRequest() {
    }

    public void handleScreenshotRequest() throws IOException {
        WritableImage image;
        image = ((AppUI) applicationTemplate.getUIComponent()).getChart().snapshot(null, null);
        FileChooser fc = new FileChooser();
        File file;
        fc.setInitialDirectory(new File("/"));
        fc.setTitle(applicationTemplate.manager.getPropertyValue(SAVE_WORK_TITLE.name()));
        String description = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.IMG_FILE_EXT_DESC.name());
        String extension = applicationTemplate.manager.getPropertyValue(AppPropertyTypes.IMG_FILE_EXT.name());
        ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*%s)", description, extension),
                String.format("*%s", extension));
        fc.getExtensionFilters().add(extFilter);
        if ((file = fc.showSaveDialog(new Stage())) == null)
            return;
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
    }

    /**
     * This helper method verifies that the user really wants to save their unsaved work, which they might not want to
     * do. The user will be presented with three options:
     * <ol>
     * <li><code>yes</code>, indicating that the user wants to save the work and continue with the action,</li>
     * <li><code>no</code>, indicating that the user wants to continue with the action without saving the work, and</li>
     * <li><code>cancel</code>, to indicate that the user does not want to continue with the action, but also does not
     * want to save the work at this point.</li>
     * </ol>
     *
     * @return <code>false</code> if the user presses the <i>cancel</i>, and <code>true</code> otherwise.
     */
    private boolean promptToSave() throws IOException {
        PropertyManager    manager = applicationTemplate.manager;
        ConfirmationDialog dialog  = ConfirmationDialog.getDialog();
        dialog.show(manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK_TITLE.name()),
                    manager.getPropertyValue(AppPropertyTypes.SAVE_UNSAVED_WORK.name()));

        if (dialog.getSelectedOption() == null) return false; // if user closes dialog using the window's close button

        if (dialog.getSelectedOption().equals(ConfirmationDialog.Option.YES)) {
            try {
                ((AppData) applicationTemplate.getDataComponent()).loadData((((AppUI) applicationTemplate.getUIComponent()).getCurrentText()));
            }
            catch (Exception e) {
                Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                error.show(
                        manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name()),
                        e.getMessage().toString()
                );
                return false;
            }
            if (dataFilePath == null) {
                FileChooser fileChooser = new FileChooser();
                String      dataDirPath = SEPARATOR + manager.getPropertyValue(AppPropertyTypes.DATA_RESOURCE_PATH.name());
                URL         dataDirURL  = getClass().getResource(dataDirPath);

                if (dataDirURL == null)
                    throw new FileNotFoundException(manager.getPropertyValue(AppPropertyTypes.RESOURCE_SUBDIR_NOT_FOUND.name()));

                fileChooser.setInitialDirectory(new File(dataDirURL.getFile()));
                fileChooser.setTitle(manager.getPropertyValue(SAVE_WORK_TITLE.name()));

                String description = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT_DESC.name());
                String extension   = manager.getPropertyValue(AppPropertyTypes.DATA_FILE_EXT.name());
                ExtensionFilter extFilter = new ExtensionFilter(String.format("%s (*%s)", description, extension),
                                                                String.format("*%s", extension));

                fileChooser.getExtensionFilters().add(extFilter);
                File selected = fileChooser.showSaveDialog(applicationTemplate.getUIComponent().getPrimaryWindow());
                if (selected != null) {
                    dataFilePath = selected.toPath();
                    save();
                } else return false; // if user presses escape after initially selecting 'yes'
            } else
                save();
        }

        return !dialog.getSelectedOption().equals(ConfirmationDialog.Option.CANCEL);
    }

    private void save() throws IOException {
        applicationTemplate.getDataComponent().saveData(dataFilePath);
        isUnsaved.set(false);
    }

    private void errorHandlingHelper() {
        ErrorDialog     dialog   = (ErrorDialog) applicationTemplate.getDialog(Dialog.DialogType.ERROR);
        PropertyManager manager  = applicationTemplate.manager;
        String          errTitle = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_TITLE.name());
        String          errMsg   = manager.getPropertyValue(PropertyTypes.SAVE_ERROR_MSG.name());
        String          errInput = manager.getPropertyValue(AppPropertyTypes.SPECIFIED_FILE.name());
        dialog.show(errTitle, errMsg + errInput);
    }
}
