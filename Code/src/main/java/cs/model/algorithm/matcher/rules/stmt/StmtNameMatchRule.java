package cs.model.algorithm.matcher.rules.stmt;

import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for statements with name.
 *
 * Statements with the same name
 */
public class StmtNameMatchRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  StmtNameMatchRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        return isWithSameName(measures, eleMappings);
    }
}
