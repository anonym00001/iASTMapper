package cs.sysu.algorithm.matcher.rules.stmt;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for statement.
 *
 * Statement with similar content (ngram-dice >= threshold) can be mapped.
 */
public class StmtNgramDiceCoefficientRule extends AbstractElementMatchRule implements ElementMatchRule {
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        ProgramElement srcElement = measures.getSrcEle();

        if (typeChecker.isPackageDec(srcElement.getITreeNode()))
            return true;

        if (typeChecker.isImportDec(srcElement.getITreeNode()))
            return false;

        SimMeasure measure = measures.getSimMeasure(SimMeasureNames.NGRAM, eleMappings);
        return measure.getValue() >= ElementMatchRule.stmtNgramThreshold;
    }
}
