package cs.sysu.baseline;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.sysu.algorithm.iASTMapper;
import cs.sysu.algorithm.actions.StmtTokenAction;
import cs.sysu.algorithm.actions.StmtTokenActionGenerator;
import cs.sysu.algorithm.actions.TreeEditAction;
import cs.sysu.algorithm.element.ElementTreeBuilder;
import cs.sysu.algorithm.element.ElementTreeUtils;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.mappings.MappingTransformer;
import cs.sysu.algorithm.ttmap.TokenRangeTypeMap;
import cs.sysu.algorithm.ttmap.TreeTokensMap;
import cs.sysu.algorithm.utils.GumTreeUtil;
import cs.sysu.algorithm.utils.RangeCalculator;
import cs.sysu.analysis.AnalysisResultPackage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class BaselineMatcher {
    private List<ProgramElement> srcStmtElements;
    private List<ProgramElement> dstStmtElements;
    private ITree src;
    private ITree dst;
    private String srcFileContent;
    private String dstFileContent;
    private TreeTokensMap srcTtMap;
    private TreeTokensMap dstTtMap;
    private TokenRangeTypeMap srcTokenTypeMap;
    private TokenRangeTypeMap dstTokenTypeMap;
    private RangeCalculator srcRc;
    private RangeCalculator dstRc;

    private List<TreeEditAction> actions;

    private long gtMappingTime = 0;
    private long gtActionTime = 0;
    private long mtdMappingTime = 0;
    private long mtdActionTime = 0;
    private long ijmMappingTime = 0;
    private long ijmActionTime = 0;

    private long ijmTreeBuildTime = 0;
    public BaselineMatcher(String srcFileContent, String dstFileContent) throws Exception {
        this.src = GumTreeUtil.getITreeRoot(srcFileContent, "gt");
        this.dst = GumTreeUtil.getITreeRoot(dstFileContent, "gt");
        this.srcRc = new RangeCalculator(srcFileContent);
        this.dstRc = new RangeCalculator(dstFileContent);
        this.srcTtMap = new TreeTokensMap(srcRc, src, src, new HashSet<>());
        this.dstTtMap = new TreeTokensMap(dstRc, dst, dst, new HashSet<>());
        this.src.setParent(null);
        this.dst.setParent(null);

        this.srcTokenTypeMap = new TokenRangeTypeMap(srcTtMap);
        this.dstTokenTypeMap = new TokenRangeTypeMap(dstTtMap);

        ProgramElement srcRootEle = ElementTreeBuilder.buildElementTree(src, srcTtMap, srcTokenTypeMap, true);
        ProgramElement dstRootEle = ElementTreeBuilder.buildElementTree(dst, dstTtMap, dstTokenTypeMap, false);
        this.srcStmtElements = ElementTreeUtils.getAllStmtsPreOrder(srcRootEle);
        this.dstStmtElements = ElementTreeUtils.getAllStmtsPreOrder(dstRootEle);
        this.srcFileContent = srcFileContent;
        this.dstFileContent = dstFileContent;
    }


    public BaselineMatcher(iASTMapper myMatcher) {
        this.srcStmtElements = myMatcher.getSrcStmts();
        this.dstStmtElements = myMatcher.getDstStmts();
        this.src = myMatcher.getSrc();
        this.dst = myMatcher.getDst();
        this.srcFileContent = myMatcher.getSrcFileContent();
        this.dstFileContent = myMatcher.getDstFileContent();
        this.srcTtMap = myMatcher.getSrcTtMap();
        this.dstTtMap = myMatcher.getDstTtMap();
        this.srcTokenTypeMap = myMatcher.getSrcTokenTypeMap();
        this.dstTokenTypeMap = myMatcher.getDstTokenTypeMap();
        this.srcRc = myMatcher.getSrcRc();
        this.dstRc = myMatcher.getDstRc();
    }

    /**
     * Generate Stmt-Token edit actions for GumTree, MTDiff and IJM
     * @param method a given method
     */
    public List<StmtTokenAction> generateStmtTokenEditActionsForMethod(String method){
        ElementMappings tmpEleMappings = getOriginalMethodMapping(method);
        StmtTokenActionGenerator generator = new StmtTokenActionGenerator(srcStmtElements, dstStmtElements, tmpEleMappings);;
        List<StmtTokenAction> actionList = generator.generateActions(false);
        return generator.reorderActions(actionList);
    }

    public List<TreeEditAction> getTreeEditActions() {
        return actions;
    }

    public AnalysisResultPackage getOriginalMethodAnalysisResult(String method) throws IOException {
        MappingStore ms = new MappingStore(src, dst);
        if (method.equals("gt")) {
            long time1 = System.currentTimeMillis();
            ms = GumTreeUtil.getTreeMappings(src, dst, ms, "gumtree");
            ms = handleAmbiguousMapping(ms, src, dst);

            long time2 = System.currentTimeMillis();
            gtMappingTime = time2 - time1;

            ElementMappings eleMappings = MappingTransformer.getElementMappingsFromTreeMappings(ms, src, dst,
                    srcTokenTypeMap, dstTokenTypeMap, srcTtMap, dstTtMap);
            AnalysisResultPackage result =  new AnalysisResultPackage(eleMappings, ms, srcRc, dstRc);
            gtActionTime = result.getActionGenerationTime();
            return result;
        } else if (method.equals("mtdiff")) {
            long time1 = System.currentTimeMillis();
            ms = GumTreeUtil.getTreeMappings(src, dst, ms, "mtdiff");
            ms = handleAmbiguousMapping(ms, src, dst);
            long time2 = System.currentTimeMillis();
            mtdMappingTime = time2 - time1;

            ElementMappings eleMappings = MappingTransformer.getElementMappingsFromTreeMappings(ms, src, dst,
                    srcTokenTypeMap, dstTokenTypeMap, srcTtMap, dstTtMap);
            AnalysisResultPackage result = new AnalysisResultPackage(eleMappings, ms, srcRc, dstRc);
            mtdActionTime = result.getActionGenerationTime();
            return result;
        } else if (method.equals("ijm")) {
            long time1 = System.currentTimeMillis();
            ITree ijmSrc = GumTreeUtil.getITreeRoot(srcFileContent, "ijm");
            ITree ijmDst = GumTreeUtil.getITreeRoot(dstFileContent, "ijm");
            long time2 = System.currentTimeMillis();
            ijmTreeBuildTime = time2 - time1;

            TreeTokensMap srcTtMapForIjm = new TreeTokensMap(srcRc, ijmSrc, src, new HashSet<>());
            TreeTokensMap dstTtMapForIjm = new TreeTokensMap(dstRc, ijmDst, dst, new HashSet<>());

            time1 = System.currentTimeMillis();
            ms = GumTreeUtil.getTreeMappings(ijmSrc, ijmDst, ms, "ijm");
            ms = handleAmbiguousMapping(ms, ijmSrc, ijmDst);
            time2 = System.currentTimeMillis();
            ijmMappingTime = time2 - time1;

            ElementMappings eleMappings = MappingTransformer.getElementMappingsFromTreeMappings(ms, ijmSrc, ijmDst,
                    srcTokenTypeMap, dstTokenTypeMap, srcTtMapForIjm, dstTtMapForIjm);
            AnalysisResultPackage result = new AnalysisResultPackage(eleMappings, ms, srcRc, dstRc);
            ijmActionTime = result.getActionGenerationTime();
            return result;
        }
        throw new RuntimeException("Method currently not support!");
    }

    private List<TreeEditAction> getTreeEditActions(MappingStore ms) {
        List<Action> actionList = GumTreeUtil.getEditActions(ms);
        List<TreeEditAction> treeEditActions = new ArrayList<>();
        if (actionList != null) {
            for (Action a: actionList) {
                TreeEditAction ea = new TreeEditAction(a, ms, srcRc, dstRc);
                if (ea.isJavadocRelated())
                    continue;
                treeEditActions.add(ea);
            }
        }
        return treeEditActions;
    }

    /**
     * Generate element mappings for given method, e.g., GumTree, MTDiff and IJM
     * @param method a given method
     */
    public ElementMappings getOriginalMethodMapping(String method){
        MappingStore ms = new MappingStore(src, dst);;
        if (method.equalsIgnoreCase("gt") || method.equalsIgnoreCase("gumtree")) {
            long time1 = System.currentTimeMillis();
            ms = GumTreeUtil.getTreeMappings(src, dst, ms, "gumtree");
            ms = handleAmbiguousMapping(ms, src, dst);
            long time2 = System.currentTimeMillis();
            gtMappingTime = time2 - time1;
            actions = getTreeEditActions(ms);
            return MappingTransformer.getElementMappingsFromTreeMappings(ms, src, dst, srcTokenTypeMap, dstTokenTypeMap,
                    srcTtMap, dstTtMap);
        } else if (method.equalsIgnoreCase("mtdiff") || method.equalsIgnoreCase("mtd")) {
            long time1 = System.currentTimeMillis();
            ms = GumTreeUtil.getTreeMappings(src, dst, ms, "mtdiff");
            ms = handleAmbiguousMapping(ms, src, dst);
            long time2 = System.currentTimeMillis();
            mtdMappingTime = time2 - time1;
            actions = getTreeEditActions(ms);
            return MappingTransformer.getElementMappingsFromTreeMappings(ms, src, dst, srcTokenTypeMap, dstTokenTypeMap,
                    srcTtMap, dstTtMap);
        } else if (method.equals("ijm")) {
            long time1 = System.currentTimeMillis();
//            System.out.println(srcFileContent);
            ITree ijmSrc = GumTreeUtil.getITreeRoot(srcFileContent, "ijm");
            ITree ijmDst = GumTreeUtil.getITreeRoot(dstFileContent, "ijm");
            long time2 = System.currentTimeMillis();
            ijmTreeBuildTime = time2 - time1;
            TreeTokensMap srcTtMapForIjm = new TreeTokensMap(srcRc, ijmSrc, src, new HashSet<>());
            TreeTokensMap dstTtMapForIjm = new TreeTokensMap(dstRc, ijmDst, dst, new HashSet<>());

            time1 = System.currentTimeMillis();
            ms = GumTreeUtil.getTreeMappings(ijmSrc, ijmDst, ms, "ijm");
            ms = handleAmbiguousMapping(ms, ijmSrc, ijmDst);
            time2 = System.currentTimeMillis();
            ijmMappingTime = time2 - time1;
            actions = getTreeEditActions(ms);
            return MappingTransformer.getElementMappingsFromTreeMappings(ms, ijmSrc, ijmDst, srcTokenTypeMap, dstTokenTypeMap,
                    srcTtMapForIjm, dstTtMapForIjm);
        }

        throw new RuntimeException("Method currently not support!");
    }

    private MappingStore handleAmbiguousMapping(MappingStore ms, ITree srcRoot, ITree dstRoot){
        MappingStore tmp = new MappingStore(srcRoot, dstRoot);
        for (ITree t: srcRoot.preOrder()){
            if (ms.isSrcMapped(t)){
                ITree dstT = ms.getDstForSrc(t);
                if (!tmp.isDstMapped(dstT)){
                    if (ms.isDstMapped(dstT) && ms.getSrcForDst(dstT) == t)
                        tmp.addMapping(t, dstT);
                }
            }
        }
        return tmp;
    }

    public long getGtMappingTime() {
        return gtMappingTime;
    }

    public long getGtActionTime() {
        return gtActionTime;
    }

    public long getMtdMappingTime() {
        return mtdMappingTime;
    }

    public long getMtdActionTime() {
        return mtdActionTime;
    }

    public long getIjmTreeBuildTime() {
        return ijmTreeBuildTime;
    }

    public long getIjmMappingTime() {
        return ijmMappingTime;
    }

    public long getIjmActionTime() {
        return ijmActionTime;
    }
}
