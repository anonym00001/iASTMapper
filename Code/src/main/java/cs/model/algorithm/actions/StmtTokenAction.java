package cs.model.algorithm.actions;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;

import java.util.*;

/**
 * Stmt-Token Edit Action
 *
 * Definition of edit actions on statements and tokens for a pair of mapped statements.
 * For statement, edit actions include ADD, DEL, MOV, EXC, UPD, M&U, E&U
 * For token, edit actions include ADD, DEL, MOV (move to other stmt), EXC, UPD, M&U, E&U
 *
 * Reason:
 * 1. Such edit actions are not sensitive towards the trees used by different mapping algorithms.
 * 2. It is more convenient to visualize the mappings of statements and tokens.
 */
public class StmtTokenAction {
    private ProgramElement srcStmtEle;
    private ProgramElement dstStmtEle;

    // sort the mapping of statements using lcs
    // key: parent element
    // value: nearest descendant element
    private Map<ProgramElement, Set<ProgramElement>> subSrcStmtsInLcs;

    // sort the mapping of tokens using lcs
    // key: a statement element
    // value: tokens of the statement
    private Map<ProgramElement, Set<ProgramElement>> subSrcTokenInLcs;

    private ElementMappings eleMappings;
    private List<TokenElement> srcTokenElements = new ArrayList<>();
    private List<TokenElement> dstTokenElements = new ArrayList<>();

    /**
     * Constructor
     * @param srcStmtEle source statement
     * @param dstStmtEle target statement
     * @param eleMappings element mappings
     * @param subSrcStmtsInLcs source statements sorted by lcs
     * @param subSrcTokenInLcs source tokens sorted by lcs
     */
    public StmtTokenAction(ProgramElement srcStmtEle, ProgramElement dstStmtEle, ElementMappings eleMappings,
                           Map<ProgramElement, Set<ProgramElement>> subSrcStmtsInLcs,
                           Map<ProgramElement, Set<ProgramElement>> subSrcTokenInLcs){
        this.srcStmtEle = srcStmtEle;
        this.dstStmtEle = dstStmtEle;
        this.eleMappings = eleMappings;
        if (srcStmtEle != null)
            srcTokenElements = srcStmtEle.getTokenElements();
        if (dstStmtEle != null)
            dstTokenElements = dstStmtEle.getTokenElements();
        this.subSrcStmtsInLcs = subSrcStmtsInLcs;
        this.subSrcTokenInLcs = subSrcTokenInLcs;
    }

    /**
     * Get a map between element and action type
     */
    public Map<ProgramElement, String> getElementActionMap(){
        Map<ProgramElement, String> eleActionMap = new HashMap<>();
        String stmtActionType = getStmtActionType();
        if (srcStmtEle != null){
            eleActionMap.put(srcStmtEle, stmtActionType);
            for (ProgramElement tokenEle: srcStmtEle.getTokenElements()){
                String tokenActionType = getTokenActionType(tokenEle);
                eleActionMap.put(tokenEle, tokenActionType);
            }
        }
        if (dstStmtEle != null){
            eleActionMap.put(dstStmtEle, stmtActionType);
            for (ProgramElement tokenEle: dstStmtEle.getTokenElements()){
                String tokenActionType = getTokenActionType(tokenEle);
                eleActionMap.put(tokenEle, tokenActionType);
            }
        }
        return eleActionMap;
    }

    public ProgramElement getSrcStmtEle() {
        return srcStmtEle;
    }

    public ProgramElement getDstStmtEle() {
        return dstStmtEle;
    }

    public boolean hasAction(){
//        ProgramElement srcParentEle = srcStmtEle.getParentElement();
//        if (subSrcStmtsInLcs.containsKey(srcParentEle))
//            System.out.println("stmt is " + srcStmtEle + " " + subSrcStmtsInLcs.get(srcParentEle));
        String stmtAction = getStmtActionType();
        return !stmtAction.equals("***");
    }

    @Override
    public String toString() {
        String stmtActionType = getStmtActionType();
        if (stmtActionType.equals("***"))
            return "***";
        String ret = "==================================================";
        ret += "\n";
        ret += "**" + stmtActionType + "**:";
        if (srcStmtEle == null)
            ret += dstStmtEle.toString() + "\n";
        else{
            ret += srcStmtEle.toString();
            if (dstStmtEle != null){
                ret += " => " + dstStmtEle.toString() + "\n";
            } else {
                ret += "\n";
            }
        }

        if (!(stmtActionType.equals("EXC") || stmtActionType.equals("MOV"))) {
            ret += "\nTOKEN MAPPING:\n";
            List<String> tokenMappingInfo = getTokenMappingInfo();
            for (String str : tokenMappingInfo)
                ret += str + "\n";
        }

        return ret;
    }

    private List<String> getTokenMappingInfo(){
        List<String> ret = new ArrayList<>();
        Set<ProgramElement> calculatedDstTokens = new HashSet<>();
        for (ProgramElement srcTokenEle: srcTokenElements){
            ProgramElement dstTokenEle = eleMappings.getDstForSrc(srcTokenEle);
            String type = getTokenActionType(srcTokenEle);
            String tokenInfo = "**" + type + "**: " + srcTokenEle.toString();
            if (dstTokenEle != null){
                tokenInfo += " => " + dstTokenEle.toString();
                calculatedDstTokens.add(dstTokenEle);
            }
            ret.add(tokenInfo);
        }

        for (ProgramElement dstTokenEle: dstTokenElements){
            if (calculatedDstTokens.contains(dstTokenEle))
                continue;
            ProgramElement srcTokenEle = eleMappings.getSrcForDst(dstTokenEle);
            String type = getTokenActionType(dstTokenEle);
            String tokenInfo = "**" + type + "**: ";
            if (srcTokenEle != null){
                tokenInfo += srcTokenEle.toString() + " => ";
            }
            tokenInfo += dstTokenEle.toString();
            ret.add(tokenInfo);
        }
        return ret;
    }

    private String getStmtActionType(){
        if (srcStmtEle == null)
            return "ADD";
        if (dstStmtEle == null)
            return "DEL";
        boolean moved = isStmtMoved();
        boolean exchanged = isStmtExchanged();
        boolean updated = isStmtUpdated();

        if (moved && updated)
            return "M&U";
        if (exchanged && updated)
            return "E&U";
        if (moved)
            return "MOV";
        if (exchanged)
            return "EXC";
        if (updated)
            return "UPD";
        return "***";
    }

    private String getTokenActionType(ProgramElement tokenEle){
        if (!eleMappings.isMapped(tokenEle)){
            if (tokenEle.isFromSrc())
                return "DEL";
            else
                return "ADD";
        }

        boolean moved = isTokenMove(tokenEle);
        boolean updated = isTokenUpdate(tokenEle);
        boolean exchanged = isTokenExchanged(tokenEle, moved);

        if (moved && updated)
            return "M&U";
        if (exchanged && updated)
            return "E&U";
        if (moved)
            return "MOV";
        if (exchanged)
            return "EXC";
        if (updated)
            return "UPD";
        return "***";
    }

    private boolean isStmtMoved(){
        ProgramElement srcParentEle = srcStmtEle.getParentElement();
        ProgramElement dstParentEle = dstStmtEle.getParentElement();
        if (srcParentEle == null && dstParentEle != null)
            return true;
        if (srcParentEle != null && dstParentEle == null)
            return true;
        if (srcParentEle == null)
            return false;
        if (eleMappings.getDstForSrc(srcParentEle) == dstParentEle)
            return false;
        return true;
    }

    private boolean isStmtExchanged() {
        if (srcStmtEle.getParentElement() == null)
            return false;
        if (srcStmtEle.isDeclaration())
            return false;
        ProgramElement srcParentEle = srcStmtEle.getParentElement();
        if (subSrcStmtsInLcs.containsKey(srcParentEle))
            return !subSrcStmtsInLcs.get(srcParentEle).contains(srcStmtEle);
        return false;
    }

    private boolean isStmtUpdated(){
        if (srcStmtEle == null || dstStmtEle == null)
            return false;
        return !srcStmtEle.equalValue(dstStmtEle);
    }

    private boolean isTokenExchanged(ProgramElement tokenEle, boolean moved){
        if (!eleMappings.isMapped(tokenEle))
            return false;
        if (moved)
            return false;
        ProgramElement srcTokenEle = tokenEle;
        if (!tokenEle.isFromSrc())
            srcTokenEle = eleMappings.getSrcForDst(tokenEle);
        ProgramElement srcStmt = srcTokenEle.getStmtElement();
        if (subSrcTokenInLcs.containsKey(srcStmt))
            return !subSrcTokenInLcs.get(srcStmt).contains(srcTokenEle);
        return false;
    }

    private boolean isTokenUpdate(ProgramElement tokenEle){
        boolean isSrc = tokenEle.isFromSrc();
        if (isSrc) {
            ProgramElement dstTokenEle = eleMappings.getDstForSrc(tokenEle);
            if (!tokenEle.equalValue(dstTokenEle))
                return true;
        } else {
            ProgramElement srcTokenEle = eleMappings.getSrcForDst(tokenEle);
            if (!srcTokenEle.equalValue(tokenEle))
                return true;
        }
        return false;
    }

    private boolean isTokenMove(ProgramElement tokenEle){
        boolean isSrc = tokenEle.isFromSrc();
        if (isSrc){
            ProgramElement dstTokenEle = eleMappings.getDstForSrc(tokenEle);
            ProgramElement srcStmtEle = tokenEle.getStmtElement();
            ProgramElement dstStmtEle = dstTokenEle.getStmtElement();
            if (eleMappings.getDstForSrc(srcStmtEle) != dstStmtEle)
                return true;
        } else {
            ProgramElement srcTokenEle = eleMappings.getSrcForDst(tokenEle);
            ProgramElement srcStmtEle = srcTokenEle.getStmtElement();
            ProgramElement dstStmtEle = tokenEle.getStmtElement();
            if (eleMappings.getDstForSrc(srcStmtEle) != dstStmtEle)
                return true;
        }
        return false;
    }
}
