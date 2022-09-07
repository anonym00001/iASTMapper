package cs.sysu.algorithm.element;

import java.util.ArrayList;
import java.util.List;

public class ElementTreeUtils {
    /**
     * return a list of every stmts and the stmts ordered using a pre-order
     * @param rootElement a root element
     */
    public static List<ProgramElement> getAllStmtsPreOrder(ProgramElement rootElement) {
        List<ProgramElement> elements = new ArrayList<>();
        preOrder(rootElement, elements);
        return elements;
    }

    private static void preOrder(ProgramElement element, List<ProgramElement> elements) {
        if (element.isStmt())
            elements.add(element);
        List<StmtElement> stmtElements = element.getNearestDescendantStmts();
        for (ProgramElement stmtEle: stmtElements)
            preOrder(stmtEle, elements);
    }

    /**
     * return a list of every stmts and the stmts ordered using a post-order
     * @param rootElement a root element
     */
    public static List<ProgramElement> getAllStmtsPostOrder(ProgramElement rootElement) {
        List<ProgramElement> elements = new ArrayList<>();
        postOrder(rootElement, elements);
        return elements;
    }

    private static void postOrder(ProgramElement element, List<ProgramElement> elements) {
        List<StmtElement> stmtElements = element.getNearestDescendantStmts();
        if (stmtElements.size() > 0){
            for (ProgramElement ele: stmtElements)
                postOrder(ele,  elements);
        }
        if (element.isStmt())
            elements.add(element);
    }

    /**
     * Return a list of every stmts and the stmt ordered using a breath-first order.
     * @param rootElement a root element
     */
    public static List<ProgramElement> getAllStmtsBreathFirst(ProgramElement rootElement){
        List<ProgramElement> ret = new ArrayList<>();
        if (rootElement instanceof StmtElement)
            ret.add(rootElement);
        List<ProgramElement> currents = new ArrayList<>(rootElement.getNearestDescendantStmts());
        while (currents.size() > 0){
            ProgramElement ele = currents.remove(0);
            ret.add(ele);
            currents.addAll(ele.getNearestDescendantStmts());
        }
        return ret;
    }
}
