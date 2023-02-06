package cs.model.algorithm.matcher.matchers;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.matcher.mappings.ElementMapping;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.matchers.searchers.BestMappingSearcher;
import cs.model.algorithm.ttmap.TreeTokensMap;

import java.util.*;

/**
 * Matcher for statement using diverse similarity measures.
 */
public class StmtMatcher extends BaseMatcher {


    public StmtMatcher(ElementMappings elementMappings, BestMappingSearcher bestMappingSearcher) {
        super(elementMappings, bestMappingSearcher);
    }

    @Override
    protected Set<ProgramElement> getAllSrcElementsToMap() {
        return bestMappingSearcher.getSrcStmtsToMap();
    }
}
