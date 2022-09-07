package cs.sysu.algorithm.matcher.rules.stmt;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;


/**
 * Mapping rule for statement.
 *
 * Statements with surrounding statements mapped can be mapped.
 */
public class StmtSandwichRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  StmtSandwichRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        ProgramElement srcElement = measures.getSrcEle();
        if (typeChecker.isBlock(srcElement.getITreeNode()))
            return false;
        SimMeasure measure = measures.getSimMeasure(SimMeasureNames.STMT_SANDWICH, eleMappings);
        return measure.getValue() == 1.0;
    }
}
