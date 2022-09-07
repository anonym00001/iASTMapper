package cs.sysu.algorithm.matcher.measures.token;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

public class TokenRenameMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        if (elementMappings.isTokenRenamed(srcEle, dstEle))
            return 1;
        return 0;
    }
}
