package cs.model.algorithm.matcher.measures.token;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;

public class TokenRenameMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        if (elementMappings.isTokenRenamed(srcEle, dstEle))
            return 1;
        return 0;
    }
}
