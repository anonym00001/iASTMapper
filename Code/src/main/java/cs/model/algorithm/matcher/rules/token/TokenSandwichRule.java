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
public class TokenSandwichRule extends AbstractElementMatchRule implements ElementMatchRule {

    public  TokenSandwichRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        SimMeasure measure = measures.getSimMeasure(SimMeasureNames.TOKEN_SANDWICH, eleMappings);
        return measure.getValue() == 1.0;
    }
}