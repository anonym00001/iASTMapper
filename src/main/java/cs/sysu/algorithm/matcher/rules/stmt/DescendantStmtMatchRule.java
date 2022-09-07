package cs.sysu.algorithm.matcher.rules.stmt;

import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

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
