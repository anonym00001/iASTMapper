package cs.sysu.algorithm.matcher.rules.innerstmt;

import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for inner-stmt elements.
 *
 * Inner-stmt elements with names mapped can be mapped.
 */
public class InnerStmtEleNameMappingRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  InnerStmtEleNameMappingRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        return measures.getSimMeasure(SimMeasureNames.INNER_STMT_ELE_NAME, eleMappings).getValue() == 1.0;
    }
}
