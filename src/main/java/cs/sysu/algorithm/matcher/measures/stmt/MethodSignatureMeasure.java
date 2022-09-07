package cs.sysu.algorithm.matcher.measures.stmt;

import cs.sysu.algorithm.element.MethodParameterType;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

import java.util.List;

/**
 * Mechanism: if two methods have identical signature, we consider they are mapped.
 */
public class MethodSignatureMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        double val = 0;
        StmtElement srcStmt = (StmtElement) srcEle;
        if (!srcStmt.isMethodDec()){
            return val;
        }
        String srcName = srcEle.getName();
        String dstName = dstEle.getName();

        // If two statements do not have the same name and they are not renamed
        if (!srcName.equals(dstName) && !isWithRename(srcEle, dstEle)) {
            return val;
        }

        List<MethodParameterType> typeList1 = ((StmtElement) srcEle).getMethodTypeList();
        List<MethodParameterType> typeList2 = ((StmtElement) dstEle).getMethodTypeList();
        if (MethodParameterType.isIdenticalMethodParameterTypeList(typeList1, typeList2, elementMappings))
            return 1;
        return 0;
    }
}
