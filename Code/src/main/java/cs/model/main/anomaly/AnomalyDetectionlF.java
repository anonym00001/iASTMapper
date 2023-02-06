package cs.model.main.anomaly;

import cs.model.algorithm.ml.ArffGenerator;
import cs.model.algorithm.ml.AttrMetaInfo;
import cs.model.algorithm.ml.AttrValue;
import cs.model.algorithm.ml.ClassifierUtils;
import cs.model.evaluation.csvrecord.measure.StmtMappingAndMeasureRecord;
import cs.model.main.analysis.StmtMappingSingleAnalysis;
import weka.classifiers.Classifier;
import weka.core.Instances;

import java.util.List;

/**
 * We use Isolation Forest to detect anomalies in the data.
 * We expect that using this method can help us determine if two elements can be mapped.
 * However, the experiments show that isolation forest cannot help us do that.
 *
 * Input: csv data from the single analysis of statement mapping of our method.
 */
public class AnomalyDetectionlF {

    private static String project = "junit4";

    public static void main(String[] args) throws Exception {
        List<String[]> records = StmtMappingSingleAnalysis.getRecords(project);
        List<AttrMetaInfo> infos = StmtMappingAndMeasureRecord.getAttrMetaInfos();
        List<List<AttrValue>> valuesList = StmtMappingAndMeasureRecord.getAttrValuesFromCsv(records);

        ArffGenerator generator = new ArffGenerator(project, infos);
        for (List<AttrValue> values: valuesList) {
            if (needToBeRemoved(values))
                continue;
            generator.addInstance(values);
        }
        Instances data = generator.getInstances();
        data.deleteAttributeAt(0);
        data.setClassIndex(data.numAttributes() - 1);
        Classifier cla = ClassifierUtils.buildClassifier(data, ClassifierUtils.CLASSIFIER_IF);

        for (int i = 0; i < data.size(); i++) {
            double[] score = cla.distributionForInstance(data.instance(i));
            if (score[1] > 0.5)
                System.out.println(data.instance(i));
        }
    }

    private static boolean needToBeRemoved(List<AttrValue> values) {
        for (int i = 0; i < values.size() - 1; i ++) {
            if ((Double) values.get(i).getValue() < 1)
                return false;
        }
        return true;
    }
}
