package cs.model.analysis;

import cs.model.algorithm.element.ElementTreeUtils;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.StmtElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.iASTMapper;
import cs.model.algorithm.actions.StmtTokenAction;
import cs.model.evaluation.csvrecord.measure.StmtMappingAndMeasureRecord;
import cs.model.gitops.GitUtils;
import cs.model.algorithm.actions.TreeEditAction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Perform analysis with our method on a file revision
 */
public class RevisionAnalysis {
    protected String project;
    protected String commitId;
    protected String srcFilePath;
    protected String dstFilePath;
    protected String srcFileContent;
    protected String dstFileContent;

    private iASTMapper matcher;
    private List<StmtMappingAndMeasureRecord> mappingRecords;
    private ProgramElement srcRootEle;
    private ProgramElement dstRootEle;
    private List<StmtTokenAction> actionList = null;
    private ElementMappings eleMappings;

    public RevisionAnalysis(String project, String commitId, String baseCommitId,
                            String srcFilePath, String dstFilePath) throws Exception{
        this.project = project;
        this.commitId = commitId;
        this.srcFilePath = srcFilePath;
        this.dstFilePath = dstFilePath;

        System.out.println(srcFilePath);
        try {
            ByteArrayOutputStream srcFileStream = GitUtils
                    .getFileContentOfCommitFile(project, baseCommitId, srcFilePath);
            srcFileContent = srcFileStream.toString("UTF-8");
            if (srcFileContent.equals("")){
                this.srcFilePath = null;
                return;
            }
            ByteArrayOutputStream dstFileStream = GitUtils
                    .getFileContentOfCommitFile(project, commitId, dstFilePath);
            dstFileContent = dstFileStream.toString("UTF-8");
            if (dstFileContent.equals("")) {
                this.dstFilePath = null;
                return;
            }
            String file_name = "";
            String[]  strs =this.srcFilePath.split("/");
            for(int i=0,len=strs[strs.length - 1].length();i<len;i++){
                if(strs[strs.length - 1].charAt(i) == '.') {
                    file_name = strs[strs.length - 1].substring(0, i);
                    break;
                }
            }
            matcher = new iASTMapper(srcFileContent, dstFileContent);
            matcher.buildMappingsOuterLoop();
            this.eleMappings = matcher.getEleMappings();
            srcRootEle = matcher.getSrcRootEle();
            dstRootEle = matcher.getDstRootEle();
            calMappingRecords();
        } catch (Exception e){
            e.printStackTrace();
            this.srcFilePath = null;
            this.dstFilePath = null;
            throw new RuntimeException(e.getMessage());
        }
    }

    private void calMappingRecords(){
        mappingRecords = new ArrayList<>();
        List<ProgramElement> srcStmts = ElementTreeUtils.getAllStmtsPreOrder(srcRootEle);
        for (ProgramElement srcStmt: srcStmts) {
            ProgramElement dstStmt = eleMappings.getDstForSrc(srcStmt);
            if (dstStmt != null){
                StmtMappingAndMeasureRecord record = new StmtMappingAndMeasureRecord(project, commitId, srcFilePath);
                record.setStmtInfo((StmtElement) srcStmt, (StmtElement) dstStmt);
                record.setMeasures(srcStmt, dstStmt, eleMappings);
                mappingRecords.add(record);
            }
        }
    }

    public List<StmtTokenAction> generateActions() {
        if (actionList == null)
            actionList = matcher.generateStmtTokenEditActions();
        return actionList;
    }

    public List<TreeEditAction> generateEditActions(){
        return matcher.getTreeEditActions();
    }

    public List<String[]> getMappingRecords() {
        List<String[]> ret = new ArrayList<>();
        if (mappingRecords.size() > 0) {
            for (StmtMappingAndMeasureRecord record: mappingRecords) {
                ret.add(record.toRecord());
            }
        }
        return ret;
    }

    public String getSrcFilePath() {
        return srcFilePath;
    }

    public String getDstFilePath() {
        return dstFilePath;
    }

    public String getSrcFileContent() {
        return srcFileContent;
    }

    public String getDstFileContent() {
        return dstFileContent;
    }

    public iASTMapper getMatcher() {
        return matcher;
    }
}
