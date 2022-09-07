package cs.sysu.algorithm.matcher.measures.token;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

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
