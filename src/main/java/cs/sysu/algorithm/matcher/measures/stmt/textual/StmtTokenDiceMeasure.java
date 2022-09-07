package cs.sysu.algorithm.matcher.measures.stmt.textual;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

/**
 * Mechanism: statements sharing more identical tokens are more likely to be mapped.
 */
public class StmtTokenDiceMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        StmtElement srcStmtEle = (StmtElement) srcEle;
        StmtElement dstStmtEle = (StmtElement) dstEle;
        int srcTokenNum = srcStmtEle.getNameAndLiteralNum();
        int dstTokenNum = dstStmtEle.getNameAndLiteralNum();
//        System.out.println("Src dice is " + srcStmtEle + " " + srcTokenNum + "   dst is " + dstStmtEle + " " + dstTokenNum);
        if (srcTokenNum == 0 || dstTokenNum == 0)
            return 0;
//        System.out.println("Src dice is " + srcStmtEle + " " + srcTokenNum + "   dst is " + dstStmtEle + " " + dstTokenNum);
        SimMeasure measure = new StmtIdenticalTokenMeasure();
        measure.setElementMappings(elementMappings);
        measure.calSimMeasure(srcEle, dstEle);
        double val = measure.getValue();
//        System.out.println(2.0 * val / (srcTokenNum + dstTokenNum));
        return 2.0 * val / (srcTokenNum + dstTokenNum);
    }
}
