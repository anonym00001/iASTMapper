package cs.sysu.algorithm.matcher.measures.innerstmt;

import cs.sysu.algorithm.element.InnerStmtElement;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

public class MethodInvocationSameNameMeasure extends AbstractSimMeasure implements SimMeasure {
    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        InnerStmtElement srcInnerEle = (InnerStmtElement) srcEle;
        InnerStmtElement dstInnerEle = (InnerStmtElement) dstEle;
        if (typeChecker.isMethodInvocation(srcInnerEle.getITreeNode())) {
            ProgramElement srcNameToken = srcInnerEle.getNameToken();
            ProgramElement dstNameToken = dstInnerEle.getNameToken();
            if (srcNameToken != null && dstNameToken != null && srcNameToken.equalValue(dstNameToken))
                return 1;
        }

        return 0;
    }
}
