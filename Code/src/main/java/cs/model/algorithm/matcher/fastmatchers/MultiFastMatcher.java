package cs.model.algorithm.matcher.fastmatchers;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;

import java.util.List;

public class MultiFastMatcher extends BaseFastMatcher {

    public MultiFastMatcher(List<ProgramElement> srcStmts, List<ProgramElement> dstStmts,
                            ElementMappings elementMappings) {
        super(srcStmts, dstStmts, elementMappings);
    }

    @Override
    public void buildMappings() {
        TokenMapStatistics statistics = new TokenMapStatistics();

        // Fast matcher 1: map two statements if they have the same content
        BaseFastMatcher matcher1 = new SameStmtMatcher(srcStmts, dstStmts, elementMappings, statistics);
        matcher1.setTreeTokenMaps(srcTtMap, dstTtMap);
        matcher1.buildMappings();

        // Fast matcher 2: map two methods if they have the same signature
        BaseFastMatcher matcher2 = new MethodMatcherWithSameSignature(srcStmts, dstStmts, elementMappings);
        matcher2.setTreeTokenMaps(srcTtMap, dstTtMap);
        matcher2.buildMappings();

        // Fast matcher 3: match all tokens in identical statements
        BaseFastMatcher matcher3 = new SameBigStructureMatcher(srcStmts, dstStmts, elementMappings);
        matcher3.setTokenMapStatistics(statistics);
        matcher3.buildMappings();
    }
}
