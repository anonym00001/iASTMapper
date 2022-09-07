package cs.sysu.algorithm.matcher.measures.stmt;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

/**
 * Mechanism: statements with the same name are more likely to be mapped.
 */
public class StmtSameNameMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        if (srcEle.getName() == null || dstEle.getName() == null)
            return 0;
//        System.out.println("Src and name " + srcEle + " " + srcEle.getName() + " Dst and name " + dstEle + " " + dstEle.getName());
        boolean equalName = srcEle.getName().equals(dstEle.getName());
        return equalName || isWithRename(srcEle, dstEle) ? 1 : 0;
    }
}
