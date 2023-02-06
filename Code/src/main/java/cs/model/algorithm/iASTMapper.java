package cs.model.algorithm;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.element.ElementTreeBuilder;
import cs.model.algorithm.element.ElementTreeUtils;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.matcher.matchers.InnerStmtEleMatcher;
import cs.model.algorithm.matcher.matchers.searchers.BestMappingSearcher;
import cs.model.algorithm.matcher.matchers.searchers.CandidateSearcher;
import cs.model.algorithm.matcher.fastmatchers.BaseFastMatcher;
import cs.model.algorithm.matcher.fastmatchers.MultiFastMatcher;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.mappings.MappingTransformer;
import cs.model.algorithm.matcher.matchers.BaseMatcher;
import cs.model.algorithm.matcher.matchers.StmtMatcher;
import cs.model.algorithm.matcher.matchers.TokenMatcher;
import cs.model.algorithm.actions.StmtTokenAction;
import cs.model.algorithm.actions.StmtTokenActionGenerator;
import cs.model.algorithm.matcher.matchers.searchers.CandidateSetsAndMaps;
import cs.model.algorithm.ttmap.TokenRangeTypeMap;
import cs.model.algorithm.ttmap.TreeTokensMap;
import cs.model.algorithm.actions.TreeEditAction;
import cs.model.algorithm.utils.GumTreeUtil;
import cs.model.algorithm.utils.RangeCalculator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * APIs of SE-Mapping
 */
public class iASTMapper {
    // root ITree node of source file and target file
    private final ITree src;
    private final ITree dst;

    // content of source file and target file
    private final String srcFileContent;
    private final String dstFileContent;

    // range calculator for source file and target file
    private final RangeCalculator srcRc;
    private final RangeCalculator dstRc;

    // tree token map for source file and target file
    private final TreeTokensMap srcTtMap;
    private final TreeTokensMap dstTtMap;

    // root element of source file and target file
    private final ProgramElement srcRootEle;
    private final ProgramElement dstRootEle;

    // statements of source file and target file
    private final List<ProgramElement> srcStmts;
    private final List<ProgramElement> dstStmts;

    // token type map for source and target file
    private final TokenRangeTypeMap srcTokenTypeMap;
    private final TokenRangeTypeMap dstTokenTypeMap;

    private final ElementMappings eleMappings;   // mappings of program elements
    private MappingStore ms;    // mappings of tree nodes
    private List<TreeEditAction> actions;

    private long treeBuildTime;
    private long ttMapBuildTime;
    private long mappingTime;
    private long actionGenerationTime;

    private String data_dir;

    public iASTMapper(String srcFileContent, String dstFileContent) throws IOException {
        // We use gumtree's AST in SE-Mapping algorithm
        long time1 = System.currentTimeMillis();
        this.src = GumTreeUtil.getITreeRoot(srcFileContent, "gt");
        this.dst = GumTreeUtil.getITreeRoot(dstFileContent, "gt");
        long time2 = System.currentTimeMillis();
        treeBuildTime = time2 - time1;

        this.srcFileContent = srcFileContent;
        this.dstFileContent = dstFileContent;

        time1 = System.currentTimeMillis();
        this.srcRc = new RangeCalculator(srcFileContent);
        this.dstRc = new RangeCalculator(dstFileContent);
        this.srcTtMap = new TreeTokensMap(srcRc, src, src, new HashSet<>());
        this.dstTtMap = new TreeTokensMap(dstRc, dst, dst, new HashSet<>());
        time2 = System.currentTimeMillis();
        ttMapBuildTime = time2 - time1;

        this.src.setParent(null);
        this.dst.setParent(null);
        this.srcTokenTypeMap = new TokenRangeTypeMap(srcTtMap);
        this.dstTokenTypeMap = new TokenRangeTypeMap(dstTtMap);

        this.srcRootEle = ElementTreeBuilder.buildElementTree(src, srcTtMap, srcTokenTypeMap, true);
        this.dstRootEle = ElementTreeBuilder.buildElementTree(dst, dstTtMap, dstTokenTypeMap, false);
        this.srcStmts = ElementTreeUtils.getAllStmtsPreOrder(srcRootEle);
        this.dstStmts = ElementTreeUtils.getAllStmtsPreOrder(dstRootEle);

        this.eleMappings = new ElementMappings();
        this.eleMappings.addMapping(srcRootEle, dstRootEle);
    }
    /**
     * Build element mappings and tree mappings.
     */
    public void buildMappingsOuterLoop() throws IOException {

        long time1 = System.currentTimeMillis();

        // Run fast matchers
        BaseFastMatcher fastMatcher = new MultiFastMatcher(srcStmts, dstStmts, eleMappings);
        fastMatcher.setTreeTokenMaps(srcTtMap, dstTtMap);
        fastMatcher.buildMappings();

        // Build candidate searcher and best mapping searcher
        CandidateSetsAndMaps candidateSetsAndMaps = new CandidateSetsAndMaps(eleMappings, srcStmts, dstStmts);
        CandidateSearcher candidateSearcher = new CandidateSearcher(candidateSetsAndMaps, eleMappings);
        BestMappingSearcher bestMappingSearcher = new BestMappingSearcher(candidateSearcher);
        // iterative statement matcher
        BaseMatcher matcher1 = new StmtMatcher(eleMappings, bestMappingSearcher);
        matcher1.setProcessToken(false);
        matcher1.calSrcElementsToMap();

        // iterative token matcher
        BaseMatcher matcher2 = new TokenMatcher(eleMappings, bestMappingSearcher);
        matcher2.setProcessToken(true);
        matcher2.calSrcElementsToMap();

        // iterative inner-stmt element matcher
        BaseMatcher matcher3 = new InnerStmtEleMatcher(eleMappings, bestMappingSearcher);
        matcher3.setProcessToken(false);
        matcher3.calSrcElementsToMap();

        matcher1.setMatcher_type("STMT");
        matcher2.setMatcher_type("TOKEN");
        matcher3.setMatcher_type("INNER");

        while (true) {
            boolean findMappings1 = matcher1.buildMappingsInnerLoop();
            boolean findMappings3 = matcher3.buildMappingsInnerLoop();
            boolean findMappings2 = matcher2.buildMappingsInnerLoop();
            if (!findMappings2)
                break;
        }

        // map all inner-stmt elements
        ms = MappingTransformer.elementMappingsToTreeMappings(eleMappings, srcRootEle, dstRootEle);
        long time2 = System.currentTimeMillis();
        mappingTime = time2 - time1;

        time1 = System.currentTimeMillis();
        List<Action> actionList = GumTreeUtil.getEditActions(ms);
        time2 = System.currentTimeMillis();
        actionGenerationTime = time2 - time1;
        if (actionList != null) {
            actions = new ArrayList<>();
            for (Action a : actionList) {
                TreeEditAction ea = new TreeEditAction(a, ms, srcRc, dstRc);
                if (ea.isJavadocRelated())
                    continue;
                actions.add(ea);
            }
        }
    }
    public MappingStore getMs(){return ms;};
    /**
     * Get the generated element mappings
     */
    public ElementMappings getEleMappings(){
        return eleMappings;
    }

    /**
     * Get the generated tree node mappings
     */
    public MappingStore getTreeMappings() {
        return ms;
    }

    public ITree getSrc() {
        return src;
    }

    public ITree getDst() {
        return dst;
    }

    public ProgramElement getSrcRootEle() {
        return srcRootEle;
    }

    public ProgramElement getDstRootEle() {
        return dstRootEle;
    }

    public List<ProgramElement> getSrcStmts() {
        return srcStmts;
    }

    public List<ProgramElement> getDstStmts() {
        return dstStmts;
    }

    public String getSrcFileContent() {
        return srcFileContent;
    }

    public String getDstFileContent() {
        return dstFileContent;
    }

    public TreeTokensMap getSrcTtMap() {
        return srcTtMap;
    }

    public TreeTokensMap getDstTtMap() {
        return dstTtMap;
    }

    public TokenRangeTypeMap getSrcTokenTypeMap() {
        return srcTokenTypeMap;
    }

    public TokenRangeTypeMap getDstTokenTypeMap() {
        return dstTokenTypeMap;
    }

    public RangeCalculator getSrcRc() {
        return srcRc;
    }

    public RangeCalculator getDstRc() {
        return dstRc;
    }

    public long getTreeBuildTime() {
        return treeBuildTime;
    }

    public long getTtMapBuildTime() {
        return ttMapBuildTime;
    }

    public long getMappingTime() {
        return mappingTime;
    }

    public long getActionGenerationTime() {
        return actionGenerationTime;
    }

    /**
     * Get the generated tree edit actions that are implemented in GumTree
     */
    public List<TreeEditAction> getTreeEditActions() {
        return actions;
    }

    /**
     * Generate Stmt-Token edit actions.
     * Such actions are grouped along each pair of mapped statements.
     * It is more convenient to visualize the mappings of statements and tokens.
     */
    public List<StmtTokenAction> generateStmtTokenEditActions(){
        StmtTokenActionGenerator generator = new StmtTokenActionGenerator(srcStmts, dstStmts, eleMappings);
        List<StmtTokenAction> actionList = generator.generateActions(false);
        return generator.reorderActions(actionList);
    }
}
