package cs.sysu.algorithm.matcher.rules.stmt;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

public class StmtTokenDiceCoefficientRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  StmtTokenDiceCoefficientRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        ProgramElement srcElement = measures.getSrcEle();
        if (typeChecker.isPackageDec(srcElement.getITreeNode()))
            return true;

        double dice = measures.getSimMeasure(SimMeasureNames.DICE, eleMappings).getValue();
        if (typeChecker.isImportDec(srcElement.getITreeNode()))
            return dice >= stmtDiceThreshold0;
//            return dice == 1;

        if (isParentMapping(measures, eleMappings))
            return dice >= stmtDiceThreshold1;
        else
            return dice >= stmtDiceThreshold2;
    }

    private boolean isMethodMapping(ElementSimMeasures measures, ElementMappings eleMappings) {
        ProgramElement srcEle = measures.getSrcEle();
        ProgramElement dstEle = measures.getDstEle();
        ProgramElement srcMethodEle = ((StmtElement) srcEle).getMethodOfElement();
        ProgramElement dstMethodEle = ((StmtElement) dstEle).getMethodOfElement();
        if (srcMethodEle == null || dstMethodEle == null)
            return false;
        return eleMappings.getMappedElement(srcMethodEle) == dstMethodEle;
    }
}
