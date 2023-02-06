package cs.model.evaluation.csvrecord.eval;

import cs.model.utils.FileRevision;

/**
 * Csv Record for Evaluation.
 *
 * Base class for manual and automatic evaluation record.
 */
public class EvaluationRecord {
    protected String project;
    protected String commitId;
    protected String filePath;
    protected String stmtType;
    protected int srcStartPos = -1;
    protected int dstStartPos = -1;
    protected int srcStartLine = -1;
    protected int dstStartLine = -1;

    public EvaluationRecord() {}

    public EvaluationRecord(String project, String commitId, String filePath) {
        this.project = project;
        this.commitId = commitId;
        this.filePath = filePath;
    }

    public FileRevision getFileRevision() {
        return new FileRevision(commitId, filePath);
    }

    public String getStmtType() {
        return stmtType;
    }

}
