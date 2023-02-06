package at.aau.softwaredynamics.gen;

import com.github.gumtreediff.gen.jdt.AbstractJdtTreeGenerator;
import com.github.gumtreediff.gen.jdt.AbstractJdtVisitor;
import org.eclipse.jdt.core.compiler.IScanner;

/**
 * Created by thomas on 01.12.2016.
 */
public class AstNodePopulatingJdtTreeGenerator extends AbstractJdtTreeGenerator {

//    @Override
//    protected AbstractJdtVisitor createVisitor() {
//        return new AstNodePopulatingJdtVisitor();
//    }

    @Override
    protected AbstractJdtVisitor createVisitor(IScanner scanner) {
        return new AstNodePopulatingJdtVisitor();
    }
}
