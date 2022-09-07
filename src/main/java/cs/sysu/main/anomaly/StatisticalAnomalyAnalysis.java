package cs.sysu.main.anomaly;

import org.apache.commons.math3.stat.descriptive.rank.Median;

/**
 * APIs for calculating median and MAD
 */
public class StatisticalAnomalyAnalysis {
    public static double getMedian(double[] values){
        Median m = new Median();
        m.setData(values);
        return m.evaluate();
    }

    public static double getLowerMAD(double[] values){
        double median = getMedian(values);
        double[] tmpValues = new double[values.length];
        for (int i = 0; i < values.length; i++){
            tmpValues[i] = Math.abs(values[i] - median);
        }

        double median2 = getMedian(tmpValues);
        return median - median2;
    }

}
