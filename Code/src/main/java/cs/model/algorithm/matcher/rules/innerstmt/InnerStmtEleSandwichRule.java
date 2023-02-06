package cs.model.algorithm.matcher.rules.innerstmt;

import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

public class InnerStmtEleSandwichRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  InnerStmtEleSandwichRule(){
        super();
    }

    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        return measures.getSimMeasure(SimMeasureNames.INNER_STMT_ELE_SANDWICH, eleMappings).getValue() == 1.0;
    }
}
