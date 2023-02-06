package cs.model.algorithm.matcher.measures.stmt;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;

import java.util.List;

/**
 * Mechanism: stmt with identical content can be mapped.
 */
public class IdenticalStmtMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        if (typeChecker.isBlock(srcEle.getITreeNode()))
            return 0;
//        System.out.println("Src identical " + srcEle + " dst identical " + dstEle + " isRename " + isIdenticalWithRename(srcEle, dstEle) + "  isSignature  " + isWithSameMethodSignature(srcEle, dstEle));
        if (isIdenticalWithRename(srcEle, dstEle))
            return 1;
        if (isWithSameMethodSignature(srcEle, dstEle))
            return 1;
        return 0;
    }

    private boolean isIdenticalWithRename(ProgramElement srcEle, ProgramElement dstEle) {
        List<TokenElement> srcTokenElements = srcEle.getTokenElements();
        List<TokenElement> dstTokenElements = dstEle.getTokenElements();
        if (srcTokenElements.size() != dstTokenElements.size())
            return false;
        for (int i = 0; i < srcTokenElements.size(); i++) {
            TokenElement srcTokenEle = srcTokenElements.get(i);
            TokenElement dstTokenEle = dstTokenElements.get(i);
            if (srcTokenEle.equalValue(dstTokenEle))
                continue;
            if (elementMappings.isTokenRenamed(srcTokenEle, dstTokenEle))
                continue;
            return false;
        }
        return true;
    }

    private boolean isWithSameMethodSignature(ProgramElement srcEle, ProgramElement dstEle) {
        SimMeasure measure = new MethodSignatureMeasure();
        measure.setElementMappings(elementMappings);
        measure.calSimMeasure(srcEle, dstEle);
        return measure.getValue() == 1.0;
    }
}
