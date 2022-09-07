package cs.sysu.algorithm.matcher.fastmatchers;

import cs.sysu.algorithm.element.MethodParameterType;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;

import java.util.*;

/**
 * Fast Method Matcher.
 *
 * Directly maps two methods with identical signature.
 * For method signature, we only consider method name, types of the
 */
public class MethodMatcherWithSameSignature extends BaseFastMatcher {
    private final Map<String, List<ProgramElement>> srcNameMethodMap;
    private final Map<String, List<ProgramElement>> dstNameMethodMap;

    /**
     * Constructor
     * @param srcStmts list of source statement elements
     * @param dstStmts list of target statement elements
     * @param elementMappings current element mappings
     */
    public MethodMatcherWithSameSignature(List<ProgramElement> srcStmts, List<ProgramElement> dstStmts,
                                          ElementMappings elementMappings) {
        super(srcStmts, dstStmts, elementMappings);
        this.srcNameMethodMap = new HashMap<>();
        this.dstNameMethodMap = new HashMap<>();
    }

    public void buildMappings(){
        buildNameMethodMap(srcStmts, srcNameMethodMap);
        buildNameMethodMap(dstStmts, dstNameMethodMap);

        for (String name: srcNameMethodMap.keySet()) {
            List<ProgramElement> srcMethods = srcNameMethodMap.get(name);
            List<ProgramElement> dstMethods = dstNameMethodMap.get(name);
            if (dstMethods == null || dstMethods.size() != 1)
                continue;
//            System.out.println("Src Method is " + srcMethods + "   Dst Method is " + dstMethods);
            buildMapping(srcMethods, dstMethods);
        }
    }

    // only consider to map methods with identical signature (name + type list)
    private void buildMapping(List<ProgramElement> srcMethods, List<ProgramElement> dstMethods) {
        Map<ProgramElement, Set<ProgramElement>> srcToMultiDst = new HashMap<>();
        Map<ProgramElement, Set<ProgramElement>> dstToMultiSrc = new HashMap<>();
        for (ProgramElement srcMethod: srcMethods) {
            for (ProgramElement dstMethod: dstMethods) {
                List<MethodParameterType> typeList1 = ((StmtElement) srcMethod).getMethodTypeList();
                List<MethodParameterType> typeList2 = ((StmtElement) dstMethod).getMethodTypeList();
//                System.out.println("SrecMehod " + srcMethod + " " + dstMethod);
//                System.out.println("Typelist  " + typeList1 + " " + typeList2);
                if (MethodParameterType.isIdenticalMethodParameterTypeList(typeList1, typeList2, elementMappings)){
                    if (!srcToMultiDst.containsKey(srcMethod))
                        srcToMultiDst.put(srcMethod, new HashSet<>());
                    if (!dstToMultiSrc.containsKey(dstMethod))
                        dstToMultiSrc.put(dstMethod, new HashSet<>());
                    srcToMultiDst.get(srcMethod).add(dstMethod);
                    dstToMultiSrc.get(dstMethod).add(srcMethod);
//                    System.out.println("True");
                }
            }
        }

        for (ProgramElement srcEle: srcToMultiDst.keySet()) {
            Set<ProgramElement> dstElements = srcToMultiDst.get(srcEle);
            if (dstElements.size() == 1) {
                ProgramElement dstEle = dstElements.iterator().next();
                if (dstToMultiSrc.get(dstEle).size() == 1){
                    elementMappings.addMapping(srcEle, dstEle);
                }
            }
        }
    }

    private void buildNameMethodMap(List<ProgramElement> stmts, Map<String, List<ProgramElement>> nameMethodMap) {
        for (ProgramElement srcEle: stmts) {
            if (elementMappings.isMapped(srcEle))
                continue;
            if (((StmtElement) srcEle).isMethodDec()) {
                String name = srcEle.getName();
//                System.out.println("src " + srcEle + " name " + name);
                if (!nameMethodMap.containsKey(name))
                    nameMethodMap.put(name, new ArrayList<>());
                nameMethodMap.get(name).add(srcEle);
            }
        }
    }
}
