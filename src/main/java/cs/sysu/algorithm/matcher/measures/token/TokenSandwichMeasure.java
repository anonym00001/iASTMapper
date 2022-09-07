package cs.sysu.algorithm.matcher.measures.token;

import cs.sysu.algorithm.element.InnerStmtElement;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.matcher.matchers.searchers.CandidateSetsAndMaps;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;
import cs.sysu.algorithm.ttmap.TokenTypeCalculator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mechanism: if surrounding tokens of a token are mapped, the token is likely to be mapped together.
 *
 * We check the surrounding tokens in the nearest multi-token element of the token.
 */
public class TokenSandwichMeasure extends AbstractSimMeasure implements SimMeasure {

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

        // If left tokens are not mapped, do not further calculate the measure
        if (!leftMapped)
            return 0;

        boolean rightMapped = isRightMapped(srcTokenEle, dstTokenEle);
        val = rightMapped ? 1 : 0;
        return val;
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

    @Override
    public Set<ProgramElement> filterBadDstCandidateElements(ProgramElement srcEle, Set<ProgramElement> dstCandidates,
                                                             CandidateSetsAndMaps candidateSetsAndMaps) {
        if (!srcEle.isFromSrc())
            return null;

        SimMeasure neighborMeasure = new TokenNeighborMeasure();
        neighborMeasure.setElementMappings(elementMappings);
        Set<ProgramElement> neighborDstTokens = neighborMeasure
                .filterBadDstCandidateElements(srcEle, dstCandidates, candidateSetsAndMaps);
        Set<ProgramElement> ret = new HashSet<>();

        if (neighborDstTokens.size() == 0)
            return ret;

        TokenElement srcToken = (TokenElement) srcEle;
        for (ProgramElement dstToken : neighborDstTokens) {
            SimMeasure measure = new TokenSandwichMeasure();
            measure.setElementMappings(elementMappings);
            measure.calSimMeasure(srcToken, dstToken);
            if (measure.getValue() == 1.0)
                ret.add(dstToken);
        }
        return ret;
    }
}
