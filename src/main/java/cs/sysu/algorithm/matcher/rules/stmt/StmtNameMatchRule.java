package cs.sysu.algorithm.matcher.rules.stmt;

import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

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
