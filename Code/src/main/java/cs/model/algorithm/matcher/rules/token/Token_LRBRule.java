package cs.model.algorithm.matcher.rules.token;


import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

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
