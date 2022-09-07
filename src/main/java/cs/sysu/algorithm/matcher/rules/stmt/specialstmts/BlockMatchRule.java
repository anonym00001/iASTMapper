package cs.sysu.algorithm.matcher.rules.stmt.specialstmts;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for block statement.
 *
 * For two blocks, they can only be mapped if their parent elements are mapped.
 */
public class BlockMatchRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  BlockMatchRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        ProgramElement srcElement = measures.getSrcEle();
        if (!typeChecker.isBlock(srcElement.getITreeNode()))
            return false;
        return isParentMapping(measures, eleMappings);
    }
}
