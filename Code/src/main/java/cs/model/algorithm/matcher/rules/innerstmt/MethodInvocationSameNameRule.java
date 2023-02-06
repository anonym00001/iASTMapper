package cs.model.algorithm.matcher.rules.innerstmt;

import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

public class MethodInvocationSameNameRule extends AbstractElementMatchRule implements ElementMatchRule {

    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        return measures.getSimMeasure(SimMeasureNames.METHOD_INVOCATION_SAME_NAME, eleMappings).getValue() == 1;
    }
}
