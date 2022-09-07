package cs.sysu.algorithm.matcher.measures.token;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

/**
 * Mechanism: Tokens in the same scope (a pair of brackets) is more likely to be mapped.
 */
public class TokenScopeMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        double val = 0;
        TokenElement srcTokenEle = (TokenElement) srcEle;
        TokenElement dstTokenEle = (TokenElement) dstEle;

        ProgramElement srcStmt = srcTokenEle.getStmtElement().getParentElement();
        ProgramElement dstStmt = dstTokenEle.getStmtElement().getParentElement();

        if (srcStmt != null && dstStmt != null)
            val = elementMappings.getMappedElement(srcStmt) == dstStmt ? 1 : 0;
        return val;
    }
}
