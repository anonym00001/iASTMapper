package cs.model.evaluation.csvrecord.compare;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.evaluation.utils.StmtMappingFilter;
import cs.model.utils.FileRevision;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Record for comparing the statement mapping results of our method and baselines.
 */
public class StmtComparisonRecord extends ComparisonRecord {
    private String project;
    private String commitId;
    private String filePath;

    // node type of the statement
    private String stmtType;
    private int srcStartLine;
    private int dstStartLine;
    private ProgramElement srcStmtEle;
    private ProgramElement dstStmtEle;

    private String accurate;

    public StmtComparisonRecord(String project, String commitId, String filePath,
                                boolean gtInconsistent, boolean mtdInconsistent, boolean ijmInconsistent,
                                ProgramElement srcStmtEle, ProgramElement dstStmtEle){
        super(gtInconsistent, mtdInconsistent, ijmInconsistent);
        this.project = project;
        this.commitId = commitId;
        this.filePath = filePath;
        this.stmtType = srcStmtEle == null ? dstStmtEle.getNodeType() : srcStmtEle.getNodeType();
        this.srcStartLine = -1;
        this.dstStartLine = -1;
        this.srcStmtEle = srcStmtEle;
        this.dstStmtEle = dstStmtEle;
        if (srcStmtEle != null)
            this.srcStartLine = srcStmtEle.getStartLine();
        if (dstStmtEle != null)
            this.dstStartLine = dstStmtEle.getStartLine();
    }

    public StmtComparisonRecord(String[] record){
        super(record[6].equals("1"), record[7].equals("1"), record[8].equals("1"));
        this.project = record[0];
        this.commitId = record[1];
        this.filePath = record[2];
        this.stmtType = record[3];
        this.srcStartLine = Integer.parseInt(record[4]);
        this.dstStartLine = Integer.parseInt(record[5]);
        this.accurate = record[9];
    }

    public String getScript(){
        if (srcStmtEle == null)
            return "**ADD** " + dstStmtEle;
        if (dstStmtEle == null)
            return "**DEL** " + srcStmtEle;
        return "**MAP** " + srcStmtEle + " => " + dstStmtEle;
    }

    public String[] toRecord(ElementMappings eleMappings){
        StmtMappingFilter filter = new StmtMappingFilter(srcStmtEle, dstStmtEle, eleMappings);
        if (filter.isGoodMapping())
            return null;
        boolean goodMapping = filter.goodMappingAccordingRules();
        String[] record = {
                project,
                commitId,
                filePath,
                stmtType,
                Integer.toString(srcStartLine),
                Integer.toString(dstStartLine),
                gtInconsistent ? "1" : "0",
                mtdInconsistent ? "1" : "0",
                ijmInconsistent ? "1" : "0",
                goodMapping ? "1" : ""
        };
        return record;
    }

    public FileRevision getFileRevision(){
        return new FileRevision(commitId, filePath);
    }

    /**
     * process csv records to get the map of revision and stmt mappings
     * @param records stmt mapping running csv records
     * @return map of revision and stmt mappings
     */
    public static Map<FileRevision, List<StmtComparisonRecord>> processCsvData(List<String[]> records) {
        Map<FileRevision, List<StmtComparisonRecord>> ret = new HashMap<>();
        for (String[] record: records){
            StmtComparisonRecord obj = new StmtComparisonRecord(record);
            FileRevision fr = obj.getFileRevision();
            if (!ret.containsKey(fr))
                ret.put(fr, new ArrayList<>());
            ret.get(fr).add(obj);
        }
        return ret;
    }

    public static List<FileRevision> getAllFileRevisionsFromCsvRecords(List<String[]> records) {
        List<FileRevision> revisions = new ArrayList<>();
        for (String[] record: records){
            StmtComparisonRecord obj = new StmtComparisonRecord(record);
            FileRevision fr = obj.getFileRevision();
            revisions.add(fr);
        }
        return revisions;
    }

    public static String[] getHeaders() {
        String[] headers = {
                "project", "commitId", "filePath", "stmtType", "srcStmtLine", "dstStmtLine",
                "gt", "mtdiff", "ijm", "accurate"
        };
        return headers;
    }
}
