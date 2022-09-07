package cs.sysu.evaluation.csvrecord.compare;


import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;

public class TokenComparisonRecord extends ComparisonRecord {

    private String project;
    private String commitId;
    private String filePath;
    private int srcStartPos = -1;
    private int srcEndPos = -1;
    private int dstStartPos = -1;
    private int dstEndPos = -1;
    private int srcLine = -1;
    private String srcTokenValue = "";
    private int srcIdx = -1;
    private int dstLine = -1;
    private String dstTokenValue = "";
    private int dstIdx = -1;
    private String accurate = "";

    private ProgramElement srcTokenEle;
    private ProgramElement dstTokenEle;

    public TokenComparisonRecord(String project, String commitId, String filePath,
                                 boolean gtInconsistent, boolean mtdInconsistent, boolean ijmInconsistent,
                                 ProgramElement srcEle, ProgramElement dstEle){
        super(gtInconsistent, mtdInconsistent, ijmInconsistent);
        this.project = project;
        this.commitId = commitId;
        this.filePath = filePath;

        TokenElement srcToken = (TokenElement) srcEle;
        TokenElement dstToken = (TokenElement) dstEle;

        if (srcEle != null) {
            this.srcStartPos = srcToken.getTokenRange().first;
            this.srcEndPos = srcToken.getTokenRange().second;
            this.srcLine = srcEle.getStartLine();
            this.srcTokenValue = srcEle.getStringValue();
            this.srcIdx = srcEle.getChildIdx();
        }

        if (dstEle != null) {
            this.dstStartPos = dstToken.getTokenRange().first;
            this.dstEndPos = dstToken.getTokenRange().second;
            this.dstLine = dstEle.getStartLine();
            this.dstTokenValue = dstEle.getStringValue();
            this.dstIdx = dstEle.getChildIdx();
            this.srcTokenEle = srcEle;
            this.dstTokenEle = dstEle;
        }
    }

    public String getScript(){
        if (srcTokenEle == null)
            return "**ADD** " + dstTokenEle;
        if (dstTokenEle == null)
            return "**DEL** " + srcTokenEle;
        return "**MAP** " + srcTokenEle + " => " + dstTokenEle;
    }

    public String[] toRecord(ElementMappings eleMappings){
        String[] record = {
                project,
                commitId,
                filePath,
                Integer.toString(srcStartPos),
                Integer.toString(srcEndPos),
                Integer.toString(dstStartPos),
                Integer.toString(dstEndPos),
                Integer.toString(srcLine),
                srcTokenValue,
                Integer.toString(srcIdx),
                Integer.toString(dstLine),
                dstTokenValue,
                Integer.toString(dstIdx),
                gtInconsistent ? "1" : "0",
                mtdInconsistent ? "1" : "0",
                ijmInconsistent ? "1" : "0",
                ""
        };
        return record;
    }

    public static String[] getTokenHeaders() {
        String[] headers = {
                "commitId", "filePath", "srcStmtLine", "srcToken", "srcIndex",
                "dstStmtLine", "dstToken", "dstIndex", "gtInconsistent", "mtdInconsistent", "ijmInconsistent",
                "accurate"
        };
        return headers;
    }
}
