package cs.sysu.algorithm.matcher.rules.stmt;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for method.
 *
 * Methods with the same name can be mapped.
 */
public class MethodNameMatchRule extends AbstractElementMatchRule implements ElementMatchRule {

    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        ProgramElement srcElement = measures.getSrcEle();
        if (!((StmtElement) srcElement).isMethodDec())
            return false;
        return isWithSameName(measures, eleMappings);
    }
}
