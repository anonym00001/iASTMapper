package cs.sysu.algorithm.matcher.rules.innerstmt;

import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

public class MethodInvocationSameNameRule extends AbstractElementMatchRule implements ElementMatchRule {

    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        return measures.getSimMeasure(SimMeasureNames.METHOD_INVOCATION_SAME_NAME, eleMappings).getValue() == 1;
    }
}
