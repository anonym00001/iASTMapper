package at.aau.softwaredynamics.gen;

import com.github.gumtreediff.gen.jdt.AbstractJdtTreeGenerator;
import com.github.gumtreediff.gen.jdt.AbstractJdtVisitor;
import org.eclipse.jdt.core.compiler.IScanner;

/**
 * Created by thomas on 09.05.2017.
 */
public class DocIgnoringTreeGenerator extends AbstractJdtTreeGenerator {
//    @Override
//    protected AbstractJdtVisitor createVisitor() {
//        return new DocIgnoringTreeVisitor();
//    }

    @Override
    protected AbstractJdtVisitor createVisitor(IScanner scanner) {
        return new DocIgnoringTreeVisitor(scanner);
    }
}
