package cs.sysu.algorithm.matcher.rules.stmt;

import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

public class IdenticalMethodBodyMatchRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  IdenticalMethodBodyMatchRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        return measures.getSimMeasure(SimMeasureNames.SAME_METHOD_BODY, eleMappings).getValue() == 1.0;
    }
}
