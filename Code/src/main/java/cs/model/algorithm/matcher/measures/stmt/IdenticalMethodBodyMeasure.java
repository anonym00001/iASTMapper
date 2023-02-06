package cs.model.algorithm.matcher.measures.stmt;

import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.StmtElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;

public class IdenticalMethodBodyMeasure extends AbstractSimMeasure implements SimMeasure {
    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        StmtElement srcStmt = (StmtElement) srcEle;
        StmtElement dstStmt = (StmtElement) dstEle;
        if (srcStmt.isMethodDec()) {
            ITree srcMethodBody = getMethodBody(srcStmt.getITreeNode());
            ITree dstMethodBody = getMethodBody(dstStmt.getITreeNode());
//            System.out.println("Src Stmt MethodDec  " + srcStmt + "  Dst stmt MethodDec   "+ dstStmt + "  " + srcMethodBody.isIsomorphicTo(dstMethodBody));
//            System.out.println("Src Stmt MethodDec  " + srcMethodBody + "  Dst stmt MethodDec   "+ dstMethodBody);
            if (srcMethodBody == null || dstMethodBody == null)
                return 0;
            if (srcMethodBody.isIsomorphicTo(dstMethodBody))
                return 1;
        }

        return 0;
    }

    private static ITree getMethodBody(ITree method) {
        for (ITree t: method.getChildren()) {
            if (typeChecker.isBlock(t))
                return t;
        }
        return null;
    }
}
