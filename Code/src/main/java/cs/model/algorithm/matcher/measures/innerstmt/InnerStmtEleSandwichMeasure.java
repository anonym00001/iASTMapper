package cs.model.algorithm.matcher.measures.innerstmt;

import cs.model.algorithm.element.InnerStmtElement;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.mappings.ElementMapping;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;


import java.util.List;

public class InnerStmtEleSandwichMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        ProgramElement srcParentEle = srcEle.getParentElement();
        ProgramElement dstParentEle = dstEle.getParentElement();

        if (elementMappings.getMappedElement(srcParentEle) != dstParentEle) {
            return 0;
        }

        if (srcParentEle.getInnerStmtElements().size() == 1 && dstParentEle.getInnerStmtElements().size() == 1) {
            return 1;
        }

        ProgramElement leftSrcInnerEle = srcEle.getLeftSibling();
        ProgramElement leftDstInnerEle = dstEle.getLeftSibling();
        boolean leftMapped = false;
        if (leftSrcInnerEle == null && leftDstInnerEle == null)
            leftMapped = true;
        else if (leftSrcInnerEle != null && leftDstInnerEle != null)
            leftMapped = elementMappings.getMappedElement(leftSrcInnerEle) == leftDstInnerEle;

        if (!leftMapped)
            return 0;

        ProgramElement rightSrcInnerEle = srcEle.getRightSibling();
        ProgramElement rightDstInnerEle = dstEle.getRightSibling();
        boolean rightMapped = false;
        if (rightSrcInnerEle == null && rightDstInnerEle == null)
            rightMapped = true;
        else if (rightSrcInnerEle != null && rightDstInnerEle != null)
            rightMapped = elementMappings.getMappedElement(rightSrcInnerEle) == rightDstInnerEle;
        return rightMapped ? 1 : 0;
    }

    protected boolean tokenSandwich(ProgramElement srcEle, ProgramElement dstEle) {
        InnerStmtElement srcInnerStmtEle = (InnerStmtElement) srcEle;
        InnerStmtElement dstInnerStmtEle = (InnerStmtElement) dstEle;

        List<TokenElement> srcTokenElements = srcInnerStmtEle.getTokenElements();
        List<TokenElement> dstTokenElements = dstInnerStmtEle.getTokenElements();

        if (srcTokenElements.size() == 0 || dstTokenElements.size() == 0)
            return false;

        TokenElement srcFirstToken = srcTokenElements.get(0);
        TokenElement dstFirstToken = dstTokenElements.get(0);
        TokenElement srcLeftSiblingToken = (TokenElement) srcFirstToken.getLeftSibling();
        TokenElement dstLeftSiblingToken = (TokenElement) dstFirstToken.getLeftSibling();
        boolean leftMapped = false;
        if (srcLeftSiblingToken == null && dstLeftSiblingToken == null)
            leftMapped = true;
        else if (srcLeftSiblingToken != null && dstLeftSiblingToken != null)
            leftMapped = elementMappings.getMappedElement(srcLeftSiblingToken) == dstLeftSiblingToken;

        if (!leftMapped)
            return false;

        TokenElement srcLastToken = srcTokenElements.get(srcTokenElements.size() - 1);
        TokenElement dstLastToken = dstTokenElements.get(dstTokenElements.size() - 1);
        TokenElement srcRightSiblingToken = (TokenElement) srcLastToken.getRightSibling();
        TokenElement dstRightSiblingToken = (TokenElement) dstLastToken.getRightSibling();
        boolean rightMapped = false;
        if (srcRightSiblingToken == null && dstRightSiblingToken == null)
            rightMapped = true;
        else if (srcRightSiblingToken != null && dstRightSiblingToken != null)
            rightMapped = elementMappings.getMappedElement(srcRightSiblingToken) == dstRightSiblingToken;

        return rightMapped;
    }
}
