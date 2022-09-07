package cs.sysu.algorithm.matcher.measures.innerstmt;

import cs.sysu.algorithm.element.InnerStmtElement;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

/**
 * Mechanism: inner stmt elements with many tokens mapped are likely to be mapped.
 */
public class InnerStmtEleTokenDiceMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        InnerStmtElement srcElement = (InnerStmtElement) srcEle;
        InnerStmtElement dstElement = (InnerStmtElement) dstEle;
        return InnerStmtElement.getDiceForMappedTokens(elementMappings, srcElement, dstElement);
    }
}
