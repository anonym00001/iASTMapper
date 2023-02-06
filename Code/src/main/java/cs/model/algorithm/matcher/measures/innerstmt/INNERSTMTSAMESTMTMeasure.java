package cs.model.algorithm.matcher.measures.innerstmt;

import cs.model.algorithm.element.InnerStmtElement;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;


/**
 * Mechanism: for two method invocations, assignments or other elements, if their names
 * are mapped and they can can be mapped.
 */
public class INNERSTMTSAMESTMTMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        double val = 0;
        ProgramElement srcStmtEle = srcEle.getStmtElement();
        ProgramElement dstStmtEle = dstEle.getStmtElement();
        if (elementMappings.getDstForSrc(srcStmtEle) == dstStmtEle)
            val = 1;
        return val;
    }
}
