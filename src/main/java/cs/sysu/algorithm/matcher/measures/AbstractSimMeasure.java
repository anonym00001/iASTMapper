package cs.sysu.algorithm.matcher.measures;

import cs.sysu.algorithm.element.NGramCalculator;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.matchers.searchers.CandidateSetsAndMaps;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Base class of SimMeasure
 */
public abstract class AbstractSimMeasure implements SimMeasure {
    protected static NGramCalculator calculator = new NGramCalculator();

    protected ElementMappings elementMappings;
    protected double value;

    @Override
    public void setElementMappings(ElementMappings elementMappings) {
        this.elementMappings = elementMappings;
    }

    @Override
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public double getValue() {
        return value;
    }

    protected boolean isParentMapping(ProgramElement srcEle, ProgramElement dstEle) {
        ProgramElement srcParentEle = srcEle.getParentElement();
        ProgramElement dstParentEle = dstEle.getParentElement();
        return elementMappings.getDstForSrc(srcParentEle) == dstParentEle;
    }

    @Override
    public void calSimMeasure(ProgramElement srcEle, ProgramElement dstEle) {
        this.value = calMeasureValue(srcEle, dstEle);
    }

    @Override
    public Set<ProgramElement> filterBadDstCandidateElements(ProgramElement srcEle, Set<ProgramElement> dstCandidates,
                                                             CandidateSetsAndMaps candidateSetsAndMaps) {
        return null;
    }

    protected abstract double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle);

    public int compare(SimMeasure measure) {
        return compareMeasureVal(this.getValue(), measure.getValue());
    }

    protected int compareMeasureVal(double val1, double val2) {
        return Double.compare(val1, val2);
    }

    protected boolean isLeftStmtMapped(ProgramElement srcElement, ProgramElement dstElement) {
        List<StmtElement> srcSiblingElements = srcElement.getParentElement().getNearestDescendantStmts();
        List<StmtElement> dstSiblingElements = dstElement.getParentElement().getNearestDescendantStmts();
        boolean leftMapped = false;
        if (srcElement.getChildIdx() == 0 && dstElement.getChildIdx() == 0)
            leftMapped = true;
        else if (srcElement.getChildIdx() > 0 && dstElement.getChildIdx() > 0) {
            ProgramElement srcEle1 = srcSiblingElements.get(srcElement.getChildIdx() - 1);
            ProgramElement dstEle1 = dstSiblingElements.get(dstElement.getChildIdx() - 1);

//            System.out.println("Left Src is " + srcElement + " " + srcEle1 + " Dst is " + dstElement + " " + dstEle1);
            if (elementMappings.getDstForSrc(srcEle1) == dstEle1)
                leftMapped = true;
        }
        return leftMapped;
    }

    protected boolean isRightStmtMapped(ProgramElement srcElement, ProgramElement dstElement) {
        List<StmtElement> srcSiblingElements = srcElement.getParentElement().getNearestDescendantStmts();
        List<StmtElement> dstSiblingElements = dstElement.getParentElement().getNearestDescendantStmts();
        boolean rightMapped = false;
        int srcSiblingSize = srcSiblingElements.size();
        int dstSiblingSize = dstSiblingElements.size();
        if (srcElement.getChildIdx() == srcSiblingSize - 1 && dstElement.getChildIdx() == dstSiblingSize - 1)
            rightMapped = true;
        else if (srcElement.getChildIdx() < srcSiblingSize - 1 && dstElement.getChildIdx() < dstSiblingSize - 1){
            ProgramElement srcEle2 = srcSiblingElements.get(srcElement.getChildIdx() + 1);
            ProgramElement dstEle2 = dstSiblingElements.get(dstElement.getChildIdx() + 1);
//            System.out.println("Right Src is " + srcElement + " " + srcEle2 + " Dst is " + dstElement + " " + dstEle2);
            if (elementMappings.getDstForSrc(srcEle2) == dstEle2)
                rightMapped = true;
        }
        return rightMapped;
    }

    protected boolean isWithRename(ProgramElement srcElement, ProgramElement dstElement) {
        TokenElement srcNameToken = ((StmtElement) srcElement).getNameToken();
        TokenElement dstNameToken = ((StmtElement) dstElement).getNameToken();
//        System.out.println("Name Token is " + srcNameToken + " | " + dstNameToken);
        if (srcNameToken != null && dstNameToken != null) {
            return elementMappings.isTokenRenamed(srcNameToken, dstNameToken);
        }
        return false;
    }
}
