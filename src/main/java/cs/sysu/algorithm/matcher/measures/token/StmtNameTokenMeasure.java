package cs.sysu.algorithm.matcher.measures.token;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

public class StmtNameTokenMeasure extends AbstractSimMeasure implements SimMeasure {
    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        ProgramElement srcStmtEle = srcEle.getStmtElement();
        ProgramElement dstStmtEle = dstEle.getStmtElement();
        if (elementMappings.isMapped(srcStmtEle)) {
            if (elementMappings.getMappedElement(srcStmtEle) == dstStmtEle) {
                TokenElement srcNameToken = ((StmtElement) srcStmtEle).getNameToken();
                TokenElement dstNameToken = ((StmtElement) dstStmtEle).getNameToken();
//                System.out.println("Src " + srcEle + " " + dstEle);
//                System.out.println("Name token " + srcNameToken + " " + dstNameToken);
                if (srcEle == srcNameToken && dstEle == dstNameToken)
                    return 1;
            }
        }
        return 0;
    }
}
