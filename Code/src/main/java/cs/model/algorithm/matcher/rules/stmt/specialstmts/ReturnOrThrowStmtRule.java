package cs.model.algorithm.matcher.rules.stmt.specialstmts;

import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

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
