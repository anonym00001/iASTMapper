package cs.model.evaluation.csvrecord.eval;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.evaluation.csvrecord.eval.EvaluationRecord;

import java.util.Map;


public class ManualEvaluationRecord extends EvaluationRecord {
    private String method;
    private ProgramElement stmtEle;
    private ElementMappings mappings;

    public ManualEvaluationRecord(String project, String commitId, String filePath,
                                  String method, ProgramElement stmtEle) {
        super(project, commitId, filePath);
        this.method = method;
        this.stmtEle = stmtEle;
    }

    public void setElementMappings(ElementMappings mappings) {
        this.mappings = mappings;
        if (stmtEle.isFromSrc()) {
            stmtType = stmtEle.getNodeType();
            srcStartPos = stmtEle.getITreeNode().getPos();
            srcStartLine = stmtEle.getStartLine();

            ProgramElement dstStmtEle = mappings.getMappedElement(stmtEle);
            if (dstStmtEle != null) {
                dstStartPos = dstStmtEle.getITreeNode().getPos();
                dstStartLine = dstStmtEle.getStartLine();
            }

        } else {
            stmtType = stmtEle.getNodeType();
            dstStartPos = stmtEle.getITreeNode().getPos();
            dstStartLine = stmtEle.getStartLine();

            ProgramElement srcStmtEle = mappings.getMappedElement(stmtEle);
            if (srcStmtEle != null) {
                srcStartPos = srcStmtEle.getITreeNode().getPos();
                srcStartLine = srcStmtEle.getStartLine();
            }
        }
    }

    public String getScript(Map<ProgramElement, String> elementActionMap) {
        String script = "=================================================\n";
        if (stmtEle.isFromSrc()) {
            ProgramElement dstStmtEle = mappings.getMappedElement(stmtEle);
            if (dstStmtEle == null)
                script += "**DEL** " + stmtEle;
            else
                script += "**" + elementActionMap.get(stmtEle) + "** " + stmtEle + " => " + dstStmtEle;
        } else {
            ProgramElement srcStmtEle = mappings.getMappedElement(stmtEle);
            if (srcStmtEle == null)
                script += "**ADD** " + stmtEle;
            else
                script += "**" + elementActionMap.get(stmtEle) + "** " + srcStmtEle + " => " + stmtEle;
        }
        script += "\n\n";

        for (ProgramElement tokenEle: stmtEle.getTokenElements()) {
            if (tokenEle.isFromSrc()) {
                ProgramElement dstTokenEle = mappings.getMappedElement(tokenEle);
                if (dstTokenEle == null)
                    script += "**DEL** " + tokenEle;
                else
                    script += "**" + elementActionMap.get(tokenEle) + "** " + tokenEle + " => " + dstTokenEle;
                script += "\n";
            } else {
                ProgramElement srcTokenEle = mappings.getMappedElement(tokenEle);
                if (srcTokenEle == null)
                    script += "**ADD** " + tokenEle;
                else
                    script += "**" + elementActionMap.get(tokenEle) + "** " + srcTokenEle + " => " + tokenEle;
                script += "\n";
            }
        }
        return script;
    }


    public String[] toCsvRecord() {
        String[] record = {
                project, method, commitId, filePath, stmtType,
                Integer.toString(srcStartPos),
                Integer.toString(dstStartPos),
                Integer.toString(srcStartLine),
                Integer.toString(dstStartLine),
                "",
                ""
        };
        return record;
    }

    public static String[] getHeaders() {
        String[] headers = {
                "project", "method", "commitId", "filePath", "stmtType", "srcStartPos", "dstStartPos",
                "srcStartLine", "dstStartLine", "stmtAccurate", "tokenAccurate"
        };
        return headers;
    }
}
