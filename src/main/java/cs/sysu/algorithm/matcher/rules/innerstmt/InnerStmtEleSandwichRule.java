package cs.sysu.algorithm.matcher.rules.innerstmt;

import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

public class InnerStmtEleSandwichRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  InnerStmtEleSandwichRule(){
        super();
    }

    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        return measures.getSimMeasure(SimMeasureNames.INNER_STMT_ELE_SANDWICH, eleMappings).getValue() == 1.0;
    }
}
