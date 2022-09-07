package cs.sysu.main.anomaly;

import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.evaluation.csvrecord.measure.StmtMappingAndMeasureRecord;
import cs.sysu.main.analysis.StmtMappingSingleAnalysis;

import java.util.ArrayList;
import java.util.List;

/**
 * Use a statistical method: upper MAD to find the threshold of different measures.
 *
 * Input: csv data from the single analysis of statement mapping of our method.
 */
public class AnomalyDetectionMAD {
    private static String project = "junit4";

    public static void main(String[] args) throws Exception {
        List<String[]> records = StmtMappingSingleAnalysis.getRecords(project);
        List<Double> allValues = new ArrayList<>();

        // Get measure vector for each mapped statement pair
        for (String[] record: records) {
            StmtMappingAndMeasureRecord resultRecord = new StmtMappingAndMeasureRecord(record);
            if (resultRecord.stmtHasNoToken())
                continue;
            double value = resultRecord.getMeasureValue(SimMeasureNames.NGRAM);
            if (value == 1.0)
                continue;
            if (value == 0.0)
                continue;
            allValues.add(value);
        }

        double[] values = new double[allValues.size()];
        for (int i = 0; i < allValues.size(); i++)
            values[i] = allValues.get(i);
        System.out.println(allValues);

        System.out.println(StatisticalAnomalyAnalysis.getMedian(values));
        System.out.println(StatisticalAnomalyAnalysis.getLowerMAD(values));
    }
}
