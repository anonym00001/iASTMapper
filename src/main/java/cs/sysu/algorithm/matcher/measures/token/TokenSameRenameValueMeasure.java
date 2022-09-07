package cs.sysu.algorithm.matcher.measures.token;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.matchers.searchers.CandidateSetsAndMaps;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

import java.util.HashSet;
import java.util.Set;


public class TokenSameRenameValueMeasure extends AbstractSimMeasure implements SimMeasure {
    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        if (srcEle.getStringValue().equals(dstEle.getStringValue()))
            return 1;
        if (elementMappings.isTokenRenamed(srcEle, dstEle))
            return 1;
        return 0;
    }

    @Override
    public Set<ProgramElement> filterBadDstCandidateElements(ProgramElement srcEle, Set<ProgramElement> dstCandidates,
                                                             CandidateSetsAndMaps candidateSetsAndMaps) {
        if (!srcEle.isFromSrc())
            return null;

        Set<ProgramElement> ret = new HashSet<>();
        if (dstCandidates.size() == 0)
            return ret;

        String tokenValue = srcEle.getStringValue();
        ret.addAll(candidateSetsAndMaps.getSameValDstCandidates(tokenValue));

        Set<String> renameValues = elementMappings.getRenameStatistics().getDstNameForSrcName(tokenValue);
        if (renameValues != null) {
            for (String renameValue: renameValues) {
                ret.addAll(candidateSetsAndMaps.getSameValDstCandidates(renameValue));
            }
        }

        ret.retainAll(dstCandidates);
        return ret;
    }
}
