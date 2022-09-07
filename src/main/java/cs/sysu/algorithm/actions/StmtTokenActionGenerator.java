package cs.sysu.algorithm.actions;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.utils.LongestCommonSubsequence;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;

import java.util.*;

/**
 * Stmt-Token Edit Action Generator
 */
public class StmtTokenActionGenerator {
    private Map<ProgramElement, Set<ProgramElement>> subSrcStmtsInLcs;
    private Map<ProgramElement, Set<ProgramElement>> subSrcTokenInLcs;

    private ElementMappings eleMappings;
    private List<ProgramElement> srcStmts;
    private List<ProgramElement> dstStmts;

    /**
     * Constructor
     * @param srcStmts all the source statements
     * @param dstStmts all the target statements
     * @param eleMappings element mappings
     */
    public StmtTokenActionGenerator(List<ProgramElement> srcStmts,
                                    List<ProgramElement> dstStmts,
                                    ElementMappings eleMappings){
        this.srcStmts = srcStmts;
        this.dstStmts = dstStmts;
        this.eleMappings = eleMappings;
        this.subSrcStmtsInLcs = new HashMap<>();
        this.subSrcTokenInLcs = new HashMap<>();
        prepare();
    }

    /**
     * Generate a list of StmtTokenAction
     * @param includeNonChanged whether include non-changed statements
     * @return a list of stmt-token actions
     */
    public List<StmtTokenAction> generateActions(boolean includeNonChanged){
        Set<ProgramElement> calculatedDstStmts = new HashSet<>();
        List<StmtTokenAction> actionList = new ArrayList<>();
        for (ProgramElement srcStmt: srcStmts){
            ProgramElement dstStmt = eleMappings.getDstForSrc(srcStmt);
            StmtTokenAction action = new StmtTokenAction(srcStmt, dstStmt, eleMappings, subSrcStmtsInLcs, subSrcTokenInLcs);
            if (!includeNonChanged && !action.hasAction()) {
                if (dstStmt != null)
                    calculatedDstStmts.add(dstStmt);
                continue;
            }
            actionList.add(action);
            if (dstStmt != null)
                calculatedDstStmts.add(dstStmt);
        }
        for (ProgramElement dstStmt: dstStmts){
            if (calculatedDstStmts.contains(dstStmt))
                continue;
            StmtTokenAction action = new StmtTokenAction(null, dstStmt, eleMappings, subSrcStmtsInLcs, subSrcTokenInLcs);
            actionList.add(action);
        }
        return actionList;
    }

    /**
     * Sort given actions in lcs order
     * @param originalActions the given actions
     * @return the ordered actions
     */
    public List<StmtTokenAction> reorderActions(List<StmtTokenAction> originalActions){
        Map<ProgramElement, StmtTokenAction> srcStmtActionMap = new HashMap<>();
        Map<ProgramElement, StmtTokenAction> dstStmtActionMap = new HashMap<>();

        for (StmtTokenAction action: originalActions){
            ProgramElement srcStmtEle = action.getSrcStmtEle();
            ProgramElement dstStmtEle = action.getDstStmtEle();
            if (srcStmtEle != null)
                srcStmtActionMap.put(srcStmtEle, action);
            if (dstStmtEle != null)
                dstStmtActionMap.put(dstStmtEle, action);
        }

        LongestCommonSubsequence<ProgramElement> lcs = new LongestCommonSubsequence<ProgramElement>(srcStmts, dstStmts) {
            @Override
            public boolean isEqual(ProgramElement t1, ProgramElement t2) {
                return eleMappings.getDstForSrc(t1) == t2;
            }
        };
        List<StmtTokenAction> ret = new ArrayList<>();
        List<int[]> idxPairList = lcs.extractIdxes();
        Set<ProgramElement> addedDstStmts = new HashSet<>();
        idxPairList.add(new int[]{srcStmts.size(), dstStmts.size()});
        int startSrcIdx = 0;
        int startDstIdx = 0;
        for (int[] pair: idxPairList){
            int endSrcIdx = pair[0];
            int endDstIdx = pair[1];
            List<StmtTokenAction> actionList = getStmtTokenActionsBetweenMappedStmtPairs(startSrcIdx, startDstIdx,
                    endSrcIdx, endDstIdx, addedDstStmts, srcStmtActionMap, dstStmtActionMap);
            ret.addAll(actionList);
            startSrcIdx = endSrcIdx;
            startDstIdx = endDstIdx;
        }

        return ret;
    }

    private List<StmtTokenAction> getStmtTokenActionsBetweenMappedStmtPairs(int startSrcIdx, int startDstIdx,
                                                                            int endSrcIdx, int endDstIdx,
                                                                            Set<ProgramElement> addedDstStmts,
                                                                            Map<ProgramElement, StmtTokenAction> srcStmtActionMap,
                                                                            Map<ProgramElement, StmtTokenAction> dstStmtActionMap){
        List<StmtTokenAction> ret = new ArrayList<>();
        for (int i = startSrcIdx; i < endSrcIdx; i++){
            ProgramElement srcEle = srcStmts.get(i);
            if (!srcStmtActionMap.containsKey(srcEle))
                continue;
            if (eleMappings.isMapped(srcEle)) {
                if (addedDstStmts.contains(eleMappings.getDstForSrc(srcEle)))
                    continue;
                addedDstStmts.add(eleMappings.getDstForSrc(srcEle));
                ret.add(srcStmtActionMap.get(srcEle));
            } else {
                ret.add(srcStmtActionMap.get(srcEle));
            }
        }

        for (int i = startDstIdx; i < endDstIdx; i++){
            ProgramElement dstEle = dstStmts.get(i);
            if (!dstStmtActionMap.containsKey(dstEle))
                continue;
            if (addedDstStmts.contains(dstEle))
                continue;
            addedDstStmts.add(dstEle);
            ret.add(dstStmtActionMap.get(dstEle));
        }
        return ret;
    }

    private void prepare(){
        for (ProgramElement srcStmt: srcStmts){
            if (!eleMappings.isMapped(srcStmt))
                continue;
            ProgramElement dstStmt = eleMappings.getDstForSrc(srcStmt);
            List<StmtElement> srcSubStmts = srcStmt.getNearestDescendantStmts();
            List<StmtElement> dstSubStmts = dstStmt.getNearestDescendantStmts();
            if (srcSubStmts.size() <= 1)
                continue;
            if (dstSubStmts.size() <= 1)
                continue;
            LongestCommonSubsequence<StmtElement> lcs = new LongestCommonSubsequence<StmtElement>(srcSubStmts, dstSubStmts) {
                @Override
                public boolean isEqual(StmtElement t1, StmtElement t2) {
                    return eleMappings.getDstForSrc(t1) == t2;
                }
            };
            List<int[]> mappedIdxes = lcs.extractIdxes();
            Set<ProgramElement> elements = new HashSet<>();
            for (int[] idxes: mappedIdxes){
                elements.add(srcSubStmts.get(idxes[0]));
            }
            subSrcStmtsInLcs.put(srcStmt, elements);
        }

        for (ProgramElement srcStmt: srcStmts){
            if (!eleMappings.isMapped(srcStmt))
                continue;
            ProgramElement dstStmt = eleMappings.getDstForSrc(srcStmt);
            List<TokenElement> srcTokenElements = srcStmt.getTokenElements();
            List<TokenElement> dstTokenElements = dstStmt.getTokenElements();
            if (srcTokenElements.size() <= 1)
                continue;
            if (dstTokenElements.size() <= 1)
                continue;
            LongestCommonSubsequence<TokenElement> lcs =
                    new LongestCommonSubsequence<TokenElement>(srcTokenElements, dstTokenElements) {
                @Override
                public boolean isEqual(TokenElement t1, TokenElement t2) {
                    return eleMappings.getDstForSrc(t1) == t2;
                }
            };
            List<int[]> mappedIdxes = lcs.extractIdxes();
            Set<ProgramElement> elements = new HashSet<>();
            for (int[] idxes: mappedIdxes) {
                elements.add(srcTokenElements.get(idxes[0]));
            }
            subSrcTokenInLcs.put(srcStmt, elements);
        }
    }

}
