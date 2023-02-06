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
public class TokenNeighborMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        int srcChildIdx = srcEle.getChildIdx();
        int dstChildIdx = dstEle.getChildIdx();
        List<TokenElement> srcTokenElements = srcEle.getStmtElement().getTokenElements();
        List<TokenElement> dstTokenElements = dstEle.getStmtElement().getTokenElements();

        double val = 0;
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
}
