/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2015 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2015 Floréal Morandat <florealm@gmail.com>
 */

package com.github.gumtreediff.matchers.heuristic.gt;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.SimilarityMetrics;
import com.github.gumtreediff.tree.ITree;

import java.util.List;

/**
 * Match the nodes using a bottom-up approach. It browse the nodes of the source and destination trees
 * using a post-order traversal, testing if the two selected trees might be mapped. The two trees are mapped 
 * if they are mappable and have a dice coefficient greater than SIM_THRESHOLD. Whenever two trees are mapped
 * a exact ZS algorithm is applied to look to possibly forgotten nodes.
 */
public class GreedyBottomUpMatcher extends AbstractBottomUpMatcher implements Matcher {
    @Override
    public MappingStore match(ITree src, ITree dst, MappingStore mappings) {
        Implementation impl =  new Implementation(src, dst, mappings);
        impl.match();
        return impl.mappings;
    }

    protected static class Implementation extends AbstractBottomUpMatcher.Implementation {
        public Implementation(ITree src, ITree dst, MappingStore mappings) {
            super(src, dst, mappings);
        }

        public void match() {
            for (ITree t : src.postOrder()) {
                if (t.isRoot()) {
                    mappings.addMapping(t, this.dst);
                    lastChanceMatch(t, this.dst);
                    break;
                } else if (!(mappings.isSrcMapped(t) || t.isLeaf())) {
                    List<ITree> candidates = getDstCandidates(t);
                    ITree best = null;
                    double max = -1D;
                    for (ITree cand : candidates) {
                        double sim = SimilarityMetrics.diceSimilarity(t, cand, mappings);
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
    }
}
