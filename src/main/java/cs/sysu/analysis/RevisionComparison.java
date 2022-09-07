package cs.sysu.analysis;

import cs.sysu.algorithm.element.ElementTreeUtils;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.iASTMapper;
import cs.sysu.baseline.BaselineMatcher;
import cs.sysu.evaluation.config.MyConfig;
import cs.sysu.evaluation.csvrecord.compare.ComparisonRecord;
import cs.sysu.evaluation.csvrecord.compare.StmtComparisonRecord;
import cs.sysu.evaluation.csvrecord.compare.TokenComparisonRecord;
import cs.sysu.evaluation.utils.PairwiseComparison;
import cs.sysu.gitops.GitUtils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RevisionComparison {
    private String project;
    private String commitId;
    private String srcFilePath;
    private iASTMapper myMatcher;

    private ProgramElement srcRootEle;
    private ProgramElement dstRootEle;
    private ElementMappings myMappings;
    private ElementMappings gtMappings;
    private ElementMappings mtdMappings;
    private ElementMappings ijmMappings;

    private Set<ProgramElement> inconsistentlyMappedElements;
    private Set<ProgramElement> inconsistentlyMappedElementsWithGt;
    private Set<ProgramElement> inconsistentlyMappedElementsWithMtd;
    private Set<ProgramElement> inconsistentlyMappedElementsWithIjm;

    private String script;
    private List<String[]> records;
    private boolean noGoodResult = false;
    private boolean stmtOrToken;

    private List<ProgramElement> srcElements;
    private List<ProgramElement> dstElements;

    public RevisionComparison(String project, String commitId, String baseCommitId,
                              String srcFilePath, String dstFilePath, boolean stmtOrToken) throws Exception {
        this.project = project;
        this.commitId = commitId;
        this.srcFilePath = srcFilePath;
        this.stmtOrToken = stmtOrToken;
        this.srcElements = new ArrayList<>();
        this.dstElements = new ArrayList<>();
        this.inconsistentlyMappedElements = new HashSet<>();

        // get source file content
        ByteArrayOutputStream srcFileStream = GitUtils
                .getFileContentOfCommitFile(project, baseCommitId, srcFilePath);
        String srcFileContent = srcFileStream.toString("UTF-8");
        if (srcFileContent.equals("")) {
            noGoodResult = true;
            return;
        }

        // get target file content
        ByteArrayOutputStream dstFileStream = GitUtils
                .getFileContentOfCommitFile(project, commitId, dstFilePath);
        String dstFileContent = dstFileStream.toString("UTF-8");
        if (dstFileContent.equals("")) {
            noGoodResult = true;
            return;
        }
        // build mappings using our method
        this.myMatcher = new iASTMapper(srcFileContent, dstFileContent);
        this.myMatcher.buildMappingsOuterLoop();
        this.myMappings = this.myMatcher.getEleMappings();

        // build mappings using gumtree, mtdiff and ijm
        BaselineMatcher baselineAnalysis = new BaselineMatcher(myMatcher);
        this.gtMappings = baselineAnalysis.getOriginalMethodMapping("gt");
        this.mtdMappings = baselineAnalysis.getOriginalMethodMapping("mtdiff");
        this.ijmMappings = baselineAnalysis.getOriginalMethodMapping("ijm");

        this.srcRootEle = this.myMatcher.getSrcRootEle();
        this.dstRootEle = this.myMatcher.getDstRootEle();
        initAnalyzedElements();
        initMappingComparison();
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

    private void initAnalyzedElements(){
        if (stmtOrToken) {
            srcElements = ElementTreeUtils.getAllStmtsPreOrder(srcRootEle);
            dstElements = ElementTreeUtils.getAllStmtsPreOrder(dstRootEle);
        } else {
            List<ProgramElement> srcStmts = ElementTreeUtils.getAllStmtsPreOrder(srcRootEle);
            List<ProgramElement> dstStmts = ElementTreeUtils.getAllStmtsPreOrder(dstRootEle);
            for (ProgramElement stmt: srcStmts)
                srcElements.addAll(stmt.getTokenElements());
            for (ProgramElement stmt: dstStmts)
                dstElements.addAll(stmt.getTokenElements());
        }
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
        generateRecordsAndScript();
    }

    private void generateRecordsAndScript(){
        script = "";
        records = new ArrayList<>();

        script += "URL: " + MyConfig.getCommitUrl(project, commitId) + "\n";
        script += "File Path: " + srcFilePath + "\n";
        script += "===============================================================\n";

        Set<ProgramElement> addedDstElements = new HashSet<>();
        for (ProgramElement ele: srcElements){
            if (inconsistentlyMappedElements.contains(ele)){
                ProgramElement dstEle = myMappings.getDstForSrc(ele);
                ComparisonRecord cr;

                boolean gtInconsistent = inconsistentlyMappedElementsWithGt.contains(ele);
                boolean mtdInconsistent = inconsistentlyMappedElementsWithMtd.contains(ele);
                boolean ijmInconsistent = inconsistentlyMappedElementsWithIjm.contains(ele);

                if (stmtOrToken)
                    cr = new StmtComparisonRecord(project, commitId, srcFilePath,
                            gtInconsistent, mtdInconsistent, ijmInconsistent, ele, dstEle);
                else
                    cr = new TokenComparisonRecord(project, commitId, srcFilePath,
                            gtInconsistent, mtdInconsistent, ijmInconsistent, ele, dstEle);
                String[] record = cr.toRecord(myMappings);
                if (record != null) {
                    records.add(record);
                    script += cr.getScript() + "\n";
                }
                if (dstEle != null)
                    addedDstElements.add(dstEle);
            }
        }

        for (ProgramElement ele: dstElements){
            if (addedDstElements.contains(ele))
                continue;
            boolean gtInconsistent = inconsistentlyMappedElementsWithGt.contains(ele);
            boolean mtdInconsistent = inconsistentlyMappedElementsWithMtd.contains(ele);
            boolean ijmInconsistent = inconsistentlyMappedElementsWithIjm.contains(ele);
            if (inconsistentlyMappedElements.contains(ele)){
                ComparisonRecord cr;
                if (stmtOrToken)
                    cr = new StmtComparisonRecord(project, commitId, srcFilePath,
                            gtInconsistent, mtdInconsistent, ijmInconsistent,
                            null, ele);
                else
                    cr = new TokenComparisonRecord(project, commitId, srcFilePath,
                            gtInconsistent, mtdInconsistent, ijmInconsistent,
                            null, ele);
                String[] record = cr.toRecord(myMappings);
                records.add(record);
                script += cr.getScript() + "\n";
            }
        }
    }

}
