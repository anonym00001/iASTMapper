package at.aau.softwaredynamics.matchers;

import at.aau.softwaredynamics.util.IJMTreeUtil;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.optimal.zs.ZsMatcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
import com.github.gumtreediff.tree.Type;
import com.github.gumtreediff.tree.TypeSet;

import java.util.*;

/**
 * Created by thomas on 27.02.2017. based on GreedyBottomUpMatcher in GumTreeDiff
 */
public class LabelAwareBottomUpMatcher extends AbstractGreedyBottomUpMatcher {
    private static Set<Type> labelMatchingNodes = initLabelMatchingNodes();

    private static double similarityThreshold = Double.parseDouble(
            System.getProperty("ijm.label.sim.threshold", "0.3"));

    private LabelSimilarityHelper labelSimHelper;
    private boolean removeMatched = true;

    private static Set<Type> initLabelMatchingNodes(){
        Set<Type> ret = new HashSet<>();
        Type SimpleNameType = TypeSet.type("SimpleName");
        Type MethodDecType = TypeSet.type("MethodDeclaration");
        Type EnumDecType = TypeSet.type("EnumDeclaration");
        Type MethodInvType = TypeSet.type("MethodInvocation");
        Type ImportDecType = TypeSet.type("ImportDeclaration");
        Type EnumConstantDecType = TypeSet.type("EnumConstantDeclaration");
        ret.add(SimpleNameType);
        ret.add(MethodDecType);
        ret.add(EnumDecType);
        ret.add(MethodInvType);
        ret.add(ImportDecType);
        ret.add(EnumConstantDecType);
        return ret;
    }

    public void setMinLabelSimilarity(double simThreshold) {
        this.labelSimHelper = new LabelSimilarityHelper(simThreshold);
    }
    
    public void setRemoveMatched(boolean removeMatched) {
        this.removeMatched = removeMatched;
    }

    protected void lastChanceMatch(ITree src, ITree dst) {
        this.labelSimHelper = new LabelSimilarityHelper(similarityThreshold);
        List<ITree> srcTreesInPostOrder = TreeUtils.postOrder(src);
        List<ITree> dstTreesInPostOrder = TreeUtils.postOrder(dst);
        ITree cSrc = src.deepCopy();
        ITree cDst = dst.deepCopy();
        Map<ITree, Integer> srcTreePosMap = new HashMap<>();
        Map<ITree, Integer> dstTreePosMap = new HashMap<>();
        IJMTreeUtil.removeMatched(cSrc, srcTreesInPostOrder, srcTreePosMap, mappings, true);
        IJMTreeUtil.removeMatched(cDst, dstTreesInPostOrder, dstTreePosMap, mappings,false);

        if (cSrc.getMetrics().size < SIZE_THRESHOLD || cDst.getMetrics().size < SIZE_THRESHOLD) {
            Matcher m = new ZsMatcher();
            MappingStore zsMappings = m.match(cSrc, cDst);
            for (Mapping candidate: zsMappings) {
                ITree left = srcTreesInPostOrder.get(srcTreePosMap.get(candidate.first));
                ITree right = dstTreesInPostOrder.get(dstTreePosMap.get(candidate.second));

                if (left == src || right == dst) {
                    continue;
                } else if (left.getType() != right.getType()) {
                    continue;
                } else if (left.getParent().getType() != right.getParent().getType()) {
                    continue;
                } else if (labelMatchingNodes.contains(left.getType())) {
                    if (this.labelSimHelper.getSimilarity(left, right) > 0D) {
                        mappings.addMapping(left, right);
                    }
                } else {
                    mappings.addMapping(left, right);
                }
            }
        }
    }
}
