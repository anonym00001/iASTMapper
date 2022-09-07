package cs.sysu.algorithm.matcher.measures.token;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

import java.util.HashSet;
import java.util.Set;

public class TokenNgramMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        String srcToken = srcEle.getStringValue();
        String dstToken = dstEle.getStringValue();
        if (srcToken.equals(dstToken))
            return 1;

        Set<String> srcSet = new HashSet<>(calculator.calculateNGrams(srcToken, ngramSize, true));
        Set<String> dstSet = calculator.calculateNGrams(dstToken, ngramSize, true);

        double srcNum = srcSet.size();
        double dstNum = dstSet.size();
        srcSet.retainAll(dstSet);
        return 2.0 * srcSet.size() /  (srcNum + dstNum);
    }
}
