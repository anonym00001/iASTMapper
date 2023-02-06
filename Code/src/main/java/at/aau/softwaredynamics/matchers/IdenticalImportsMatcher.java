package at.aau.softwaredynamics.matchers;

import at.aau.softwaredynamics.util.CuttingCopyTreeHelper;
import at.aau.softwaredynamics.util.IJMTreeUtil;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
import cs.model.algorithm.languageutils.typechecker.JavaNodeTypeChecker;
import cs.model.algorithm.languageutils.typechecker.StaticNodeTypeChecker;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by thomas on 27.03.2017.
 */
public class IdenticalImportsMatcher implements Matcher {

    private ITree src;
    private ITree dst;
    private MappingStore mappings;

    private CuttingCopyTreeHelper treeCutter;

    private static JavaNodeTypeChecker typeChecker = StaticNodeTypeChecker.getConfigNodeTypeChecker();

//    public IdenticalImportsMatcher(ITree src, ITree dst, MappingStore store) {
//        super(src, dst, store);
//
//        srcIds = new TreeMap(src);
//        dstIds = new TreeMap(dst);
//    }

    private void match() {
        List<ITree> srcTreesInPostOrder = TreeUtils.postOrder(src);
        List<ITree> dstTreesInPostOrder = TreeUtils.postOrder(dst);

        Map<ITree, Integer> srcTreePosMap = new HashMap<>();
        Map<ITree, Integer> dstTreePosMap = new HashMap<>();
        List<ITree> srcImports = getImports(src, srcTreesInPostOrder, srcTreePosMap,true);
        List<ITree> dstImports = getImports(dst, dstTreesInPostOrder, dstTreePosMap,false);

        Iterator<ITree> srcIterator = srcImports.iterator();

        while (srcIterator.hasNext() && dstImports.size() > 0) {
            ITree currentSrc = srcIterator.next();

            Iterator<ITree> dstIterator = dstImports.iterator();

            while (dstIterator.hasNext()) {
                ITree currentDst = dstIterator.next();

                int compValue = currentSrc.getLabel().compareTo(currentDst.getLabel());

                if (compValue > 1)
                    break;

                if (compValue == 0) {
                    ITree originalSrc = srcTreesInPostOrder.get(srcTreePosMap.get(currentSrc));
                    ITree originalDst = dstTreesInPostOrder.get(dstTreePosMap.get(currentDst));

                    mappings.addMapping(originalSrc, originalDst);

                    srcIterator.remove();
                    dstIterator.remove();

                    break;
                }
            }
        }
    }

    private List<ITree> getImports(ITree root, List<ITree> treesInPostOrder,
                                   Map<ITree, Integer> treePosMap, boolean isSrc) {
        ITree cutTree = treeCutter.cutTree(root);
        IJMTreeUtil.removeMatched(cutTree, treesInPostOrder, treePosMap, mappings, isSrc);

        return cutTree.getDescendants()
                .stream()
                .filter(typeChecker::isImportDec)
                .sorted(Comparator.comparing(ITree::getLabel))
                .collect(Collectors.toList());
    }

    @Override
    public MappingStore match(ITree src, ITree dst, MappingStore mappings) {
        this.src = src;
        this.dst = dst;
        this.mappings = mappings;
        treeCutter = new CuttingCopyTreeHelper(
                t -> !t.isRoot()
                        && typeChecker.isTypeDec(t.getParent())
                        && typeChecker.isImportDec(t),
                false);
        match();
        return this.mappings;
    }
}
