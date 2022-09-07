package at.aau.softwaredynamics.gen;

import com.github.gumtreediff.gen.jdt.JdtVisitor;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Javadoc;

/**
 * Created by thomas on 09.05.2017.
 */
public class DocIgnoringTreeVisitor extends JdtVisitor {

    public DocIgnoringTreeVisitor(IScanner scanner) {
        super(scanner);
    }

    @Override
    public boolean visit(Javadoc node) {
        return false;
    }

    @Override
    public boolean visit(BlockComment node) {
        return false;
    }
}
