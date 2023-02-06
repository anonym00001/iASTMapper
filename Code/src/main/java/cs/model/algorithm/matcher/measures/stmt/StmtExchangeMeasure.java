package cs.model.algorithm.matcher.measures.stmt;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.StmtElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;

import java.util.List;

/**
 * Mechanism: mapping statements should avoid exchange with other statements
 */
public class StmtExchangeMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        double val = 0;
//        System.out.println("123");
        if (srcEle.isDeclaration()) {
            val = 0;
            return val;
        }

//        System.out.println("133");
        // We consider this measure when two statement have common parent nodes
        if (!isParentMapping(srcEle, dstEle))
            return val;

//        System.out.println("1234");
        if (elementMappings.isMapped(srcEle) || elementMappings.isMapped(dstEle) )
            return val;

//        System.out.println("12345");
        ProgramElement srcLeftEle = getLeftElement(srcEle);
        ProgramElement dstLeftEle = getLeftElement(dstEle);

//        System.out.println(srcLeftEle + " " + dstLeftEle);
        boolean leftMapping;
        if (srcEle.getChildIdx() == 0 && dstEle.getChildIdx() == 0)
            leftMapping = true;
        else
            leftMapping = isMapped(srcLeftEle, dstLeftEle);

        if (!leftMapping)
            return 0;

//        System.out.println("123456");
        ProgramElement srcRightEle = getRightElement(srcEle, dstEle);
        ProgramElement dstRightEle = getRightElement(dstEle, srcEle);

        boolean rightMapping;
        int srcSiblingSize = srcEle.getParentElement().getNearestDescendantStmts().size();
        int dstSiblingSize = dstEle.getParentElement().getNearestDescendantStmts().size();
        if (srcEle.getChildIdx() == srcSiblingSize - 1 && dstEle.getChildIdx() == dstSiblingSize - 1)
            rightMapping = true;
        else
            rightMapping = isMapped(srcRightEle, dstRightEle);

        if (rightMapping)
            val = 1;
//        System.out.println("1234567");
//        System.out.println("Src " + srcEle + " dst " + dstEle);
        return val;
    }

    private boolean isMapped(ProgramElement srcEle, ProgramElement dstEle) {
        if (srcEle == null || dstEle == null)
            return false;
        return elementMappings.getDstForSrc(srcEle) == dstEle;
    }

    private ProgramElement getLeftElement(ProgramElement element){
        int idx = element.getChildIdx();
        List<StmtElement> stmtElements = element.getParentElement().getNearestDescendantStmts();
        int tmpIdx = idx - 1;
        while (tmpIdx >= 0){
            ProgramElement tmpEle = stmtElements.get(tmpIdx);
            if (elementMappings.isMapped(tmpEle))
                return tmpEle;
            tmpIdx --;
        }
        return null;
    }

    private ProgramElement getRightElement(ProgramElement element, ProgramElement needToCheckEle){
        int idx = element.getChildIdx();
        List<StmtElement> stmtElements = element.getParentElement().getNearestDescendantStmts();
        int tmpIdx = idx + 1;
        while (tmpIdx < stmtElements.size()){
            ProgramElement tmpEle = stmtElements.get(tmpIdx);
            if (elementMappings.isMapped(tmpEle))
                return tmpEle;
            tmpIdx ++;
        }
        return null;
    }
}
