package cs.model.algorithm.matcher.fastmatchers;

import cs.model.algorithm.element.ElementTreeUtils;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.StmtElement;
import cs.model.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.ttmap.TreeTokensMap;

import java.util.*;

/**
 * Fast Stmt Matcher.
 *
 * Maps identical statements.
 * Only identical declarations with identical ancestors can be mapped.
 */
public class SameStmtMatcher extends BaseFastMatcher {
    private final Map<String, Set<ProgramElement>> srcValueStmtMap;
    private final Map<String, Set<ProgramElement>> dstValueStmtMap;
    private final TokenMapStatistics tokenMapStatistics;

    public SameStmtMatcher(List<ProgramElement> srcStmtElements,
                           List<ProgramElement> dstStmtElements,
                           ElementMappings elementMappings,
                           TokenMapStatistics tokenMapStatistics) {
        super(srcStmtElements, dstStmtElements, elementMappings);
        this.srcValueStmtMap = new HashMap<>();
        this.dstValueStmtMap = new HashMap<>();
        this.tokenMapStatistics = tokenMapStatistics;
    }

    @Override
    public void buildMappings(){
        buildValueStmtMap(srcStmts, srcTtMap, srcValueStmtMap);
        buildValueStmtMap(dstStmts, dstTtMap, dstValueStmtMap);

        for (String value: srcValueStmtMap.keySet()){
            Set<ProgramElement> srcStmts = srcValueStmtMap.get(value);
//            System.out.println("elemen " + srcStmts);
//            System.out.println("elemen " + srcStmts + " |And| " + value + " |size| " + srcStmts.size());
            if (srcStmts.size() > 1)
                continue;
            if (!dstValueStmtMap.containsKey(value))
                continue;
            Set<ProgramElement> dstStmts = dstValueStmtMap.get(value);
            if (dstStmts.size() > 1)
                continue;

            ProgramElement srcStmtEle = srcStmts.iterator().next();
            ProgramElement dstStmtEle = dstStmts.iterator().next();
            if (elementMappings.isMapped(srcStmtEle))
                continue;
            if (elementMappings.isMapped(dstStmtEle))
                continue;
//            System.out.println("Src " + srcStmtEle + " || " + dstStmtEle);
            addRecursiveMappings(srcStmtEle, dstStmtEle);
        }
    }

    private void addRecursiveMappings(ProgramElement srcEle, ProgramElement dstEle) {
        List<ProgramElement> srcStmts = ElementTreeUtils.getAllStmtsPreOrder(srcEle);
        List<ProgramElement> dstStmts = ElementTreeUtils.getAllStmtsPreOrder(dstEle);
        for (int i = 0; i < srcStmts.size(); i++) {
            ProgramElement srcStmtEle = srcStmts.get(i);
            ProgramElement dstStmtEle = dstStmts.get(i);
            elementMappings.addMapping(srcStmtEle, dstStmtEle);
//            System.out.println(i + " src " +srcStmtEle + " |And| " + dstStmtEle);
            for (int j = 0; j < srcStmtEle.getInnerStmtElements().size(); j++) {
                ProgramElement srcInnerStmtEle = srcStmtEle.getInnerStmtElements().get(j);
                ProgramElement dstInnerStmtEle = dstStmtEle.getInnerStmtElements().get(j);
//                System.out.println("src inner  " +srcInnerStmtEle + " |And| " + dstInnerStmtEle);
                elementMappings.addMapping(srcInnerStmtEle, dstInnerStmtEle);
            }
//            System.out.println("SrcStmt " + srcStmtEle + " ||| " + dstStmtEle + " " + srcStmtEle.getTokenElements().size() + " " + dstStmtEle.getTokenElements().size());
            // debug, repair the bug about the length error
            for (int j = 0; j < Math.min(srcStmtEle.getTokenElements().size(),dstStmtEle.getTokenElements().size()); j++) {
//            for (int j = 0; j < srcStmtEle.getTokenElements().size(); j++) {
                ProgramElement srcTokenEle = srcStmtEle.getTokenElements().get(j);
                ProgramElement dstTokenEle = dstStmtEle.getTokenElements().get(j);
//                System.out.println("src token  " +srcTokenEle + " |And| " + dstTokenEle);
                elementMappings.addMapping(srcTokenEle, dstTokenEle);
            }
            tokenMapStatistics.recordAllTokenForStmt(srcStmtEle);
            tokenMapStatistics.recordAllTokenForStmt(dstStmtEle);
        }
    }

    private static void buildValueStmtMap(List<ProgramElement> srcStmts,
                                          TreeTokensMap ttMap,
                                          Map<String, Set<ProgramElement>> valueStmtMap){
        for (ProgramElement element: srcStmts){
            if (StaticNodeTypeChecker.getConfigNodeTypeChecker().isBlock(element.getITreeNode()))
                continue;
            String value = ttMap.getNodeContentAndRemoveJavadoc(element.getITreeNode());
//            System.out.println("First value  " + value);
            if (((StmtElement) element).isMethodDec())
                value = getAncestorValue(element) + value;
//            System.out.println("Second value  " + value);
            if (!value.equals("")) {
                if (!valueStmtMap.containsKey(value))
                    valueStmtMap.put(value, new HashSet<>());
                valueStmtMap.get(value).add(element);
            }
        }
    }

    private static String getAncestorValue(ProgramElement element) {
        ProgramElement parent = element.getParentElement();
        String ret = "";
        while (!parent.isRoot()) {
            if (parent.isDeclaration()) {
                ret += "[" + parent.getNodeType() + ":" + parent.getName() + "]";
            }
            parent = parent.getParentElement();
        }
        return ret;
    }
}
