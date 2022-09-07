package cs.sysu.algorithm.matcher.rules.token;

import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

public class NameTokenOfStmtRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  NameTokenOfStmtRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        SimMeasure measure = measures.getSimMeasure(SimMeasureNames.STMT_NAME_TOKEN, eleMappings);
        return measure.getValue() == 1.0;
    }
}
