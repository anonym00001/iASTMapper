package at.algorithm.softwaredynamics.gen;

import com.github.gumtreediff.gen.jdt.AbstractJdtTreeGenerator;
import com.github.gumtreediff.gen.jdt.AbstractJdtVisitor;
import org.eclipse.jdt.core.compiler.IScanner;

/**
 * Created by thomas on 14.03.2017.
 */
public class OptimizedJdtTreeGenerator extends AbstractJdtTreeGenerator {

//    @Override
//    protected AbstractJdtVisitor createVisitor() {
//        return new OptimizedJdtVisitor();
//    }

    @Override
    protected AbstractJdtVisitor createVisitor(IScanner scanner) {
        return new OptimizedJdtVisitor();
    }
}
