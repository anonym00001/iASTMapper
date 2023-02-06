package cs.model.algorithm.matcher.rules.stmt;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

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
