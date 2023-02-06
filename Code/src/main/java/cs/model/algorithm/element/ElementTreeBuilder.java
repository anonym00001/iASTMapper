package cs.model.algorithm.element;

import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.ttmap.TokenRange;
import cs.model.algorithm.ttmap.TokenRangeTypeMap;
import cs.model.algorithm.ttmap.TreeTokensMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * APIs for building element trees.
 */
public class ElementTreeBuilder {
    /**
     * Build an element tree based on an ITree.
     *
     * @param root root node of ITree
     * @param ttMap a tree token map
     * @param trtMap token and type map
     * @param isSrc if the analyzed file is source file
     * @return the root element of the element tree.
     */
    public static ProgramElement buildElementTree(ITree root, TreeTokensMap ttMap,
                                                  TokenRangeTypeMap trtMap, boolean isSrc){
        Map<ITree, ProgramElement> treeElementMap = new HashMap<>();
        AbstractElement rootEle = new RootElement();
        if (ProgramElement.typeChecker.isStatementNode(root))
            rootEle = new StmtElement();
        rootEle.initNearestDescendantStmts();
        rootEle.initTokenElements();
        rootEle.setRoot(true);
        rootEle.setFromSrc(isSrc);
        rootEle.setITreeNode(root);
        treeElementMap.put(root, rootEle);
        for (ITree t: root.preOrder()){
            if (t == root)
                continue;
            if (ProgramElement.typeChecker.isStatementNode(t)){
                ProgramElement stmtEle = createStmtElement(t, ttMap, isSrc,
                        treeElementMap, trtMap, rootEle);
                treeElementMap.put(t, stmtEle);
            } else {
                if (isJavadocRelated(t))
                    continue;
                ITree stmt = findNearestAncestorStmt(t);
                if (stmt != null) {
                    ProgramElement stmtEle = treeElementMap.get(stmt);
                    InnerStmtElement element = ((StmtElement) stmtEle).addInnerStmtElement(t);

                    ITree parent = t.getParent();
                    ProgramElement parentEle = treeElementMap.get(parent);
                    element.setParentElement(parentEle);
                    element.setChildIdx(parentEle.getInnerStmtElements().size());
                    parentEle.addInnerStmtElement(element);
                    element.setStartLine(ttMap.getStartLineOfNode(t.getPos()));
                    treeElementMap.put(t, element);
                }
            }
        }

        List<ProgramElement> allStmts = ElementTreeUtils.getAllStmtsPreOrder(rootEle);
        for (ProgramElement stmt: allStmts) {
            for (TokenElement token: stmt.getTokenElements()) {
                ProgramElement element = treeElementMap.get(token.getITreeNode());
                if (element.isInnerStmtElement())
                    token.setInnerStmtEleOfToken((InnerStmtElement) element);
            }
        }

        return rootEle;
    }

    private static ITree findNearestAncestorStmt(ITree t) {
        ITree tmp = t;
        while (tmp != null && !ProgramElement.typeChecker.isStatementNode(tmp))
            tmp = tmp.getParent();
        return tmp;
    }

    private static boolean isJavadocRelated(ITree t) {
        ITree tmp = t;
        while (tmp != null) {
            if (ProgramElement.typeChecker.isJavaDoc(tmp))
                return true;
            tmp = tmp.getParent();
        }
        return false;
    }

    private static ProgramElement createStmtElement(ITree stmt, TreeTokensMap ttMap, boolean isSrc,
                                                    Map<ITree, ProgramElement> treeElementMap,
                                                    TokenRangeTypeMap tokenTypeMap,
                                                    ProgramElement rootElement) {
        List<TokenRange> tokens = ttMap.getTokenRangesOfNode(stmt);
        StmtElement stmtEle = new StmtElement();
        stmtEle.initNearestDescendantStmts();
        stmtEle.initTokenElements();
        stmtEle.setFromSrc(isSrc);
        stmtEle.setITreeNode(stmt);
        stmtEle.setStartLine(ttMap.getStartLineOfNode(stmt.getPos()));
        ProgramElement directParentElement = getDirectParentElement(stmtEle,  treeElementMap, rootElement);
        stmtEle.setParentElement(directParentElement);
        stmtEle.setChildIdx(directParentElement.getNearestDescendantStmts().size());
        directParentElement.addNearestDescendantStmts(stmtEle);
        String stmtValue = "";

        ElementNameCalculator calculator = new ElementNameCalculator(stmtEle, ttMap);
        String name = calculator.getName();
        stmtEle.setName(name);

        for (TokenRange token: tokens){
            ITree tokenNode = ttMap.getTokenRangeTreeMap().get(token);
            TokenElement tokenEle = new TokenElement();
            tokenEle.setStartLine(ttMap.getStartLineOfNode(token.first));
            tokenEle.setFromSrc(isSrc);
            tokenEle.setITreeNode(tokenNode);
            tokenEle.setTokenRange(token);
            String tokenValue = ttMap.getTokenByRange(token);
            tokenEle.setStringValue(tokenValue);
            String tokenType = tokenTypeMap.getTokenType(token);
            tokenEle.setTokenType(tokenType);
            tokenEle.setChildIdx(stmtEle.getTokenElements().size());
            tokenEle.setStmtElement(stmtEle);
            tokenEle.setParentElement(stmtEle);
            stmtEle.addTokenElement(tokenEle);
            stmtValue += tokenValue + " ";
        }

        if (stmtEle.getTokenElements().size() > 0)
            stmtEle.setStartLine(stmtEle.getTokenElements().get(0).getStartLine());

        stmtEle.setStringValue(stmtValue);
        return stmtEle;
    }

    private static ProgramElement getDirectParentElement(ProgramElement ele,
                                                         Map<ITree, ProgramElement> treeElementMap,
                                                         ProgramElement rootElement){
        ITree treeNode = ele.getITreeNode();
        ITree tmpNode = treeNode.getParent();
        while (tmpNode != null){
            if (ProgramElement.typeChecker.isStatementNode(tmpNode))
                return treeElementMap.get(tmpNode);
            tmpNode = tmpNode.getParent();
        }
        return rootElement;
    }
}
