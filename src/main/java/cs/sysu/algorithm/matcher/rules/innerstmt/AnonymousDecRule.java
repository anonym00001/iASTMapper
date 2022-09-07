package cs.sysu.algorithm.matcher.rules.innerstmt;

import cs.sysu.algorithm.element.InnerStmtElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

public class AnonymousDecRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  AnonymousDecRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        if (typeChecker.isAnonymousClassDec(measures.getSrcEle().getITreeNode())) {
//            System.out.println("AnonymousClassDec is " + measures.getSrcEle());
            InnerStmtElement srcEle = (InnerStmtElement) measures.getSrcEle();
            InnerStmtElement dstEle = (InnerStmtElement) measures.getDstEle();
            return eleMappings.getMappedElement(srcEle.getParentElement()) == dstEle.getParentElement();
        }
        return false;
    }
}
