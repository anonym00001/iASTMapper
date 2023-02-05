package cs.sysu.algorithm.matcher.matchers.searchers;

import cs.sysu.algorithm.element.*;
import cs.sysu.algorithm.element.InnerStmtElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.rules.ElementMatchDeterminer;

import java.util.*;

/**
 * The class for searching candidates for a given
 * source statement, inner-stmt element or token.
 */
public class CandidateSearcher {

    private CandidateSetsAndMaps candidateSetsAndMaps;
    private ElementMappings elementMappings;


    public CandidateSearcher(CandidateSetsAndMaps candidateSetsAndMaps, ElementMappings elementMappings) {
        this.candidateSetsAndMaps = candidateSetsAndMaps;
        this.elementMappings = elementMappings;
    }
    public CandidateSetsAndMaps getCandidateSetsAndMaps() {
        return candidateSetsAndMaps;
    }

    /**
     * Set current element mappings each time the element mappings are updated
     * @param elementMappings current element mappings
     */
    public void setElementMappings(ElementMappings elementMappings) {
        this.elementMappings = elementMappings;
    }

    /**
     * Get the target candidates for a given source element
     * @param srcElement a given source element
     * @return the set of target candidates
     */
    public Set<ProgramElement> getDstCandidateElements(ProgramElement srcElement) {
        if (srcElement.isStmt())
            return getDstCandidateStmtElements((StmtElement) srcElement);
        else if (srcElement.isToken())
            return getDstCandidateTokenElements((TokenElement) srcElement);
        else
            return getDstCandidateInnerStmtElements((InnerStmtElement) srcElement);
    }

    /**
     * Get source statements that are not mapped by fast matchers
     */
    public Set<ProgramElement> getSrcStmtsToMap() {
        return candidateSetsAndMaps.getSrcStmtsToMap();
    }

    /**
     * Get source tokens that are not mapped by fast matchers
     */
    public Set<ProgramElement> getSrcTokensToMap() {
        return candidateSetsAndMaps.getSrcTokensToMap();
    }

    /**
     * Get all the source statements in the file
     */
    public Set<ProgramElement> getAllSrcStmts() {
        return candidateSetsAndMaps.getAllSrcStmts();
    }


    private Map<ProgramElementType, Set<ProgramElement>> getDstTypeElementMap() {
        return candidateSetsAndMaps.getDstTypeElementMap();
    }

    private Set<ProgramElement> getDstCandidateStmtElements(StmtElement srcStmt) {
        return getDstTypeElementMap().get(srcStmt.getElementType());
    }

    private Set<ProgramElement> getDstCandidateInnerStmtElements(InnerStmtElement srcInnerStmtEle) {
        List<TokenElement> srcTokens = srcInnerStmtEle.getTokenElements();
        Set<ProgramElement> ret = new HashSet<>();
        ProgramElementType srcEleType = srcInnerStmtEle.getElementType();

        for (TokenElement token: srcTokens) {
            ProgramElement dstToken = elementMappings.getMappedElement(token);
            if (dstToken != null) {
                List<InnerStmtElement> innerStmtElements = ((TokenElement) dstToken).getInnerStmtElementsWithToken();
                for (InnerStmtElement element: innerStmtElements){
                    ProgramElementType dstEleType = element.getElementType();
                    if (srcEleType.equals(dstEleType))
                        ret.add(element);
                }
            }
        }
        ProgramElement srcParentEle = srcInnerStmtEle.getParentElement();
        if (elementMappings.isMapped(srcParentEle)) {
            ProgramElement dstParentEle = elementMappings.getMappedElement(srcParentEle);
            ret.addAll(dstParentEle.getInnerStmtElements());
        }
        return ret;
    }

    private Set<ProgramElement> getDstCandidateTokenElements(TokenElement srcToken) {
        FastTokenCandidateSearcher searcher = new FastTokenCandidateSearcher(srcToken, elementMappings,
                candidateSetsAndMaps);
        Set<ProgramElement> candidatesWithSameStructure = searcher.getCandidatesWithIdenticalMultiTokenForSrcToken();
        Set<ProgramElement> sameStmtCandidates = searcher.getSameStmtCandidateTokensForSrcToken();

        Set<ProgramElement> tmp = getDstCandidateTokenElementsInSameStmt(srcToken, sameStmtCandidates);
        if (tmp.size() > 0) {
            Set<ProgramElement> tmp2 = new HashSet<>(tmp);
            tmp2.retainAll(candidatesWithSameStructure);
            if (tmp2.size() > 0) {
                if (checkGoodCandidatesCondition(srcToken, tmp2))
                    return tmp2;
            }

            if (checkGoodCandidatesCondition(srcToken, tmp))
                return tmp;
        } else if (candidatesWithSameStructure.size() > 0) {
            if (checkGoodCandidatesCondition(srcToken, candidatesWithSameStructure))
                return candidatesWithSameStructure;
        }

        Set<ProgramElement> neighborCandidates = searcher.getNeighborCandidateTokensForSrcToken();
        Set<ProgramElement> sameOrRenameValCandidates = searcher.getSameValOrRenameCandidateTokensForSrcToken();
        Set<ProgramElement> ret = new HashSet<>();
        ret.addAll(tmp);
        ret.addAll(neighborCandidates);
        ret.addAll(sameOrRenameValCandidates);
        return ret;
    }

    private Set<ProgramElement> getDstCandidateTokenElementsInSameStmt(TokenElement srcToken,
                                                                       Set<ProgramElement> sameStmtCandidates) {
        ElementMatchDeterminer determiner = new ElementMatchDeterminer(elementMappings);
        Set<ProgramElement> ret = new HashSet<>();
        for (ProgramElement element: sameStmtCandidates) {
            ElementSimMeasures measures = new ElementSimMeasures(srcToken, element);
            if (determiner.determine(measures))
                ret.add(element);
        }
        return ret;
    }

    private boolean checkGoodCandidatesCondition(TokenElement srcToken, Set<ProgramElement> candidates) {
        if (elementMappings.isMapped(srcToken)) {
            ProgramElement dstToken = elementMappings.getMappedElement(srcToken);
            return candidates.contains(dstToken);
        } else {
            for (ProgramElement dst: candidates) {
                if (!elementMappings.isMapped(dst))
                    return true;
            }
            return false;
        }
    }
}
