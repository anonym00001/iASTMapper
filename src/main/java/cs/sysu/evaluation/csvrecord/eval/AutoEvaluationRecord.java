package cs.sysu.evaluation.csvrecord.eval;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.evaluation.utils.MappingMethodNames;

import java.util.Random;


/**
 * Record for automatic evaluation
 */
public class AutoEvaluationRecord extends EvaluationRecord {

    // our method vs. gumtree on source statement
    private boolean gtSrcInconsistent = false;

    // our method vs. mtdiff on source statement
    private boolean mtdSrcInconsistent = false;

    // our method vs.
    private boolean ijmSrcInconsistent = false;
    private boolean gtDstInconsistent = false;
    private boolean mtdDstInconsistent = false;
    private boolean ijmDstInconsistent = false;

    AutoEvaluationRecord(){
        super();
    }

    public AutoEvaluationRecord(String project, String commitId, String filePath,
                                ProgramElement srcStmtEle, ProgramElement dstStmtEle,
                                boolean gtSrc, boolean mtdSrc, boolean ijmSrc,
                                boolean gtDst, boolean mtdDst, boolean ijmDst) {
        super(project, commitId, filePath);
        this.gtSrcInconsistent = gtSrc;
        this.mtdSrcInconsistent = mtdSrc;
        this.ijmSrcInconsistent = ijmSrc;
        this.gtDstInconsistent = gtDst;
        this.mtdDstInconsistent = mtdDst;
        this.ijmDstInconsistent = ijmDst;

        this.stmtType = srcStmtEle != null ? srcStmtEle.getNodeType() : dstStmtEle.getNodeType();
        if (srcStmtEle != null) {
            srcStartPos = srcStmtEle.getITreeNode().getPos();
            srcStartLine = srcStmtEle.getStartLine();
        }
        if (dstStmtEle != null) {
            dstStartPos = dstStmtEle.getITreeNode().getPos();
            dstStartLine = dstStmtEle.getStartLine();
        }
    }

    public int getStartPos(boolean isSrc) {
        if (isSrc)
            return srcStartPos;
        else
            return dstStartPos;
    }

    public boolean getRandomSrcOrDst(String method) {
        if (srcStartPos == -1)
            return false;
        if (dstStartPos == -1)
            return true;

        if (method.equals(MappingMethodNames.GT)) {
            if (!isGtSrcInconsistent())
                return false;
            if (!isGtDstInconsistent())
                return true;
        }
        if (method.equals(MappingMethodNames.MTDIFF)) {
            if (!isMtdSrcInconsistent())
                return false;
            if (!isMtdDstInconsistent())
                return true;
        }
        if (method.equals(MappingMethodNames.IJM)) {
            if (!isIjmSrcInconsistent())
                return false;
            if (!isIjmDstInconsistent())
                return true;
        }

        Random rand = new Random(1);
        return rand.nextBoolean();
    }

    public boolean isGtSrcInconsistent() {
        return gtSrcInconsistent;
    }

    public boolean isGtDstInconsistent() {
        return gtDstInconsistent;
    }

    public boolean isMtdSrcInconsistent() {
        return mtdSrcInconsistent;
    }

    public boolean isMtdDstInconsistent() {
        return mtdDstInconsistent;
    }

    public boolean isIjmDstInconsistent() {
        return ijmDstInconsistent;
    }

    public boolean isIjmSrcInconsistent() {
        return ijmSrcInconsistent;
    }

    public String[] toCsvRecord() {
        return new String[] {
                project, commitId, filePath, stmtType,
                Integer.toString(srcStartPos),
                Integer.toString(dstStartPos),
                Integer.toString(srcStartLine),
                Integer.toString(dstStartLine),
                gtSrcInconsistent ? "1" : "0",
                mtdSrcInconsistent ? "1" : "0",
                ijmSrcInconsistent ? "1" : "0",
                gtDstInconsistent ? "1" : "0",
                mtdDstInconsistent ? "1" : "0",
                ijmDstInconsistent ? "1" : "0"
        };
    }

    public static String[] getHeaders() {
        return new String[]{
                "project", "commitId", "filePath", "stmtType",
                "srcStmtPos", "dstStmtPos",
                "srcStmtLine", "dstStmtLine",
                "gtSrc", "mtdSrc", "ijmSrc",
                "gtDst", "mtdDst", "ijmDst"
        };
    }

    public static AutoEvaluationRecord getObjFromCsvRecord(String[] record){
        AutoEvaluationRecord obj = new AutoEvaluationRecord();
        obj.project = record[0];
        obj.commitId = record[1];
        obj.filePath = record[2];
        obj.stmtType = record[3];
        obj.srcStartPos = Integer.parseInt(record[4]);
        obj.dstStartPos = Integer.parseInt(record[5]);
        obj.srcStartLine = Integer.parseInt(record[6]);
        obj.dstStartLine = Integer.parseInt(record[7]);
        obj.gtSrcInconsistent = record[8].equals("1");
        obj.mtdSrcInconsistent = record[9].equals("1");
        obj.ijmSrcInconsistent = record[10].equals("1");
        obj.gtDstInconsistent = record[11].equals("1");
        obj.mtdDstInconsistent = record[12].equals("1");
        obj.ijmDstInconsistent = record[13].equals("1");
        return obj;
    }
}
