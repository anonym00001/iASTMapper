package cs.sysu.algorithm.matcher.fastmatchers;

import com.github.gumtreediff.tree.ITree;
import cs.sysu.algorithm.element.InnerStmtElement;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.utils.LongestCommonSubsequence;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.sysu.algorithm.matcher.mappings.ElementMappings;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.*;

/**
 * Fast token matcher.
 *
 * First, find the tokens with the same structure
 * Then, match those one-to-one mapped structures first
 * By doing so, in our iterative process, we sidestep a lot of tokens to be processed.
 */
public class SameBigStructureMatcher extends BaseFastMatcher {
    private final Set<ITree> srcMultiTokenNodes = new HashSet<>();
    private final Set<ITree> dstMultiTokenNodes = new HashSet<>();

    private final Map<String, Set<InnerStmtElement>> srcValEleMap = new HashMap<>();
    private final Map<String, Set<InnerStmtElement>> dstValEleMap = new HashMap<>();

    private final static int BIG_STRUCTURE_THRESHOLD = 5;

    public SameBigStructureMatcher(List<ProgramElement> srcStmts, List<ProgramElement> dstStmts,
                                   ElementMappings elementMappings) {
        super(srcStmts, dstStmts, elementMappings);
    }

    public void buildMappings(){
        prepareMultiTokenNodes();
        Set<String> multiTokenValues = new HashSet<>(srcValEleMap.keySet());
        Set<String> dstMultiTokenValues = dstValEleMap.keySet();
//        System.out.println("RetainAll : " + multiTokenValues);
        multiTokenValues.retainAll(dstMultiTokenValues);
//        System.out.println("set string : " + dstMultiTokenValues);
//        System.out.println("ALL Token values : " + multiTokenValues);

        for (String val: multiTokenValues){
            Set<InnerStmtElement> srcElements = srcValEleMap.get(val);
            Set<InnerStmtElement> dstElements = dstValEleMap.get(val);
            addElementToMap(srcElements, dstElements);
        }
    }

    private void addElementToMap(Set<InnerStmtElement> srcElements,
                                 Set<InnerStmtElement> dstElements){
        boolean addMapping = srcElements.size() == 1 && dstElements.size() == 1;
        if (addMapping) {
            InnerStmtElement srcEle = srcElements.iterator().next();
            InnerStmtElement dstEle = dstElements.iterator().next();
//            System.out.println("Src is " + srcEle + " Src Size is " + srcEle.getTokenElements().size());
//            System.out.println("dst is " + dstEle + " Dst Size is " + dstEle.getTokenElements().size());
            if (srcEle.getTokenElements().size() >= BIG_STRUCTURE_THRESHOLD) {
                boolean canBeMapped = determineCanMap(srcEle, dstEle);
                addMappingForMultiTokenElement(srcEle, dstEle, canBeMapped);
//                System.out.println("Src is " + srcEle + " || " + dstEle);
                addRecursiveMapping(srcEle, dstEle);
            }
        }
    }

    private List<InnerStmtElement> getPreorder(InnerStmtElement Ele){
        List<InnerStmtElement> res = new ArrayList<>();
        List<InnerStmtElement> tmp = new ArrayList<>();
        tmp.add(Ele);
        while (tmp.size() != 0){
            res.add(tmp.get(0));
            for (int i = 0; i < tmp.get(0).getInnerStmtElements().size(); i++) {
//                System.out.println("order " + tmp.get(0).getStringValue());
                tmp.add(tmp.get(0).getInnerStmtElements().get(i));
            }
            tmp.remove(0);
        }
        return res;
    }

    private void addRecursiveMapping(InnerStmtElement srcEle, InnerStmtElement dstEle) {
//        for (int i = 0; i < srcEle.getInnerStmtElements().size(); i++) {
        // repair the bug that causal by the different size of the srcEle InnerStmt and the dstEle InnerStmt using LCS match

        if(srcEle.getInnerStmtElements().size() != dstEle.getInnerStmtElements().size()) {
            List<InnerStmtElement> srcSubInnerStmts = getPreorder(srcEle);
            List<InnerStmtElement> dstSubInnerStmts = getPreorder(dstEle);
            LongestCommonSubsequence<InnerStmtElement> lcs = new LongestCommonSubsequence<InnerStmtElement>(srcSubInnerStmts, dstSubInnerStmts) {
                @Override
                public boolean isEqual(InnerStmtElement t1, InnerStmtElement t2) {
                    return t1.equalValue(t2);
                }
            };
            List<int[]> mappedIdxes = lcs.extractIdxes();
            for (int[] idxes: mappedIdxes){
                elementMappings.addMapping(srcSubInnerStmts.get(idxes[0]), dstSubInnerStmts.get(idxes[1]));
            }
        }
        else{
            for (int i = 0; i < srcEle.getInnerStmtElements().size(); i++) {
    //            System.out.println(srcEle + " || " + dstEle + "  i is " + i + " " + srcEle.getInnerStmtElements().size() + " " + dstEle.getInnerStmtElements().size());
                InnerStmtElement srcChildEle = srcEle.getInnerStmtElements().get(i);
                InnerStmtElement dstChildEle = dstEle.getInnerStmtElements().get(i);
    //            System.out.println("Src child " + srcChildEle + " Dst child " + dstChildEle);
                elementMappings.addMapping(srcChildEle, dstChildEle);
                addRecursiveMapping(srcChildEle, dstChildEle);
            }
        }
    }

    private boolean determineCanMap(InnerStmtElement srcEle, InnerStmtElement dstEle) {
        ProgramElement srcStmtEle = srcEle.getStmtElement();
        ProgramElement dstStmtEle = dstEle.getStmtElement();
//        System.out.println("Src and dst " + srcStmtEle + "   " + dstStmtEle);
        if (elementMappings.getDstForSrc(srcStmtEle) == dstStmtEle)
            return true;
        // SingleVariableDeclaration in for loop or method declaration
        // may not be mapped across different statements.
        return !StaticNodeTypeChecker
                .getConfigNodeTypeChecker()
                .isDescendantOfSingleVariableDeclaration(srcEle.getITreeNode());
    }

    private void addMappingForMultiTokenElement(InnerStmtElement srcEle,
                                                InnerStmtElement dstEle,
                                                boolean canBeMapped){
        List<TokenElement> srcTokenElements = srcEle.getTokenElements();
        List<TokenElement> dstTokenElements = dstEle.getTokenElements();

        for (int i = 0; i < srcTokenElements.size(); i++){
            ProgramElement srcTokenEle = srcTokenElements.get(i);
            ProgramElement dstTokenEle = dstTokenElements.get(i);
            if (canBeMapped) {
                if (!elementMappings.isMapped(srcTokenEle) && !elementMappings.isMapped(dstTokenEle)) {
                    elementMappings.addMapping(srcTokenEle, dstTokenEle);
                }
            }
        }
    }

    private void prepareMultiTokenNodes(){
        buildValInnerStmtEleMap(srcStmts);
        buildValInnerStmtEleMap(dstStmts);
    }

    private void buildValInnerStmtEleMap(List<ProgramElement> stmts) {
//        int num = 0;
        for (ProgramElement stmt: stmts){
            if (statistics.isAllTokenMapped(stmt))
                continue;
//            System.out.println(num + " Stmt is " + stmt);
            for (TokenElement token: stmt.getTokenElements()) {
//                System.out.println("Token is " + token);
                if (elementMappings.isMapped(token))
                    continue;
                List<InnerStmtElement> multiTokenElements = token.getMultiTokenElementsWithToken();
                for (InnerStmtElement innerStmtElement: multiTokenElements)
                    addInnerStmtElementToValMap(innerStmtElement);
            }
//            num++;
        }
    }

    private void addInnerStmtElementToValMap(InnerStmtElement element) {
        if (element.isFromSrc() && srcMultiTokenNodes.contains(element.getITreeNode()))
            return;
        if (!element.isFromSrc() && dstMultiTokenNodes.contains(element.getITreeNode()))
            return;

        String value = element.getStringValue();
        value = element.getNodeType() + ": " + value;
//        System.out.println("The value is " + value + " inner " + element);
        if (element.isFromSrc()) {
            srcMultiTokenNodes.add(element.getITreeNode());
            if (!srcValEleMap.containsKey(value))
                srcValEleMap.put(value, new HashSet<>());
            srcValEleMap.get(value).add(element);
        }

        if (!element.isFromSrc()) {
            dstMultiTokenNodes.add(element.getITreeNode());
            if (!dstValEleMap.containsKey(value))
                dstValEleMap.put(value, new HashSet<>());
            dstValEleMap.get(value).add(element);
        }
    }
}
