package cs.model.algorithm.matcher.rules.innerstmt;

import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

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
