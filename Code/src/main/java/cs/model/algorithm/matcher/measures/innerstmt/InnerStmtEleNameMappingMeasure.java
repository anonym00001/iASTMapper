package cs.model.algorithm.matcher.measures.innerstmt;

import cs.model.algorithm.element.InnerStmtElement;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;


/**
 * Mechanism: for two method invocations, assignments or other elements, if their names
 * are mapped and they can can be mapped.
 */
public class InnerStmtEleNameMappingMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        double val = 0;
        InnerStmtElement srcElement = (InnerStmtElement) srcEle;
        InnerStmtElement dstElement = (InnerStmtElement) dstEle;

        ProgramElement srcNameToken = srcElement.getNameToken();
        ProgramElement dstNameToken = dstElement.getNameToken();

//        System.out.println("Src is " + srcEle + " " + dstEle);
//        System.out.println("Inner is " + srcElement + " " + dstElement);
//        System.out.println("Token is " + srcNameToken + " " + dstNameToken);
//        System.out.println("----------------------------------------");

        if (srcNameToken == null || dstNameToken == null)
            return 0;
        if (elementMappings.getMappedElement(srcNameToken) == dstNameToken)
            val = 1;
        return val;
    }
}
