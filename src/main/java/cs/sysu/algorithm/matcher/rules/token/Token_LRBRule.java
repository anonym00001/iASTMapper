package cs.sysu.algorithm.matcher.rules.token;


import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for token.
 *
 * Tokens with surrounding tokens mapped are likely to be mapped.
 */
public class Token_LRBRule extends AbstractElementMatchRule implements ElementMatchRule {

    public  Token_LRBRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        SimMeasure measure1 = measures.getSimMeasure(SimMeasureNames.TOKEN_SANDWICH, eleMappings);
        SimMeasure measure2 = measures.getSimMeasure(SimMeasureNames.TOKEN_NEIGHBOR, eleMappings);
        return measure1.getValue() == 1.0;
    }
}
