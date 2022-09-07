package cs.sysu.algorithm.matcher.matchers;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.matcher.mappings.ElementMapping;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.matchers.searchers.BestMappingSearcher;
import cs.sysu.algorithm.ttmap.TreeTokensMap;

import java.util.*;


/**
 * Matcher for tokens using diverse similarity measures.
 */
public class TokenMatcher extends BaseMatcher {

    public TokenMatcher(ElementMappings elementMappings, BestMappingSearcher bestMappingSearcher) {
        super(elementMappings, bestMappingSearcher);
    }

    @Override
    protected Set<ProgramElement> getAllSrcElementsToMap() {
        return bestMappingSearcher.getSrcTokensToMap();
    }
}
