package cs.model.algorithm.matcher.matchers.searchers;

import cs.model.algorithm.element.InnerStmtElement;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.ProgramElementType;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;

import java.util.*;

public class CandidateSetsAndMaps {
    private final Set<ProgramElement> allSrcStmts;
    private final Set<ProgramElement> srcStmtsToMap;
    private final Set<ProgramElement> srcTokensToMap;
    private final Set<ProgramElement> srcinnerStmtsToMap;
    private final Set<ProgramElement> dstStmtsToMap;
    private final Set<ProgramElement> dstTokensToMap;
    private final Set<ProgramElement> dstinnerStmtsToMap;

    private final Map<ProgramElementType, Set<ProgramElement>> dstTypeElementMap;
    private final Map<String, Set<ProgramElement>> dstValTokenMap;
    private final Map<String, Set<ProgramElement>> dstValMultiTokenElementMap;

    private final ElementMappings fastEleMappings;

    public CandidateSetsAndMaps(ElementMappings fastEleMappings, List<ProgramElement> srcStmts,
                                List<ProgramElement> dstStmts) {
        this.fastEleMappings = fastEleMappings;
        this.dstTypeElementMap = new HashMap<>();
        this.dstValTokenMap = new HashMap<>();
        this.dstValMultiTokenElementMap = new HashMap<>();
        this.allSrcStmts = new HashSet<>(srcStmts);
        this.srcStmtsToMap = new HashSet<>();
        this.srcTokensToMap = new HashSet<>();
        this.srcinnerStmtsToMap = new HashSet<>();
        this.dstStmtsToMap = new HashSet<>();
        this.dstTokensToMap = new HashSet<>();
        this.dstinnerStmtsToMap = new HashSet<>();
        initStmtsAndTokens(srcStmts, dstStmts);
        initMultiTokenElementMap(dstStmts);
        initMap();
    }

    private void initStmtsAndTokens(List<ProgramElement> srcStmts, List<ProgramElement> dstStmts) {
        for (ProgramElement srcStmt: srcStmts) {
            if (!fastEleMappings.isMapped(srcStmt)) {
                this.srcStmtsToMap.add(srcStmt);
            }
            for (ProgramElement tokenEle: srcStmt.getTokenElements()) {
                if (!fastEleMappings.isMapped(tokenEle))
                    this.srcTokensToMap.add(tokenEle);
            }
            for (ProgramElement innerStmtEle: srcStmt.getInnerStmtElements()) {
                if (!fastEleMappings.isMapped(innerStmtEle))
                    this.srcinnerStmtsToMap.add(innerStmtEle);
            }
        }

        for (ProgramElement dstStmt: dstStmts) {
            if (!fastEleMappings.isMapped(dstStmt)) {
                this.dstStmtsToMap.add(dstStmt);
            }
            for (ProgramElement tokenEle: dstStmt.getTokenElements()) {
                if (!fastEleMappings.isMapped(tokenEle)) {
                    this.dstTokensToMap.add(tokenEle);
                    String value = tokenEle.getStringValue();
                    if (!dstValTokenMap.containsKey(value))
                        dstValTokenMap.put(value, new HashSet<>());
                    dstValTokenMap.get(value).add(tokenEle);
                }
                for (ProgramElement innerStmtEle: ((TokenElement) tokenEle).getInnerStmtElementsWithToken()) {
                    if (!fastEleMappings.isMapped(innerStmtEle))
                        this.dstinnerStmtsToMap.add(innerStmtEle);
                }
            }
            addrecursive(dstStmt.getInnerStmtElements());
//            for (ProgramElement innerStmtEle: dstStmt.getInnerStmtElements()) {
//                if (!fastEleMappings.isMapped(innerStmtEle))
//                    this.dstinnerStmtsToMap.add(innerStmtEle);
//            }
        }
    }
    private void addrecursive(List<InnerStmtElement> innerStmtElementList){
        if(innerStmtElementList == null)
            return;
        for (ProgramElement innerStmtEle: innerStmtElementList) {
            if (!fastEleMappings.isMapped(innerStmtEle))
                this.dstinnerStmtsToMap.add(innerStmtEle);
            addrecursive(innerStmtEle.getInnerStmtElements());
        }
    }
    private void initMultiTokenElementMap(List<ProgramElement> dstStmts) {
        for (ProgramElement dstStmt: dstStmts) {
            for (TokenElement tokenEle: dstStmt.getTokenElements()) {
                if (fastEleMappings.isMapped(tokenEle))
                    continue;
                List<InnerStmtElement> elements = tokenEle.getInnerStmtElementsWithToken();
                for (InnerStmtElement element: elements) {
                    if (element.getTokenElements().size() == 1)
                        continue;
                    String value = element.getStringValue();
                    String typeWithValue = element.getNodeType() + ":" + value;
                    if (!dstValMultiTokenElementMap.containsKey(typeWithValue))
                        dstValMultiTokenElementMap.put(typeWithValue, new HashSet<>());
                    dstValMultiTokenElementMap.get(typeWithValue).add(element);
                }
            }
        }
    }

    private void initMap() {
        // target value stmt map
        for (ProgramElement dstStmt: dstStmtsToMap)
            addElementTypeToMap(dstStmt, dstTypeElementMap);

        // target value token map
        for (ProgramElement dstToken: dstTokensToMap)
            addElementTypeToMap(dstToken, dstTypeElementMap);

        // target value inner stmt map
//        System.out.println("the size of the inner stmt is " + dstinnerStmtsToMap.size());
        for (ProgramElement dstInnerStmt: dstinnerStmtsToMap)
            addElementTypeToMap(dstInnerStmt, dstTypeElementMap);
    }

    private void addElementTypeToMap(ProgramElement element, Map<ProgramElementType, Set<ProgramElement>> typeEleMap) {
        ProgramElementType type = element.getElementType();
//        System.out.println("Type is " + type + " " + element);
        if (!typeEleMap.containsKey(type))
            typeEleMap.put(type, new HashSet<>());
        typeEleMap.get(type).add(element);
    }

    public Set<ProgramElement> getAllSrcStmts() {
        return allSrcStmts;
    }

    public Set<ProgramElement> getSrcStmtsToMap() {
        return srcStmtsToMap;
    }

    public Set<ProgramElement> getSrcTokensToMap() {
        return srcTokensToMap;
    }

    public Set<ProgramElement> getDstStmtsToMap() {
        return dstStmtsToMap;
    }

    public Set<ProgramElement> getDstTokensToMap() {
        return dstTokensToMap;
    }

    public Map<ProgramElementType, Set<ProgramElement>> getDstTypeElementMap() {
        return dstTypeElementMap;
    }

    public Map<String, Set<ProgramElement>> getDstValTokenMap() {
        return dstValTokenMap;
    }

    public Map<String, Set<ProgramElement>> getDstValMultiTokenElementMap() {
        return dstValMultiTokenElementMap;
    }

    public Set<ProgramElement> getSameTypeDstCandidates(ProgramElementType type) {
        Set<ProgramElement> ret = new HashSet<>();
        if (dstTypeElementMap.containsKey(type))
            ret.addAll(dstTypeElementMap.get(type));
        return ret;
    }

    public Set<ProgramElement> getSameValDstCandidates(String value) {
        Set<ProgramElement> ret = new HashSet<>();
        if (dstValTokenMap.containsKey(value))
            ret.addAll(dstValTokenMap.get(value));
        return ret;
    }

    public Set<ProgramElement> getSameValDstMultiTokenElements(String typeWithValue) {
        Set<ProgramElement> ret = new HashSet<>();
        if (dstValMultiTokenElementMap.containsKey(typeWithValue))
            ret.addAll(dstValMultiTokenElementMap.get(typeWithValue));
        return ret;
    }
}
