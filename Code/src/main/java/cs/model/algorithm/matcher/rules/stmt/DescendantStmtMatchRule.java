package cs.model.algorithm.matcher.rules.stmt;

import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for statement with descendant statements.
 *
 * For statements with descendant statements, they can be mapped if the ratio
 * of their common descendant statements is larger than a threshold.
 */
public class DescendantStmtMatchRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  DescendantStmtMatchRule(){
        super();
    }

    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        if (typeChecker.isBlock(measures.getSrcEle().getITreeNode()))
            return false;
        return measures.getSimMeasure(SimMeasureNames.DM, eleMappings).getValue() >= stmtDMThreshold;
    }
}
