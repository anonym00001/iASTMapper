package cs.model.algorithm.matcher.matchers.searchers;

import cs.model.algorithm.element.*;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.SimMeasure;
import cs.model.algorithm.matcher.measures.token.TokenNeighborMeasure;
import cs.model.algorithm.matcher.measures.token.TokenSameRenameValueMeasure;
import cs.model.algorithm.matcher.measures.token.TokenStmtMeasure;
import cs.model.algorithm.matcher.measures.token.TokenStructureMeasure;

import java.util.Set;

public class FastTokenCandidateSearcher {
    private final ElementMappings elementMappings;
    private final CandidateSetsAndMaps candidateSetsAndMaps;
    private TokenElement srcToken;
    private Set<ProgramElement> sameTypeCandidates;

    public FastTokenCandidateSearcher(TokenElement srcToken, ElementMappings elementMappings,
                                      CandidateSetsAndMaps candidateSetsAndMaps) {
        this.srcToken = srcToken;
        this.elementMappings = elementMappings;
        this.candidateSetsAndMaps = candidateSetsAndMaps;
        this.sameTypeCandidates = candidateSetsAndMaps.getSameTypeDstCandidates(srcToken.getElementType());
    }

    public Set<ProgramElement> getSameStmtCandidateTokensForSrcToken() {
        SimMeasure measure = new TokenStmtMeasure();
        measure.setElementMappings(elementMappings);
        return measure.filterBadDstCandidateElements(srcToken, sameTypeCandidates, candidateSetsAndMaps);
    }

    public Set<ProgramElement> getNeighborCandidateTokensForSrcToken() {
        SimMeasure measure = new TokenNeighborMeasure();
        measure.setElementMappings(elementMappings);
        return measure.filterBadDstCandidateElements(srcToken, sameTypeCandidates, candidateSetsAndMaps);
    }

    public Set<ProgramElement> getSameValOrRenameCandidateTokensForSrcToken() {
        SimMeasure measure = new TokenSameRenameValueMeasure();
        measure.setElementMappings(elementMappings);
        return measure.filterBadDstCandidateElements(srcToken, sameTypeCandidates, candidateSetsAndMaps);
    }

    public Set<ProgramElement> getCandidatesWithIdenticalMultiTokenForSrcToken() {
        SimMeasure measure = new TokenStructureMeasure();
        measure.setElementMappings(elementMappings);
        return measure.filterBadDstCandidateElements(srcToken, sameTypeCandidates, candidateSetsAndMaps);
    }
}