package cs.model.algorithm.matcher.matchers;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.element.InnerStmtElement;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.StmtElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.matchers.searchers.BestMappingSearcher;

import java.util.*;

/**
 * Matcher for inner-stmt element using diverse similarity measures.
 */
public class InnerStmtEleMatcher extends BaseMatcher {

    public InnerStmtEleMatcher(ElementMappings eleMappings, BestMappingSearcher bestMappingSearcher) {
        super(eleMappings, bestMappingSearcher);
    }

    @Override
    protected Set<ProgramElement> getAllSrcElementsToMap() {
        Set<InnerStmtElement> tmpSet = new HashSet<>();
        Set<ProgramElement> srcStmtEleSet = bestMappingSearcher.getAllSrcStmts();
        for (ProgramElement srcStmtEle: srcStmtEleSet) {
            tmpSet.addAll(((StmtElement) srcStmtEle).getAllInnerStmtElements());
        }
        Set<ProgramElement> ret = new HashSet<>();
        for (InnerStmtElement tmpEle: tmpSet) {
            if (elementMappings.isMapped(tmpEle))
                continue;
            ret.add(tmpEle);
        }
        return ret;
    }
}
