package cs.model.algorithm.matcher.measures.token;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;

/**
 * Mechanism: only tokens with the same type can be mapped.
 * For example, variable name cannot be mapped to declaration name.
 */
public class TokenTypeMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        double val = 0;
        TokenElement srcToken = (TokenElement) srcEle;
        TokenElement dstToken = (TokenElement) dstEle;
        if (srcToken.getTokenType() == null || dstToken.getTokenType() == null)
            return val;
        if (srcToken.getTokenType().equals(dstToken.getTokenType()))
            val = 1;
        return val;
    }
}
