package cs.model.algorithm.matcher.measures.token;

import cs.model.algorithm.element.InnerStmtElement;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.StmtElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.matchers.searchers.CandidateSetsAndMaps;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.ttmap.TokenTypeCalculator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Mechanism: if neighbor token is also mapped, the two tokens are more likely to be mapped.
 */
public class Token_LRBMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        double val = 0;
        TokenElement srcTokenEle = (TokenElement) srcEle;
        TokenElement dstTokenEle = (TokenElement) dstEle;
        List<TokenElement> srcTokenElements = srcTokenEle.getStmtElement().getTokenElements();
        List<TokenElement> dstTokenElements = dstTokenEle.getStmtElement().getTokenElements();

        if (srcTokenElements.size() == 1 && dstTokenElements.size() == 1 && !isParentMapping(srcEle, dstEle))
            return val;

        // Check if node of the token is the statement node.
        // Do not map two tokens with sandwich measure when node of the token is the statement node
        if (srcTokenEle.getStmtElement().getITreeNode() == srcTokenEle.getITreeNode()) {
            if (dstTokenEle.getStmtElement().getITreeNode() == dstTokenEle.getITreeNode()) {
                if (!isParentMapping(srcEle, dstEle)) {
                    return val;
                }
            }
        }

        boolean leftMapped = isLeftMapped(srcTokenEle, dstTokenEle) || fieldAccessLeftMapped(srcTokenEle, dstTokenEle);
        boolean rightMapped = isRightMapped(srcTokenEle, dstTokenEle);
        if(leftMapped && rightMapped)
            return 2.0;


        int srcChildIdx = srcEle.getChildIdx();
        int dstChildIdx = dstEle.getChildIdx();

        if (srcChildIdx > 0 && dstChildIdx > 0){
            ProgramElement srcEle1 = srcTokenElements.get(srcChildIdx - 1);
            ProgramElement dstEle1 = dstTokenElements.get(dstChildIdx - 1);
            if (elementMappings.getDstForSrc(srcEle1) == dstEle1){
                val = 1;
                return val;
            }
        }

        if (srcChildIdx < srcTokenElements.size() - 1 && dstChildIdx < dstTokenElements.size() -1){
            ProgramElement srcEle2 = srcTokenElements.get(srcChildIdx + 1);
            ProgramElement dstEle2 = dstTokenElements.get(dstChildIdx + 1);
            if (elementMappings.getDstForSrc(srcEle2) == dstEle2){
                val = 1;
                return val;
            }
        }
        return val;
    }

    @Override
    public Set<ProgramElement> filterBadDstCandidateElements(ProgramElement srcEle, Set<ProgramElement> dstCandidates,
                                                             CandidateSetsAndMaps candidateSetsAndMaps) {
        if (!srcEle.isFromSrc())
            return null;

        Set<ProgramElement> neighborCandidates = new HashSet<>();
        if (dstCandidates.size() == 0)
            return neighborCandidates;
        TokenElement srcToken = (TokenElement) srcEle;
        StmtElement srcStmt = srcToken.getStmtElement();
        if (elementMappings.isMapped(srcStmt) && srcStmt.getTokenElements().size() == 1) {
            ProgramElement dstStmt = elementMappings.getMappedElement(srcStmt);
            if (dstStmt.getTokenElements().size() == 1) {
                neighborCandidates.add(dstStmt.getTokenElements().get(0));
            }
        }

        ProgramElement leftToken = srcToken.getLeftSibling();
        ProgramElement rightToken = srcToken.getRightSibling();
        if (leftToken != null && elementMappings.isMapped(leftToken)) {
            ProgramElement mappedLeftToken = elementMappings.getMappedElement(leftToken);
            ProgramElement nextToken = mappedLeftToken.getRightSibling();
            if (nextToken != null)
                neighborCandidates.add(nextToken);
        }

        if (rightToken != null && elementMappings.isMapped(rightToken)) {
            ProgramElement mappedRightToken = elementMappings.getMappedElement(rightToken);
            ProgramElement lastToken = mappedRightToken.getLeftSibling();
            if (lastToken != null)
                neighborCandidates.add(lastToken);
        }
        neighborCandidates.retainAll(dstCandidates);
        return neighborCandidates;
    }

    private boolean fieldAccessLeftMapped(TokenElement srcTokenEle, TokenElement dstTokenEle) {
        if (srcTokenEle.getTokenType().equals(TokenTypeCalculator.VAR_NAME)) {
            InnerStmtElement srcComp = srcTokenEle.getNearestMultiTokenInnerStmtElement();
            InnerStmtElement dstComp = dstTokenEle.getNearestMultiTokenInnerStmtElement();
            boolean srcInFieldAccess = typeChecker.isFieldAccess(srcComp.getITreeNode());
            boolean dstInFieldAccess = typeChecker.isFieldAccess(dstComp.getITreeNode());
            if (srcInFieldAccess && !dstInFieldAccess) {
                TokenElement srcThisToken = srcTokenEle.getStmtElement().getTokenElements().get(srcTokenEle.getChildIdx() - 1);
                return isLeftMapped(srcThisToken, dstTokenEle);
            }

            if (!srcInFieldAccess && dstInFieldAccess) {
                TokenElement dstThisToken = dstTokenEle.getStmtElement().getTokenElements().get(dstTokenEle.getChildIdx() - 1);
                return isLeftMapped(srcTokenEle, dstThisToken);
            }
        }
        return false;
    }
    private boolean isLeftMapped(TokenElement srcTokenEle, TokenElement dstTokenEle) {
        int srcChildIdx = srcTokenEle.getChildIdx();
        int dstChildIdx = dstTokenEle.getChildIdx();
        if (srcChildIdx == 0 && dstChildIdx == 0)
            return true;
        if (srcChildIdx > 0 && dstChildIdx > 0) {
            List<TokenElement> srcTokenElements = srcTokenEle.getStmtElement().getTokenElements();
            List<TokenElement> dstTokenElements = dstTokenEle.getStmtElement().getTokenElements();
            ProgramElement srcEle1 = srcTokenElements.get(srcChildIdx - 1);
            ProgramElement dstEle1 = dstTokenElements.get(dstChildIdx - 1);
            if (elementMappings.getMappedElement(srcEle1) == dstEle1)
                return true;
        }
        if (srcTokenEle.isVarName() || srcTokenEle.isLiteral() || isParentMapping(srcTokenEle, dstTokenEle)) {
            InnerStmtElement srcInnerStmtEle = srcTokenEle.getNearestMultiTokenInnerStmtElement();
            InnerStmtElement dstInnerStmtEle = dstTokenEle.getNearestMultiTokenInnerStmtElement();
            if (srcInnerStmtEle != null && dstInnerStmtEle != null &&
                    !srcInnerStmtEle.isNullElement() && !dstInnerStmtEle.isNullElement()) {
                if (srcInnerStmtEle.getTokenElements().get(0) == srcTokenEle)
                    if (dstInnerStmtEle.getTokenElements().get(0) == dstTokenEle)
                        return true;
            }
        }
        return false;
    }

    private boolean isRightMapped(TokenElement srcTokenEle, TokenElement dstTokenEle) {
        int srcChildIdx = srcTokenEle.getChildIdx();
        int dstChildIdx = dstTokenEle.getChildIdx();
        List<TokenElement> srcTokenElements = srcTokenEle.getStmtElement().getTokenElements();
        List<TokenElement> dstTokenElements = dstTokenEle.getStmtElement().getTokenElements();
        if (srcChildIdx  == srcTokenElements.size() - 1 && dstChildIdx == dstTokenElements.size() - 1)
            return true;
        if (srcChildIdx < srcTokenElements.size() - 1 && dstChildIdx < dstTokenElements.size() - 1) {
            ProgramElement srcEle2 = srcTokenElements.get(srcChildIdx + 1);
            ProgramElement dstEle2 = dstTokenElements.get(dstChildIdx + 1);
            if (elementMappings.getDstForSrc(srcEle2) == dstEle2)
                return true;
        }
        if (srcTokenEle.isVarName() || srcTokenEle.isLiteral() || isParentMapping(srcTokenEle, dstTokenEle)) {
            InnerStmtElement srcCE = srcTokenEle.getNearestMultiTokenInnerStmtElement();
            InnerStmtElement dstCE = dstTokenEle.getNearestMultiTokenInnerStmtElement();
            if (srcCE != null && dstCE != null && !srcCE.isNullElement() && !dstCE.isNullElement()) {
                List<TokenElement> srcTokenElements2 = srcCE.getTokenElements();
                List<TokenElement> dstTokenElements2 = dstCE.getTokenElements();
                if (srcTokenElements2.get(srcTokenElements2.size() - 1) == srcTokenEle)
                    if (dstTokenElements2.get(dstTokenElements2.size() - 1) == dstTokenEle)
                        return true;
            }
        }
        return false;
    }
}
