package cs.model.algorithm.matcher.measures.innerstmt;

import cs.model.algorithm.element.InnerStmtElement;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;

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
