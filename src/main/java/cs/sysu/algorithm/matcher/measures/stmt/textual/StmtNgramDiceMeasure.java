package cs.sysu.algorithm.matcher.measures.stmt.textual;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

import java.util.HashSet;
import java.util.Set;

/**
 * Mechanism: n-gram dice coefficient similarity of two strings.
 *
 * We consider the tokens in each statement.
 * For each token, we calculate the n-grams.
 * We calculate a union set for the tokens in each statement.
 * Furthermore, we only consider n-grams.
 */
public class StmtNgramDiceMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        double val = 0;
        Set<String> srcNgramSet = new HashSet<>(((StmtElement)srcEle).getNgramsOfNameAndLiteral(ngramSize, calculator));
        Set<String> dstNgramSet = ((StmtElement) dstEle).getNgramsOfNameAndLiteral(ngramSize, calculator);

        int srcNum = srcNgramSet.size();
        int dstNum = dstNgramSet.size();
        srcNgramSet.retainAll(dstNgramSet);
        if (srcNgramSet.size() > 0)
            val = 2.0 * srcNgramSet.size() / (srcNum + dstNum);
        return val;
    }
}
