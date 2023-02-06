package cs.model.algorithm.matcher.rules.innerstmt;

import cs.model.algorithm.element.InnerStmtElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

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
