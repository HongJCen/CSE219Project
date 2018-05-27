package dataprocessors;

import javafx.geometry.Point2D;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class TSDProcessorTest {

    private AtomicInteger instances = new AtomicInteger();
    private Map<String, String> dataLabels = new HashMap<>();
    private Map<String, Point2D> dataPoints = new HashMap<>();
    private String textArea;

    public void processString(String tsdString) throws Exception {
        ArrayList<String> names = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();
        AtomicBoolean hadAnError = new AtomicBoolean(false);
        StringBuilder errorMessage = new StringBuilder();
        instances.set(0);

        Stream.of(tsdString.split("\n"))
                .map(line -> Arrays.asList(line.split("\t")))
                .forEach(list -> {
                    try {
                        counter.getAndIncrement();
                        String name = checkedname(list.get(0));
                        for (String n : names)
                            if (n.equals(name)) {
                                Exception e = new Exception("There are duplicate names.");
                                throw e;
                            }
                        names.add(name);
                        String label = list.get(1);
                        instances.incrementAndGet();
                        String[] pair = list.get(2).split(",");
                        Point2D point = new Point2D(Double.parseDouble(pair[0]), Double.parseDouble(pair[1]));
                        dataLabels.put(name, label);
                        dataPoints.put(name, point);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        errorMessage.setLength(0);
                        errorMessage.append("There is a missing number.");
                        errorMessage.append("\nThere is an error on line: ");
                        errorMessage.append(counter.get());
                        hadAnError.set(true);
                    } catch (Exception e) {
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

    private String checkedname(String name) throws TSDProcessor.InvalidDataNameException {
        if (!name.startsWith("@"))
            throw new TSDProcessor.InvalidDataNameException(name);
        return name;
    }

    /**
     * Testing a random point
     *
     * @throws Exception
     *          if the string is invalid
     */
    @Test
    public void singleInstanceTest() throws Exception {
        processString("@a\tlabel\t1,1");
        assertEquals(dataPoints.size(), 1);
    }

    /**
     * Testing for when Integer is maximum. Boundary Value b/c integer max can lead to unexpected errors since we are using
     * an AtomicInteger variable to store the point values
     *
     * @throws Exception
     */
    @Test
    public void singleInstanceMaxTest() throws Exception {
        processString("@a\tlabel\t" + Integer.MAX_VALUE + "," + Integer.MAX_VALUE);             //Integer Max because we want to test to work for the largest possible integer
        assertEquals(dataPoints.size(), 1);
    }

    /**
     * Testing for when Integer is minimum. Boundary Value b/c integer min can lead to unexpected errors since we are using
     * an AtomicInteger variable to store the point values
     *
     * @throws Exception
     */
    @Test
    public void singleInstanceMinTest() throws Exception {
        processString("@a\tlabel\t" + Integer.MIN_VALUE + "," + Integer.MIN_VALUE);             //Integer Min because we want to test to work for smallest possible integer
        assertEquals(dataPoints.size(), 1);
    }

    /**
     * Testing to see if processString throws an exception if the data is invalid
     *
     * @throws Exception
     *          If data is invalid
     */
    @Test(expected = Exception.class)
    public void instanceError() throws Exception {
        processString("a");
    }

    private void saveData(Path dataFilePath) throws IOException {
        BufferedWriter bw = Files.newBufferedWriter(dataFilePath);
        bw.write(textArea);
        bw.close();
    }

    /**
     * Test to see if the text inside the file is the same as the text inside the text area if text area is valid.
     *
     * @throws Exception
     *          If there is an error in reading or writing data or if the textArea is invalid.
     */
    @Test
    public void saveTest() throws Exception {
        File file = new File("test.tsd");
        textArea = "@a\tlabel\t1,1\n@b\tlabel\t2,2";
        processString(textArea);
        saveData(file.toPath());
        String s = "";
        String temp;
        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((temp = br.readLine()) != null)
            s += temp + '\n';
        if (s.substring(s.length() - 1).equals("\n"))
            s = s.substring(0, s.length() - 1);
        assertEquals(textArea, s);
    }

    /**
     * If the text area has invalid data, the file does not save and throws an Exception.
     *
     * @throws Exception
     *          If there is an error in reading or writing data or if the textArea is invalid.
     */
    @Test (expected = Exception.class)
    public void saveErrorTest() throws Exception {
        File file = new File("test.tsd");
        textArea = "@a";
        processString(textArea);
        saveData(file.toPath());
        String s = "";
        String temp;
        BufferedReader br = new BufferedReader(new FileReader(file));
        while ((temp = br.readLine()) != null)
            s += temp + '\n';
        if (s.substring(s.length() - 1).equals("\n"))
            s = s.substring(0, s.length() - 1);
    }
}