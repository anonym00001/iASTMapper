package cs.model.algorithm.matcher.rules.innerstmt;

import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for inner-stmt elements.
 *
 * Determine if two inner-stmt elements can be mapped by checking if
 * token mapping dice coefficient >= threshold.
 */
public class InnerStmtEleTokenDiceRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  InnerStmtEleTokenDiceRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        return measures.getSimMeasure(SimMeasureNames.INNER_STMT_ELE_DICE, eleMappings).getValue() >= InnerStmtEleDiceThreshold;
    }
}
