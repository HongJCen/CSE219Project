package ui;

import classification.RandomClassifier;
import clustering.KMeansClusterer;
import clustering.RandomClusterer;
import data.DataSet;
import org.junit.Test;

import static org.junit.Assert.*;

public class ConfigTest {

    private Config config;

    private void createConfig(String iteration, String updateInterval, String labels, boolean cont) throws Exception {
        config = new Config();
        if (Integer.parseInt(iteration) <= 0)
            throw new Exception();
        if (Integer.parseInt(updateInterval) <= 0)
            throw new Exception();
        config.setIterations(Integer.parseInt(iteration));
        config.setUpdateInterval(Integer.parseInt(updateInterval));
        config.setLabels(Integer.parseInt(labels));
        config.setCont(cont);
    }

    /**
     * Testing for boundary values for iteration and update interval. Using a string for input because the data is retrieved from the user
     * as a String. Using the value 1 for maxIterations and updateInterval because any value less than 1 is invalid. labels is set to 0 because
     * the value is not used in RandomClassifier, so the value of labels does not matter. Any value can be used for labels in this case.
     * Continuous is set to true because continuous can only be either true or false. Either value cannot cause errors.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void randomClassificationMaxIterationTest() throws Exception {
        createConfig("1", "1", "0", true);
        RandomClassifier test = new RandomClassifier(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.isCont());
        assertNotNull(test);
    }
    /**
     * Testing for boundary values for iteration and update interval. Using a string for input because the data is retrieved from the user
     * as a String. 10 is a random value used for maxIterations. 10 is a good number to test updateInterval with b/c it's not a boundary value.
     * Using the value 1 for updateInterval because any value less than 1 is invalid. labels is set to 0 because
     * the value is not used in RandomClassifier, so the value of labels does not matter. Any value can be used for labels in this case.
     * Continuous is set to true because continuous can only be either true or false. Either value cannot cause errors.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void randomClassificationUpdateIntervalTest() throws Exception {
        createConfig("10", "1", "0", true);
        RandomClassifier test = new RandomClassifier(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.isCont());
        assertNotNull(test);
    }

    /**
     * Testing the other boolean of continuous with valid non-boundary values for max iterations and update interval.
     * Continuous as true or false should not affect the validity of the algorithm.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void randomClassificationContTest() throws Exception {
        createConfig("10", "2", "0", false);
        RandomClassifier test = new RandomClassifier(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.isCont());
        assertNotNull(test);
    }

    /**
     * Testing for the boundary value of max iterations. 1 is a boundary value because any number less than 1 is invalid.
     * Labels is set to 3 because it is not a boundary value so max iteration can be tested by itself.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void KMeansClustererMaxIterationTest() throws Exception {
        createConfig("1", "1", "3", true);
        KMeansClusterer test = new KMeansClusterer(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.getLabels(), config.isCont());
        assertNotNull(test);
    }

    /**
     * Testing for the boundary value of update interval. 1 is a boundary value because any number less than 1 is invalid.
     * Labels is set to 3 because it is not a boundary value so update interval can be tested by itself. 10 is used for
     * iterations because it is not a boundary value.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void KMeansClustererUpdateIntervalTest() throws Exception {
        createConfig("10", "1", "3", true);
        KMeansClusterer test = new KMeansClusterer(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.getLabels(), config.isCont());
        assertNotNull(test);
    }

    /**
     * Testing for the value of the number of clusters. The reason Integer.MIN_VALUE is used is because in the constructor of the
     * Clusterer class. If the value of clusters is less than 2, then the value is defaulted to 2. As a result, the boundary value
     * would be integer minimum and integer maximum because the edge values of integer must be tested. The Clusterer constructor
     * uses an int input.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void KMeansClustererClustersTest() throws Exception {
        createConfig("10", "2", String.valueOf(Integer.MIN_VALUE), true);
        KMeansClusterer test = new KMeansClusterer(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.getLabels(), config.isCont());
        assertNotNull(test);
    }

    /**
     * Testing the other boolean of continuous with valid non-boundary values for max iterations and update interval.
     * Continuous as true or false should not affect the validity of the algorithm.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void KMeansClustererContTest() throws Exception {
        createConfig("10", "2", "3", false);
        KMeansClusterer test = new KMeansClusterer(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.getLabels(), config.isCont());
        assertNotNull(test);
    }

    /**
     * Testing for the boundary value of max iterations. 1 is a boundary value because any number less than 1 is invalid.
     * Labels is set to 3 because it is not a boundary value so max iteration can be tested by itself.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void RandomClustererMaxIterationTest() throws Exception {
        createConfig("1", "1", "3", true);
        RandomClusterer test = new RandomClusterer(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.getLabels(), config.isCont());
        assertNotNull(test);
    }

    /**
     * Testing for the boundary value of update interval. 1 is a boundary value because any number less than 1 is invalid.
     * Labels is set to 3 because it is not a boundary value so update interval can be tested by itself. 10 is used for
     * iterations because it is not a boundary value.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void RandomClustererUpdateIntervalTest() throws Exception {
        createConfig("10", "1", "3", true);
        RandomClusterer test = new RandomClusterer(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.getLabels(), config.isCont());
        assertNotNull(test);
    }

    /**
     * Testing for the value of the number of clusters. The reason Integer.MIN_VALUE is used is because in the constructor of the
     * Clusterer class. If the value of clusters is less than 2, then the value is defaulted to 2. As a result, the boundary value
     * would be integer minimum and integer maximum because the edge values of integer must be tested. The Clusterer constructor
     * uses an int input.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void RandomClustererClustersTest() throws Exception {
        createConfig("10", "2", String.valueOf(Integer.MIN_VALUE), true);
        RandomClusterer test = new RandomClusterer(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.getLabels(), config.isCont());
        assertNotNull(test);
    }

    /**
     * Testing the other boolean of continuous with valid non-boundary values for max iterations and update interval.
     * Continuous as true or false should not affect the validity of the algorithm.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test
    public void RandomClustererContTest() throws Exception {
        createConfig("10", "2", "3", false);
        RandomClusterer test = new RandomClusterer(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.getLabels(), config.isCont());
        assertNotNull(test);
    }

    /**
     * Testing for case where all the config is set to 0 and continuous is true. These are the default value for the Config
     * object inside of AppUI. This should throw an Exception because maxIterations cannot be less than or equal to 1
     * and updateInterval cannot be less than or equal to 1. In AppUI, the values of config are checked before the RandomClassifier
     * is constructed. In AppUI, if any values are invalid, the RandomClassifier will not be made and an Error Dialog will show.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test (expected = Exception.class)
    public void InvalidRandomClassificationTest() throws Exception {
        createConfig("0", "0","0", true);
        RandomClassifier test = new RandomClassifier(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.isCont());
    }

    /**
     * Testing for case where all the config is set to 0 and continuous is true. These are the default value for the Config
     * object inside of AppUI. This should throw an Exception because maxIterations cannot be less than or equal to 1
     * and updateInterval cannot be less than or equal to 1. In AppUI, the values of config are checked before the KMeansClusterer
     * is constructed. In AppUI, if any values are invalid, the KMeansClusterer will not be made and an Error Dialog will show.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test (expected = Exception.class)
    public void InvalidKMeansClustererTest() throws Exception {
        createConfig("0", "0","0", true);
        KMeansClusterer test = new KMeansClusterer(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.getLabels(),config.isCont());
    }
    /**
     * Testing for case where all the config is set to 0 and continuous is true. These are the default value for the Config
     * object inside of AppUI. This should throw an Exception because maxIterations cannot be less than or equal to 1
     * and updateInterval cannot be less than or equal to 1. In AppUI, the values of config are checked before the RandomClusterer
     * is constructed. In AppUI, if any values are invalid, the RandomClusterer will not be made and an Error Dialog will show.
     *
     * @throws Exception
     *          Throws an Exception if any input is not an integer or a negative for iteration and updateInterval
     */
    @Test (expected = Exception.class)
    public void InvalidRandomClustererTest() throws Exception {
        createConfig("0", "0","0", true);
        RandomClusterer test = new RandomClusterer(new DataSet(), config.getIterations(), config.getUpdateInterval(), config.getLabels(), config.isCont());
    }
}