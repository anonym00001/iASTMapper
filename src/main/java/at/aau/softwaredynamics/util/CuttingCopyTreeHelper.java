package at.aau.softwaredynamics.util;

import com.github.gumtreediff.tree.ITree;

import java.util.function.Function;

/**
 * Created by thomas on 31.01.2017.
 */
public class CuttingCopyTreeHelper {
    private final Function<ITree, Boolean> cutOffCondition;
    private final boolean resetIsMatched;

    public CuttingCopyTreeHelper(Function<ITree, Boolean> cutOffCondition, boolean resetIsMatched) {
        if (cutOffCondition == null)
            throw new IllegalArgumentException("cutOffCondition must not be null");

        this.cutOffCondition = cutOffCondition;
        this.resetIsMatched = resetIsMatched;
    }


    public ITree cutTree(ITree root) {
        ITree rootCopy = root.deepCopy();
        // reset matching info
//        rootCopy.getMetrics();

//        cutTree(root, rootCopy);
        return rootCopy;
    }

    private void cutTree(ITree currentOriginal, ITree currentCopy) {
        for (ITree child : currentOriginal.getChildren()) {
            if (cutOffCondition.apply(child))
                continue;

            ITree childCopy = child.deepCopy();

            childCopy.setParentAndUpdateChildren(currentCopy);
            cutTree(child, childCopy);
        }
    }
}
