package cs.model.algorithm.matcher.measures.stmt;

import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.StmtElement;
import cs.model.algorithm.matcher.measures.AbstractSimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasure;

/**
 * Mechanism: if two methods have the same Javadoc, they are likely to be the same method.
 */
public class StmtJavadocMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        ITree srcJavadoc = ((StmtElement) srcEle).getJavadoc();
        ITree dstJavadoc = ((StmtElement) dstEle).getJavadoc();
        if (srcJavadoc != null && dstJavadoc != null && srcJavadoc.isIsomorphicTo(dstJavadoc))
            return 1;
        return 0;
    }
}
