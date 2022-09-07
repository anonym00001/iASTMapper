package cs.sysu.algorithm.matcher.rules.stmt.specialstmts;

import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for return statements.
 *
 * Return statements can be mapped if they share the same method declaration.
 */
public class ReturnOrThrowStmtRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  ReturnOrThrowStmtRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        return measures.getSimMeasure(SimMeasureNames.RETURN_STMT, eleMappings).getValue() == 1.0;
    }
}
