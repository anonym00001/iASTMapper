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

package com.github.gumtreediff.matchers.heuristic.cd;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.SimilarityMetrics;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;

import java.util.List;

public class ChangeDistillerBottomUpMatcher implements Matcher {

    public static final double STRUCT_SIM_THRESHOLD_1 =  Double.parseDouble(System.getProperty("gt.cd.ssim1", "0.6"));

    public static final double STRUCT_SIM_THRESHOLD_2 = Double.parseDouble(System.getProperty("gt.cd.ssim2", "0.4"));

    public static final int MAX_NUMBER_OF_LEAVES = Integer.parseInt(System.getProperty("gt.cd.ml", "4"));

    @Override
    public MappingStore match(ITree src, ITree dst, MappingStore mappings) {
        Implementation impl = new Implementation(src, dst, mappings);
        impl.match();
        return impl.mappings;
    }

    private static class Implementation {
        private final ITree src;
        private final ITree dst;
        private final MappingStore mappings;

        public Implementation(ITree src, ITree dst, MappingStore mappings) {
            this.src = src;
            this.dst = dst;
            this.mappings = mappings;
        }

        public void match() {
            List<ITree> dstTrees = TreeUtils.postOrder(this.dst);
            for (ITree currentSrcTree : this.src.postOrder()) {
                int numberOfLeaves = numberOfLeaves(currentSrcTree);
                for (ITree currentDstTree : dstTrees) {
                    if (mappings.isMappingAllowed(currentSrcTree, currentDstTree)
                            && !(currentSrcTree.isLeaf() || currentDstTree.isLeaf())) {
                        double similarity =
                                SimilarityMetrics.chawatheSimilarity(currentSrcTree, currentDstTree, mappings);
                        if ((numberOfLeaves > MAX_NUMBER_OF_LEAVES && similarity >= STRUCT_SIM_THRESHOLD_1)
                                || (numberOfLeaves <= MAX_NUMBER_OF_LEAVES && similarity >= STRUCT_SIM_THRESHOLD_2)) {
                            mappings.addMapping(currentSrcTree, currentDstTree);
                            break;
                        }
                    }
                }
            }
        }

        private int numberOfLeaves(ITree root) {
            int numberOfLeaves = 0;
            for (ITree tree : root.getDescendants())
                if (tree.isLeaf())
                    numberOfLeaves++;
            return numberOfLeaves;
        }
    }
}
