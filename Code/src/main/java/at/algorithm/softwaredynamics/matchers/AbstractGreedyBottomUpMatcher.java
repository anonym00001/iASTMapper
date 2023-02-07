package at.algorithm.softwaredynamics.matchers;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.SimilarityMetrics;
import com.github.gumtreediff.tree.ITree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by thomas on 27.02.2017. based on GreedyBottomUpMatcher in GumTreeDiff
 */
public abstract class AbstractGreedyBottomUpMatcher implements Matcher {
    protected static final double SIM_THRESHOLD = Double.parseDouble(System.getProperty("gumtree.match.bu.sim", "0.3"));
    protected static final int SIZE_THRESHOLD = Integer.parseInt(System.getProperty("gumtree.match.bu.size", "1000"));

    protected ITree src;
    protected ITree dst;
    protected MappingStore mappings;

    public MappingStore match(ITree src, ITree dst, MappingStore store) {
        this.src = src;
        this.dst = dst;
        this.mappings = store;
        doMatch();
        return mappings;
    }

    private void doMatch(){
        for (ITree t: src.postOrder())  {
            if (t.isRoot()) {
                mappings.addMapping(t, this.dst);
                lastChanceMatch(t, this.dst);
                break;
            } else if (!(mappings.isSrcMapped(t) || t.isLeaf())) {
                List<ITree> candidates = getDstCandidates(t);
                ITree best = null;
                double max = -1D;

                for (ITree cand: candidates) {
                    double sim = SimilarityMetrics.jaccardSimilarity(t, cand, mappings);
                    if (sim > max && sim >= SIM_THRESHOLD) {
                        max = sim;
                        best = cand;
                    }
                }

                if (best != null) {
                    lastChanceMatch(t, best);
                    mappings.addMapping(t, best);
                }
            }
        }
    }

    private List<ITree> getDstCandidates(ITree src) {
        List<ITree> seeds = new ArrayList<>();
        for (ITree c: src.getDescendants()) {
            ITree m = mappings.getDstForSrc(c);
            if (m != null) seeds.add(m);
        }
        List<ITree> candidates = new ArrayList<>();
        Set<ITree> visited = new HashSet<>();
        for (ITree seed: seeds) {
            while (seed.getParent() != null) {
                ITree parent = seed.getParent();
                if (visited.contains(parent))
                    break;
                visited.add(parent);
                if (parent.getType() == src.getType() && !mappings.isDstMapped(parent) && !parent.isRoot())
                    candidates.add(parent);
                seed = parent;
            }
        }

        return candidates;
    }

    protected abstract void lastChanceMatch(ITree src, ITree dst);
}
