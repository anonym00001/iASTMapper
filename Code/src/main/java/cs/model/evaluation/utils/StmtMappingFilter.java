package cs.model.evaluation.utils;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;

public class StmtMappingFilter {
    private ProgramElement srcStmtEle;
    private ProgramElement dstStmtEle;
    private ElementMappings eleMappings;
    private String typename;

    public StmtMappingFilter(ProgramElement srcStmtEle, ProgramElement dstStmtEle, ElementMappings eleMappings){
        this.srcStmtEle = srcStmtEle;
        this.dstStmtEle = dstStmtEle;
        this.eleMappings = eleMappings;
        this.typename = getElementNodeType();
    }

    private String getElementNodeType(){
        if (srcStmtEle != null)
            return srcStmtEle.getNodeType();
        else
            return dstStmtEle.getNodeType();
    }

    private boolean isImportDec(){
         return typename.equals("ImportDeclaration");
    }

    private boolean isBlock(){
        return typename.equals("Block");
    }

    private boolean isEqualParent() {
        ProgramElement srcParentEle = srcStmtEle.getParentElement();
        ProgramElement dstParentEle = dstStmtEle.getParentElement();
        if (srcParentEle != null && dstParentEle != null)
            return srcParentEle.equalValue(dstParentEle);
        return false;
    }

    private boolean isParentMapping() {
        ProgramElement srcParentEle = srcStmtEle.getParentElement();
        ProgramElement dstParentEle = dstStmtEle.getParentElement();
        if (srcParentEle != null && dstParentEle != null)
            return eleMappings.getDstForSrc(srcParentEle) == dstParentEle;
        return false;
    }

    public boolean isGoodMapping(){
        if (isImportDec())
            return true;

        if (srcStmtEle == null || dstStmtEle == null)
            return true;

        if (isBlock())
            return true;

        return false;
    }

    public boolean goodMappingAccordingRules() {

        if (srcStmtEle.equalValue(dstStmtEle) && isParentMapping()) {
            return true;
        }

        return false;
    }
}
