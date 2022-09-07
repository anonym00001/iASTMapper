package cs.sysu.algorithm.matcher.measures.stmt;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

/**
 * Mechanism: statements with mapped parents are more likely to be mapped.
 */
public class StmtParentMappingMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        double val = 0;
        ProgramElement srcParentEle = srcEle.getParentElement();
        ProgramElement dstParentEle = dstEle.getParentElement();
//        System.out.println("Src  " + srcEle + " " + " Dst  " + dstEle);
//        System.out.println("Src pm is " + srcParentEle + " " + " Dst pm is " + dstParentEle + " is " + (elementMappings.getDstForSrc(srcParentEle) == dstParentEle));
        if (!elementMappings.isMapped(srcParentEle) && !elementMappings.isMapped(dstParentEle)){
            return val;
        }
        if (elementMappings.getDstForSrc(srcParentEle) == dstParentEle)
            val = 1;
        return val;
    }
}
