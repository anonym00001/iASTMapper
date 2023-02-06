package cs.model.analysis;

import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.actions.StmtTokenAction;
import cs.model.algorithm.actions.StmtTokenActionGenerator;
import cs.model.algorithm.actions.TreeEditAction;
import cs.model.algorithm.element.ElementTreeUtils;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.iASTMapper;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.utils.GumTreeUtil;
import cs.model.baseline.BaselineMatcher;
import cs.model.evaluation.config.MyConfig;
import cs.model.evaluation.csvrecord.eval.ActionAndRunningTimeRecord;
import cs.model.evaluation.utils.PairwiseComparison;
import cs.model.evaluation.csvrecord.eval.ManualEvaluationRecord;
import cs.model.evaluation.utils.MappingMethodNames;
import cs.model.evaluation.csvrecord.eval.AutoEvaluationRecord;
import cs.model.gitops.GitUtils;

import java.io.*;
import java.util.*;


/**
 * Perform the differential testing evaluation for our method
 * in comparison to other methods.
 */
public class RevisionEvaluation {
    private String project;
    private String commitId;
    private String srcFilePath;
    private iASTMapper myMatcher;     // matcher based on our method

    private ProgramElement srcRootEle;    // source root element
    private ProgramElement dstRootEle;    // target root element
    private ElementMappings myMappings;   // my element mappings

    private AnalysisResultPackage gtAnalysisResult;
    private AnalysisResultPackage mtdAnalysisResult;
    private AnalysisResultPackage ijmAnalysisResult;
    private ElementMappings gtMappings;
    private ElementMappings mtdMappings;
    private ElementMappings ijmMappings;

    private Set<ProgramElement> inconsistentlyMappedElements;
    private Set<ProgramElement> inconsistentlyMappedElementsWithGt;
    private Set<ProgramElement> inconsistentlyMappedElementsWithMtd;
    private Set<ProgramElement> inconsistentlyMappedElementsWithIjm;
    private List<TreeEditAction> myActions;

    private String script;
    private List<String[]> records;
    private String[] editActionRecord;
    private boolean noGoodResult = false;

    private List<ProgramElement> srcElements;
    private List<ProgramElement> dstElements;

    private long gtTreeBuildTime;
    private long ijmTreeBuildTime;
    private long myPreprocessTime;
    private long myMappingTime;
    private long myActionTime;
    private long gtMappingTime;
    private long gtActionTime;
    private long mtdMappingTime;
    private long mtdActionTime;
    private long ijmMappingTime;
    private long ijmActionTime;

    public RevisionEvaluation(String project, String commitId, String baseCommitId,
                              String srcFilePath, String dstFilePath) throws Exception {
        this.project = project;
        this.commitId = commitId;
        this.srcFilePath = srcFilePath;
        this.srcElements = new ArrayList<>();
        this.dstElements = new ArrayList<>();
        this.inconsistentlyMappedElements = new HashSet<>();

        // Get content of source file.
        ByteArrayOutputStream srcFileStream = GitUtils
                .getFileContentOfCommitFile(project, baseCommitId, srcFilePath);
        String srcFileContent = srcFileStream.toString("UTF-8");
        if (srcFileContent.equals("")) {
            noGoodResult = true;
            return;
        }

        // Get content of target file.
        ByteArrayOutputStream dstFileStream = GitUtils
                .getFileContentOfCommitFile(project, commitId, dstFilePath);
        String dstFileContent = dstFileStream.toString("UTF-8");
        if (dstFileContent.equals("")) {
            noGoodResult = true;
            return;
        }
        // build mappings using my method
        this.myMatcher = new iASTMapper(srcFileContent, dstFileContent);
        this.myMatcher.buildMappingsOuterLoop();
        this.myMappings = this.myMatcher.getEleMappings();
        this.myActions = this.myMatcher.getTreeEditActions();



        // build mappings, calculate edit actions using gumtree, mtdiff and ijm
        BaselineMatcher baselineMatcher = new BaselineMatcher(this.myMatcher);
        this.gtAnalysisResult = baselineMatcher.getOriginalMethodAnalysisResult("gt");
        this.mtdAnalysisResult = baselineMatcher.getOriginalMethodAnalysisResult("mtdiff");
        this.ijmAnalysisResult = baselineMatcher.getOriginalMethodAnalysisResult("ijm");
        this.gtMappings = this.gtAnalysisResult.getEleMappings();
        this.mtdMappings = this.mtdAnalysisResult.getEleMappings();
        this.ijmMappings = this.ijmAnalysisResult.getEleMappings();

        this.srcRootEle = this.myMatcher.getSrcRootEle();
        this.dstRootEle = this.myMatcher.getDstRootEle();

        initAnalyzedElements();
        initMappingComparison();
        initRunningTime(baselineMatcher);
        initEditActionRecord();
    }

    public ElementMappings getEleMappings(){
        return this.myMatcher.getEleMappings();
    }
    public ElementMappings getGTEleMappings(){
        return this.gtMappings;
    }
    public ElementMappings getMTDEleMappings(){
        return this.mtdMappings;
    }
    public ElementMappings getIJMEleMappings(){
        return this.ijmMappings;
    }

    public List<StmtTokenAction> generateStmtTokenEditActions(){
        return this.myMatcher.generateStmtTokenEditActions();
    }
    public List<StmtTokenAction> generateGtStmtTokenEditActions(){
        StmtTokenActionGenerator generator = new StmtTokenActionGenerator(this.myMatcher.getSrcStmts(), this.myMatcher.getDstStmts(), this.gtAnalysisResult.getEleMappings());
        List<StmtTokenAction> actionList = generator.generateActions(false);
        return generator.reorderActions(actionList);
    }
    public List<StmtTokenAction> generateMtdStmtTokenEditActions(){
        StmtTokenActionGenerator generator = new StmtTokenActionGenerator(this.myMatcher.getSrcStmts(), this.myMatcher.getDstStmts(), this.mtdAnalysisResult.getEleMappings());
        List<StmtTokenAction> actionList = generator.generateActions(false);
        return generator.reorderActions(actionList);
    }
    public List<StmtTokenAction> generateIjmStmtTokenEditActions(){
        StmtTokenActionGenerator generator = new StmtTokenActionGenerator(this.myMatcher.getSrcStmts(), this.myMatcher.getDstStmts(), this.ijmAnalysisResult.getEleMappings());
        List<StmtTokenAction> actionList = generator.generateActions(false);
        return generator.reorderActions(actionList);
    }
    public List<TreeEditAction> getTreeEditActions() {
        return this.myMatcher.getTreeEditActions();
    }
    public List<TreeEditAction> getGTTreeEditActions() {
        List<Action> actionList = GumTreeUtil.getEditActions(this.gtAnalysisResult.getMs());
        List<TreeEditAction> actions = null;
        if (actionList != null) {
            actions = new ArrayList<>();
            for (Action a : actionList) {
                TreeEditAction ea = new TreeEditAction(a, this.gtAnalysisResult.getMs(), this.myMatcher.getSrcRc(), this.myMatcher.getDstRc());
                if (ea.isJavadocRelated())
                    continue;
                actions.add(ea);
            }
        }
        return actions;
    }
    public List<TreeEditAction> getMTDTreeEditActions() {
        List<Action> actionList = GumTreeUtil.getEditActions(this.mtdAnalysisResult.getMs());
        List<TreeEditAction> actions = null;
        if (actionList != null) {
            actions = new ArrayList<>();
            for (Action a : actionList) {
                TreeEditAction ea = new TreeEditAction(a, this.mtdAnalysisResult.getMs(), this.myMatcher.getSrcRc(), this.myMatcher.getDstRc());
                if (ea.isJavadocRelated())
                    continue;
                actions.add(ea);
            }
        }
        return actions;
    }
    public List<TreeEditAction> getIJMTreeEditActions() {
        List<Action> actionList = GumTreeUtil.getEditActions(this.ijmAnalysisResult.getMs());
        List<TreeEditAction> actions = null;
        if (actionList != null) {
            actions = new ArrayList<>();
            for (Action a : actionList) {
                TreeEditAction ea = new TreeEditAction(a, this.ijmAnalysisResult.getMs(), this.myMatcher.getSrcRc(), this.myMatcher.getDstRc());
                if (ea.isJavadocRelated())
                    continue;
                actions.add(ea);
            }
        }
        return actions;
    }
    public void preOrder(ITree tree, BufferedWriter out, int n) throws IOException {
        for(int i = 0;i < n; i++)
            out.write("    |");
        out.write(tree + "\n");
        if (!tree.isLeaf())
            for (ITree c: tree.getChildren()) {
                preOrder(c, out, n + 1);
            }
    }
    public boolean isNoGoodResult() {
        return noGoodResult;
    }

    public String getScript() {
        return script;
    }

    public List<String[]> getRecords() {
        return records;
    }

    public String[] getEditActionRecord() {
        return editActionRecord;
    }

    public ProgramElement getStmtElement(boolean isSrc, int startPos) {
        List<ProgramElement> stmts;
        if (isSrc)
            stmts = ElementTreeUtils.getAllStmtsPreOrder(srcRootEle);
        else
            stmts = ElementTreeUtils.getAllStmtsPreOrder(dstRootEle);
        for (ProgramElement stmt: stmts) {
            if (stmt.getITreeNode().getPos() == startPos)
                return stmt;
        }
        return null;
    }

    private void initAnalyzedElements(){
        srcElements = ElementTreeUtils.getAllStmtsPreOrder(srcRootEle);
        dstElements = ElementTreeUtils.getAllStmtsPreOrder(dstRootEle);
        List<ProgramElement> srcTokenElements = new ArrayList<>();
        List<ProgramElement> dstTokenElements = new ArrayList<>();
        for (ProgramElement srcStmt: srcElements)
            srcTokenElements.addAll(srcStmt.getTokenElements());
        for (ProgramElement dstStmt: dstElements)
            dstTokenElements.addAll(dstStmt.getTokenElements());
        srcElements.addAll(srcTokenElements);
        dstElements.addAll(dstTokenElements);
    }

    private void initMappingComparison(){
        PairwiseComparison comparison1 = new PairwiseComparison(myMappings, gtMappings, srcElements, dstElements);
        PairwiseComparison comparison2 = new PairwiseComparison(myMappings, mtdMappings, srcElements, dstElements);
        PairwiseComparison comparison3 = new PairwiseComparison(myMappings, ijmMappings, srcElements, dstElements);

        inconsistentlyMappedElementsWithGt = comparison1.getInconsistentlyMappedElements();
        inconsistentlyMappedElementsWithMtd = comparison2.getInconsistentlyMappedElements();
        inconsistentlyMappedElementsWithIjm = comparison3.getInconsistentlyMappedElements();

        inconsistentlyMappedElements.addAll(inconsistentlyMappedElementsWithGt);
        inconsistentlyMappedElements.addAll(inconsistentlyMappedElementsWithMtd);
        inconsistentlyMappedElements.addAll(inconsistentlyMappedElementsWithIjm);
    }

    private void initRunningTime(BaselineMatcher baselineAnalysis) {
        this.gtTreeBuildTime = myMatcher.getTreeBuildTime();
        this.myPreprocessTime = myMatcher.getTtMapBuildTime();
        this.ijmTreeBuildTime = baselineAnalysis.getIjmTreeBuildTime();
        this.myMappingTime = myMatcher.getMappingTime();
        this.myActionTime = myMatcher.getActionGenerationTime();
        this.gtMappingTime = baselineAnalysis.getGtMappingTime();
        this.gtActionTime = baselineAnalysis.getGtActionTime();
        this.mtdMappingTime = baselineAnalysis.getMtdMappingTime();
        this.mtdActionTime = baselineAnalysis.getMtdActionTime();
        this.ijmMappingTime = baselineAnalysis.getIjmMappingTime();
        this.ijmActionTime = baselineAnalysis.getIjmActionTime();
    }

    private void initEditActionRecord() {
        ActionAndRunningTimeRecord actionRecord = new ActionAndRunningTimeRecord(project, commitId, srcFilePath);
        int myActionNum = myActions.size();
        int gtActionNum = gtAnalysisResult.getTreeEditActions().size();
        int mtdActionNum = mtdAnalysisResult.getTreeEditActions().size();
        int ijmActionNum = ijmAnalysisResult.getTreeEditActions().size();
        actionRecord.setActionNums(myActionNum, gtActionNum, mtdActionNum, ijmActionNum);
        actionRecord.setRunningTime(gtTreeBuildTime, ijmTreeBuildTime, myPreprocessTime,
                myMappingTime, myActionTime, gtMappingTime, gtActionTime, mtdMappingTime, mtdActionTime,
                ijmMappingTime, ijmActionTime);
        this.editActionRecord = actionRecord.getRecord();
    }

    public void generateManualScriptAndRecord(ProgramElement stmtEle, String compareMethod) {
        script = "";
        script += "URL: " + MyConfig.getCommitUrl(project, commitId) + "\n";
        script += "FILE PATH: " + srcFilePath + "\n";
        script += "================================================================\n";

        records = new ArrayList<>();
        Map<ProgramElement, String> stmtActionMap1 = getStmtActionMapForStmt(stmtEle, MappingMethodNames.SEMAPPING);
        Map<ProgramElement, String> stmtActionMap2 = getStmtActionMapForStmt(stmtEle, compareMethod);
        createScriptAndRecord(stmtEle, MappingMethodNames.SEMAPPING, stmtActionMap1);
        createScriptAndRecord(stmtEle, compareMethod, stmtActionMap2);
    }

    private Map<ProgramElement, String> getStmtActionMapForStmt(ProgramElement stmtEle, String methodName) {
        List<ProgramElement> srcStmts = ElementTreeUtils.getAllStmtsPreOrder(srcRootEle);
        List<ProgramElement> dstStmts = ElementTreeUtils.getAllStmtsPreOrder(dstRootEle);
        StmtTokenActionGenerator generator;
        switch (methodName) {
            case MappingMethodNames.GT:
                generator = new StmtTokenActionGenerator(srcStmts, dstStmts, gtMappings);
                break;
            case MappingMethodNames.MTDIFF:
                generator = new StmtTokenActionGenerator(srcStmts, dstStmts, mtdMappings);
                break;
            case MappingMethodNames.IJM:
                generator = new StmtTokenActionGenerator(srcStmts, dstStmts, ijmMappings);
                break;
            case MappingMethodNames.SEMAPPING:
                generator = new StmtTokenActionGenerator(srcStmts, dstStmts, myMappings);
                break;
            default:
                throw new RuntimeException("Unsupported AST mapping method");
        }
        List<StmtTokenAction> actionList = generator.generateActions(true);
        for (StmtTokenAction action: actionList) {
            if (stmtEle.equals(action.getSrcStmtEle()) || stmtEle.equals(action.getDstStmtEle()))
                return action.getElementActionMap();
        }
        return null;
    }

    private void createScriptAndRecord(ProgramElement stmtEle, String methodName,
                                       Map<ProgramElement, String> elementActionMap) {
        ManualEvaluationRecord record = new ManualEvaluationRecord(project, commitId, srcFilePath, methodName, stmtEle);
        switch (methodName) {
            case MappingMethodNames.GT:
                record.setElementMappings(gtMappings);
                break;
            case MappingMethodNames.MTDIFF:
                record.setElementMappings(mtdMappings);
                break;
            case MappingMethodNames.IJM:
                record.setElementMappings(ijmMappings);
                break;
            case MappingMethodNames.SEMAPPING:
                record.setElementMappings(myMappings);
                break;
            default:
                throw new RuntimeException("Unsupported AST mapping method.");
        }
        script += record.getScript(elementActionMap);
        records.add(record.toCsvRecord());
    }

    public void generateStmtAndTokenRecords() {
        Set<ProgramElement> inconsistentlyMappedStmtsWithGt = getInconsistentlyMappedStmtsWithGt();
        Set<ProgramElement> inconsistentlyMappedStmtsWithMtd = getInconsistentlyMappedStmtsWithMtd();
        Set<ProgramElement> inconsistentlyMappedStmtsWithIjm = getInconsistentlyMappedStmtsWithIjm();
        records = new ArrayList<>();

        List<ProgramElement> srcStmts = ElementTreeUtils.getAllStmtsPreOrder(srcRootEle);
        List<ProgramElement> dstStmts = ElementTreeUtils.getAllStmtsPreOrder(dstRootEle);

        Set<ProgramElement> addedDstStmts = new HashSet<>();

        for (ProgramElement srcStmt: srcStmts) {
            ProgramElement dstStmt = myMappings.getMappedElement(srcStmt);
            boolean condition1 = inconsistentlyMappedStmtsWithGt.contains(srcStmt);
            boolean condition2 = inconsistentlyMappedStmtsWithMtd.contains(srcStmt);
            boolean condition3 = inconsistentlyMappedStmtsWithIjm.contains(srcStmt);
            boolean condition4 = false;
            boolean condition5 = false;
            boolean condition6 = false;
            if (dstStmt != null) {
                addedDstStmts.add(dstStmt);
                condition4 = inconsistentlyMappedStmtsWithGt.contains(dstStmt);
                condition5 = inconsistentlyMappedStmtsWithMtd.contains(dstStmt);
                condition6 = inconsistentlyMappedStmtsWithIjm.contains(dstStmt);
            }

            if (condition1 || condition2 || condition3 || condition4 || condition5 || condition6) {
                AutoEvaluationRecord record = new AutoEvaluationRecord(project,
                        commitId, srcFilePath, srcStmt, dstStmt, condition1, condition2,
                        condition3, condition4, condition5, condition6);
                records.add(record.toCsvRecord());
            }
        }

        for (ProgramElement dstStmt: dstStmts) {
            if (addedDstStmts.contains(dstStmt))
                continue;
            boolean condition1 = inconsistentlyMappedStmtsWithGt.contains(dstStmt);
            boolean condition2 = inconsistentlyMappedStmtsWithMtd.contains(dstStmt);
            boolean condition3 = inconsistentlyMappedStmtsWithIjm.contains(dstStmt);
            if (condition1 || condition2 || condition3) {
                AutoEvaluationRecord record = new AutoEvaluationRecord(project,
                        commitId, srcFilePath, null, dstStmt, false, false,
                        false, condition1, condition2, condition3);
                records.add(record.toCsvRecord());
            }
        }
    }

    private Set<ProgramElement> getInconsistentlyMappedStmtsWithGt() {
        Set<ProgramElement> ret = new HashSet<>();
        for (ProgramElement ele: inconsistentlyMappedElementsWithGt) {
            if (ele.isStmt())
                ret.add(ele);
            if (ele.isToken())
                ret.add(ele.getStmtElement());
        }
        return ret;
    }

    private Set<ProgramElement> getInconsistentlyMappedStmtsWithMtd() {
        Set<ProgramElement> ret = new HashSet<>();
        for (ProgramElement ele: inconsistentlyMappedElementsWithMtd) {
            if (ele.isStmt())
                ret.add(ele);
            if (ele.isToken())
                ret.add(ele.getStmtElement());
        }
        return ret;
    }

    private Set<ProgramElement> getInconsistentlyMappedStmtsWithIjm() {
        Set<ProgramElement> ret = new HashSet<>();
        for (ProgramElement ele: inconsistentlyMappedElementsWithIjm) {
            if (ele.isStmt())
                ret.add(ele);
            if (ele.isToken())
                ret.add(ele.getStmtElement());
        }
        return ret;
    }
}
