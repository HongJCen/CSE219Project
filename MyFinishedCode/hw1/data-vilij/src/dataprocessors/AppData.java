package dataprocessors;

import settings.AppPropertyTypes;
import ui.AppUI;
import vilij.components.DataComponent;
import vilij.components.Dialog;
import vilij.components.ErrorDialog;
import vilij.propertymanager.PropertyManager;
import vilij.settings.PropertyTypes;
import vilij.templates.ApplicationTemplate;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * This is the concrete application-specific implementation of the data component defined by the Vilij framework.
 *
 * @author Ritwik Banerjee
 * @see DataComponent
 */
public class AppData implements DataComponent {


    public TSDProcessor getProcessor() {
        return processor;
    }

    private TSDProcessor        processor;
    private ApplicationTemplate applicationTemplate;
    private ArrayList<String>   extraLines = new ArrayList<>();

    public ArrayList<String> getExtraLines() {
        return extraLines;
    }

    public AppData(ApplicationTemplate applicationTemplate) {
        this.processor = new TSDProcessor();
        this.applicationTemplate = applicationTemplate;
    }

    @Override
    public void loadData(Path dataFilePath) {
        try {
            short counter = 0;
            String reader2;
            String input = "";
            String total;
            BufferedReader reader = new BufferedReader(new FileReader(dataFilePath.toFile()));
            while ((reader2 = reader.readLine()) != null) {
                counter++;
                if (counter <= 10) {
                    input += reader2;
                    input += '\n';
                }
                else
                {
                    extraLines.add(reader2);
                }
            }
            total = input;
            if (extraLines.size() > 0)
            {
                for (int i = 0; i < extraLines.size(); i++)
                {
                    total += extraLines.get(i) + '\n';
                }
            }
            loadData(total);
            if (counter >= 1)
                input = input.substring(0, input.length() - 1);
            ((AppUI) applicationTemplate.getUIComponent()).getTextArea().setText(input);
            if (counter > 10)
            {
                Dialog ExtraLines = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
                ExtraLines.show(applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MORE_LINES.name()),
                        applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MORE_LINES_MSG1.name()) +
                                    counter +
                                    applicationTemplate.manager.getPropertyValue(AppPropertyTypes.MORE_LINES_MSG2.name()));
            }
        } catch (IOException e) {
            System.out.println("ERROR");
        } catch (Exception e) {
            Dialog error = applicationTemplate.getDialog(Dialog.DialogType.ERROR);
            error.show(applicationTemplate.manager.getPropertyValue(PropertyTypes.LOAD_ERROR_TITLE.name()),
                    applicationTemplate.manager.getPropertyValue(PropertyTypes.LOAD_ERROR_MSG.name()) + dataFilePath.getFileName().toString());
        }
    }

    public void loadData(String dataString) throws Exception {
            processor.processString(dataString);
    }

    @Override
    public void saveData(Path dataFilePath) {
        // NOTE : completing this method was not a part of HW 1. You may have implemented file saving from the
        // confirmation dialog elsewhere in a different way.
        try (PrintWriter writer = new PrintWriter(Files.newOutputStream(dataFilePath))) {
            writer.write(((AppUI) applicationTemplate.getUIComponent()).getCurrentText());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void clear() {
        processor.clear();
        extraLines.clear();
    }

    public void displayData() {
        processor.toChartData(((AppUI) applicationTemplate.getUIComponent()).getChart());
    }
}
