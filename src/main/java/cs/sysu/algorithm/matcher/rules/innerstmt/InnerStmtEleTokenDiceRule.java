package cs.sysu.algorithm.matcher.rules.innerstmt;

import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

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
