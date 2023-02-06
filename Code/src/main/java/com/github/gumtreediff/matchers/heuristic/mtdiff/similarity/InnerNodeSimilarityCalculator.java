/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with GumTree. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2015-2016 Georg Dotzler <georg.dotzler@fau.de>
 * Copyright 2015-2016 Marius Kamp <marius.kamp@fau.de>
*/

package com.github.gumtreediff.matchers.heuristic.mtdiff.similarity;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.heuristic.mtdiff.intern.LabelConfiguration;
import com.github.gumtreediff.tree.ITree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InnerNodeSimilarityCalculator {

    private Map<ITree, ArrayList<ITree>> directChildrenMap1 = null;
    private Map<ITree, ArrayList<ITree>> directChildrenMap2 = null;
    private LabelConfiguration labelConfiguration;
    private Map<ITree, ArrayList<ITree>> leavesMap1 = null;
    private Map<ITree, ArrayList<ITree>> leavesMap2 = null;

    private Set<Mapping> matchedNodes;

    private NGramCalculator stringSim = new NGramCalculator(2, 10, 10);

    private final int subtreeSizeThreshold = 4;
    private final float subtreeThresholdLarge = 0.6f;
    private final float subtreeThresholdSmall = 0.4f;
    private final float subtreeThresholdValueMismatch = 0.7f;
    private final float valueThreshold = 0.6f;

    /**
     * Instantiates a new inner node calculator.
     *
     * @param labelConfiguration the label configuration
     * @param leavesMap1         the leaves map1
     * @param leavesMap2         the leaves map2
     * @param directChildrenMap1 the direct children map1
     * @param directChildrenMap2 the direct children map2
     * @param matchedNodes       the matched nodes
     */
    public InnerNodeSimilarityCalculator(LabelConfiguration labelConfiguration,
                                         Map<ITree, ArrayList<ITree>> leavesMap1, Map<ITree, ArrayList<ITree>> leavesMap2,
                                         Map<ITree, ArrayList<ITree>> directChildrenMap1,
                                         Map<ITree, ArrayList<ITree>> directChildrenMap2, Set<Mapping> matchedNodes) {
        this.labelConfiguration = labelConfiguration;
        this.leavesMap1 = leavesMap1;
        this.leavesMap2 = leavesMap2;
        this.directChildrenMap1 = directChildrenMap1;
        assert (directChildrenMap1 != null);
        this.directChildrenMap2 = directChildrenMap2;
        this.matchedNodes = matchedNodes;

    }

    private float childrenSimilarity(final List<ITree> firstChildren,
                                     final List<ITree> secondChildren, final List<ITree> firstDirectChildren,
                                     final List<ITree> secondDirectChildren) {
        int common = 0;
        assert (firstChildren != null);
        assert (secondChildren != null);
        final int max = Math.max(firstChildren.size(), secondChildren.size());
        int[] firstDcCount = new int[firstDirectChildren.size()];
        int[] secondDcCount = new int[secondDirectChildren.size()];
        @SuppressWarnings("unchecked")
        ArrayList<ITree>[] firstDclists = new ArrayList[firstDirectChildren.size()];
        @SuppressWarnings("unchecked")
        ArrayList<ITree>[] secondDclists = new ArrayList[secondDirectChildren.size()];
        for (int i = 0; i < firstDirectChildren.size(); i++) {
            firstDclists[i] = leavesMap1.get(firstDirectChildren.get(i));
            assert (firstDclists[i] != null);
        }
        for (int i = 0; i < secondDirectChildren.size(); i++) {
            secondDclists[i] = leavesMap2.get(secondDirectChildren.get(i));
            assert (secondDclists[i] != null);
        }
        if (max <= 0) {
            return 1.0f;
        }

        int posFirst = -1;
        outer:
        for (final ITree firstNode : firstChildren) {
            posFirst++;
            int posSecond = -1;
            if (firstNode == null) {
                continue;
            }

            for (final ITree secondNode : secondChildren) {
                posSecond++;
                if (secondNode == null) {
                    continue;
                }

                final Mapping pair = new Mapping(firstNode, secondNode);

                if (matchedNodes.contains(pair)) {
                    common++;
                    int counter = 0;
                    for (int i = 0; i < firstDclists.length; i++) {
                        if (firstDclists[i].contains(firstNode)) {
                            firstDcCount[i]++;
                            break;
                        } else if (counter == posFirst && firstDclists[i].isEmpty()) {
                            firstDcCount[i]++;
                            break;
                        }
                        counter += (firstDcCount[i] == 0 ? 1 : firstDcCount[i]);
                    }
                    counter = 0;
                    for (int i = 0; i < secondDclists.length; i++) {
                        if (secondDclists[i].contains(secondNode)) {
                            secondDcCount[i]++;
                            break;
                        } else if (counter == posSecond && secondDclists[i].isEmpty()) {
                            secondDcCount[i]++;
                            break;
                        }
                        counter += (secondDcCount[i] == 0 ? 1 : secondDcCount[i]);
                    }
                    continue outer;
                }
            }
        }
        assert common <= max;
        float tmp = 0.0f;
        for (int i = 0; i < firstDclists.length; i++) {
            tmp += firstDcCount[i] / (float) (firstDclists[i].size() == 0 ? 1 : firstDclists[i].size());
        }
        for (int i = 0; i < secondDclists.length; i++) {
            tmp +=
                    secondDcCount[i] / (float) (secondDclists[i].size() == 0 ? 1 : secondDclists[i].size());
        }
        tmp = tmp / (firstDclists.length + secondDclists.length);
        return tmp;
    }

    private boolean isMatch(final float childrenSimilarity, final float valueSimilarity,
                            final int childrenCount) {
        if (valueSimilarity < valueThreshold) {
            return childrenSimilarity >= subtreeThresholdValueMismatch;
        }
        if (childrenCount <= subtreeSizeThreshold) {
            return childrenSimilarity >= subtreeThresholdSmall;
        }
        return childrenSimilarity >= subtreeThresholdLarge;
    }

    /**
     * Determine whether two inner nodes are a suitable match.
     *
     * @param labelSimilarity    the label similarity
     * @param combinedSimilarity the combined similarity
     * @param first              the first node
     * @param second             the second node
     * @return true, if the nodes are a suitable match
     */
    public boolean match(float labelSimilarity, float combinedSimilarity, ITree first, ITree second) {
        if (labelSimilarity < valueThreshold) {
            if (labelSimilarity != 0 && combinedSimilarity != 0 && first.getMetrics().position == second.getMetrics().position) {
                return true;
            } else {
                return combinedSimilarity >= subtreeThresholdValueMismatch;
            }
        }
        return true;
    }

    /**
     * Determine whether two inner nodes are a suitable match.
     *
     * @param first      the first
     * @param second     the second
     * @param similarity the similarity
     * @return true, if successful
     */
    public boolean match(ITree first, ITree second, float similarity) {
        if (first.getChildren() == null || first.getChildren().size() == 0) {
            return false;
        }
        if (first.getType() != second.getType()) {
            return false;
        }
        final List<ITree> firstChldrn = leavesMap1.get(first);
        final List<ITree> secondChldrn = leavesMap2.get(second);
        final List<ITree> firstDirectCh = directChildrenMap1.get(first);
        final List<ITree> secondDirectCh = directChildrenMap2.get(second);
        if (firstChldrn == null) {
            assert (false);
        }
        if (secondChldrn == null) {
            assert (false);
        }
        final float childrenSim =
                childrenSimilarity(firstChldrn, secondChldrn, firstDirectCh, secondDirectCh);
        final int childrenCount = Math.max(firstChldrn.size(), secondChldrn.size());

        return isMatch(childrenSim, similarity, childrenCount);

    }

    /**
     * Computes the similarity for the subtrees of inner nodes.
     *
     * @param first    the first
     * @param second   the second
     * @param labelSim the label sim
     * @return the float
     */
    public float mtDiffSimilarity(ITree first, ITree second, float labelSim) {
        float childSim = 0.0f;
        if (first.getChildren() == null || first.getChildren().size() == 0) {
            childSim = 0.0f;
        } else if (first.getType() != second.getType()) {
            childSim = 0.0f;
        } else {
            final List<ITree> firstChldrn = leavesMap1.get(first);
            final List<ITree> secondChldrn = leavesMap2.get(second);
            final List<ITree> firstDirectCh = directChildrenMap1.get(first);
            final List<ITree> secondDirectCh = directChildrenMap2.get(second);
            if (firstChldrn == null) {
                assert (false);
            }
            if (secondChldrn == null) {
                assert (false);
            }
            childSim = childrenSimilarity(firstChldrn, secondChldrn, firstDirectCh, secondDirectCh);
        }
        return childSim;

    }

    /**
     * Computes the similarity of the values of inner nodes.
     *
     * @param first  the first
     * @param second the second
     * @return the float
     */
    public float similarity(ITree first, ITree second) {
        if (first.getType() != second.getType()) {
            return 0.0f;
        }
        if (first.getChildren() == null || second.getChildren() == null) {
            return 0.0f;
        }
        if (first.getChildren().size() == 0 || second.getChildren().size() == 0) {
            return 0.0f;
        }
        if (labelConfiguration.labelsForValueCompare.contains(first.getType())) {
            if (first.getLabel().equals(second.getLabel())) {
                return 1.0f;
            }
            return 0.2f;
        } else if (labelConfiguration.labelsForStringCompare.contains(first.getType())) {
            return stringSim.similarity(first.getLabel(), second.getLabel());

        } else {
            return 1.0f;
        }
    }

}
