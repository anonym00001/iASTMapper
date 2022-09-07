package cs.sysu.algorithm.matcher.rules.stmt;

import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for identical statements.
 *
 * Identical statements can be mapped.
 * But we ignore try statements.
 * For try statements, the descendant statements decide the mapping of the statements.
 */
public class IdenticalStmtMatchRule extends AbstractElementMatchRule implements ElementMatchRule {

    public  IdenticalStmtMatchRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        if (typeChecker.isTryStatement(measures.getSrcEle().getITreeNode()))
            return false;
        if (typeChecker.isBlock(measures.getSrcEle().getITreeNode()))
            return false;
        return measures.getSimMeasure(SimMeasureNames.SAME_STMT, eleMappings).getValue() == 1.0;
    }
}
