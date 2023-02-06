package cs.model.algorithm.matcher.measures.stmt;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.StmtElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;

import java.util.List;

/**
 * Mechanism: if surrounding statements are mapped, the two statements are likely to
 * be mapped.
 */
public class StmtSandwichMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        if (typeChecker.isBlock(srcEle.getITreeNode())){
            return 0;
        }

        if (srcEle.isDeclaration())
            return 0;

        List<StmtElement> srcSiblingElements = srcEle.getParentElement().getNearestDescendantStmts();
        List<StmtElement> dstSiblingElements = dstEle.getParentElement().getNearestDescendantStmts();

        // We consider this measure when the stmt have common parent nodes.
        if (!isParentMapping(srcEle, dstEle))
            return 0;

        if (srcSiblingElements.size() == 1 && dstSiblingElements.size() == 1) {
            return 1;
        }
        boolean leftMapped = isLeftStmtMapped(srcEle, dstEle);
        boolean rightMapped = isRightStmtMapped(srcEle, dstEle);
//        System.out.println("Brother " + srcEle + " | " + dstEle + " " + leftMapped + " " + rightMapped);
        if (leftMapped && rightMapped)
            return 1;
        return 0;
    }
}
