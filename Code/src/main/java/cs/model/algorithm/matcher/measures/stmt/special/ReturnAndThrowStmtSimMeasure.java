package cs.model.algorithm.matcher.measures.stmt.special;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.StmtElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;

public class ReturnAndThrowStmtSimMeasure extends AbstractSimMeasure implements SimMeasure {
    private boolean isReturnOrThrowStatement(ProgramElement element) {
        return typeChecker.isReturnStatement(element.getITreeNode()) ||
                typeChecker.isThrowStatement(element.getITreeNode());
    }

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        if (!isReturnOrThrowStatement(srcEle))
            return 0;

//        System.out.println("Src  " + srcEle + " " + " Dst  " + dstEle);
//        System.out.println("Src Method is " + srcMethod + " Dst method is " + dstMethod);
        ProgramElement srcMethod = ((StmtElement) srcEle).getMethodOfElement();
        ProgramElement dstMethod = ((StmtElement) dstEle).getMethodOfElement();
        if (elementMappings.getDstForSrc(srcMethod) == dstMethod)
            return 1.0;
        return 0;
    }
}
