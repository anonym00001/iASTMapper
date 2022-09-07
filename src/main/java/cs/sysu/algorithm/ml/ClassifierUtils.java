package cs.sysu.algorithm.ml;

import weka.classifiers.Classifier;
import weka.classifiers.misc.IsolationForest;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class ClassifierUtils {
    public static final String CLASSIFIER_J48 = "J48";
    public static final String CLASSIFIER_IF = "IF";

    private static Classifier getClassifier(String classifierName){
        switch (classifierName) {
            case CLASSIFIER_J48:
                return new J48();
            case CLASSIFIER_IF:
                return new IsolationForest();
            default:
                throw new RuntimeException("Not supported classifier");
        }
    }

    /**
     * load data from an arff file and set the class index
     * @param filePath An arff file path
     * @param classIndex the index of the attr that denotes class
     * @return The instances data from the file
     */
    public static Instances loadDataFromDisk(String filePath, int classIndex) throws Exception {
        Instances instances = new ConverterUtils.DataSource(filePath).getDataSet();
        if (classIndex != -1)
            instances.setClassIndex(classIndex);
        return instances;
    }

    /**
     * Get a classifier object given classifier name, and build the classifier based on given training data
     * @param instances the training data
     * @param classifierName classifier name
     * @return a new classifier object
     */
    public static Classifier buildClassifier(Instances instances, String classifierName) throws Exception {
        Classifier cla = getClassifier(classifierName);
        cla.buildClassifier(instances);
        return cla;
    }

    /**
     * Using a classifier to determine if an instance is positive
     * @param inst the instance to test
     * @param cla the classifier
     * @param positiveVal the value for the postive class
     * @return if an instance is positive
     */
    public static boolean classify(Instance inst, Classifier cla, double positiveVal) throws Exception {
        return cla.classifyInstance(inst) == positiveVal;
    }
}
