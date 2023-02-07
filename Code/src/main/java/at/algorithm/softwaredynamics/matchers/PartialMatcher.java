package at.algorithm.softwaredynamics.matchers;

import at.algorithm.softwaredynamics.util.CuttingCopyTreeHelper;
import at.algorithm.softwaredynamics.util.IJMTreeUtil;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by thomas on 31.01.2017.
 */
public class PartialMatcher implements Matcher {
    private PartialMatcherConfiguration config;
    private CuttingCopyTreeHelper treeCutter;
    private ITree src;
    private ITree dst;
    private MappingStore mappings;

    public PartialMatcher(PartialMatcherConfiguration config) {
        this.config = config;
        this.treeCutter = new CuttingCopyTreeHelper(config.getCutOffCondition(), false);
    }

    private void match() {
        List<ITree> srcTreesInPostOrder = TreeUtils.postOrder(src);
        List<ITree> dstTreesInPostOrder = TreeUtils.postOrder(dst);
        ITree cutSrc = treeCutter.cutTree(this.src);
        ITree cutDst = treeCutter.cutTree(this.dst);

        Map<ITree, Integer> srcTreePosMap = new HashMap<>();
        Map<ITree, Integer> dstTreePosMap = new HashMap<>();
        IJMTreeUtil.removeMatched(cutSrc, srcTreesInPostOrder, srcTreePosMap, mappings, true);
        IJMTreeUtil.removeMatched(cutDst, dstTreesInPostOrder, dstTreePosMap, mappings, false);

        // init inner matcher with cut trees
        Matcher innerMatcher = new MatcherFactory(config.getInnerMatcherType()).createMatcher();
        MappingStore innerMappings = innerMatcher.match(cutSrc, cutDst);

        // add mappings
        for(Mapping m : innerMappings) {
            if (!this.config.includeInResult(m.first))
                continue;

            // map tmp mapping back to original mappings
            ITree originalSrc = srcTreesInPostOrder.get(srcTreePosMap.get(m.first));
            ITree originalDst = dstTreesInPostOrder.get(dstTreePosMap.get(m.second));

            // add sub mappings if configured
            matchSubTrees(originalSrc, originalDst);

            // add mapping
            if (mappings.isMappingAllowed(originalSrc, originalDst))
                mappings.addMapping(originalSrc, originalDst);
        }
    }

    private void matchSubTrees(ITree src, ITree dst) {
        CuttingCopyTreeHelper copyTreeHelper = new CuttingCopyTreeHelper(t -> false, true);

        if (this.config.getMatchSubtrees()) {
            List<ITree> srcTreesInPostOrder = TreeUtils.postOrder(src);
            List<ITree> dstTreesInPostOrder = TreeUtils.postOrder(dst);
            ITree srcCopy = copyTreeHelper.cutTree(src);
            ITree dstCopy = copyTreeHelper.cutTree(dst);
            Map<ITree, Integer> srcTreePosMap = new HashMap<>();
            Map<ITree, Integer> dstTreePosMap = new HashMap<>();

            IJMTreeUtil.calTreePosMap(srcCopy, srcTreePosMap);
            IJMTreeUtil.calTreePosMap(dstCopy, dstTreePosMap);

            Matcher subMatcher =
                    new MatcherFactory(config.getSubNodeMatcherType())
                            .createMatcher();
            MappingStore subMappings = subMatcher.match(srcCopy, dstCopy);

            for(Mapping m : subMappings) {
                ITree left = srcTreesInPostOrder.get(srcTreePosMap.get(m.first));
                ITree right = dstTreesInPostOrder.get(dstTreePosMap.get(m.second));
                if (mappings.isMappingAllowed(left, right))
                    mappings.addMapping(left, right);
            }
        }
    }


//    protected void addMapping(ITree src, ITree dst) {
//        if (src.isMatched() || dst.isMatched())
//            return;
//
//        super.addMapping(src,dst);
//    }

    @Override
    public MappingStore match(ITree src, ITree dst, MappingStore mappings) {
        this.src = src;
        this.dst = dst;
        this.mappings = mappings;
        match();
        return this.mappings;
    }
}
