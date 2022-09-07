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
 * Copyright 2015-2016 Georg Dotzler <georg.dotzler@fau.de>
 * Copyright 2015-2016 Marius Kamp <marius.kamp@fau.de>
 */

package com.github.gumtreediff.matchers.heuristic.mtdiff.intern;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.heuristic.mtdiff.MtDiffOptimizedMatcher;
import com.github.gumtreediff.matchers.heuristic.mtdiff.hungarian.DoubleMatrix;
import com.github.gumtreediff.matchers.heuristic.mtdiff.hungarian.Hungarian;
import com.github.gumtreediff.matchers.heuristic.mtdiff.similarity.InnerNodeSimilarityCalculator;
import com.github.gumtreediff.matchers.heuristic.mtdiff.similarity.NGramCalculator;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Type;
import cs.sysu.evaluation.config.MyTimeUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

class SimilarityMatrixHelper {

    private static final Type NODE_AGGREGATION_LABEL = Type.NODE_AGGREGATION_TYPE;

    private static double[][] performTransformation(double[][] original) {
        double[][] similarityValues = new double[original.length][];
        for (int i = 0; i < original.length; i++) {
            similarityValues[i] = Arrays.copyOf(original[i], original[i].length);
        }

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        final int size = similarityValues.length;
        for (int row = 0; row < size; ++row) {
            for (int column = 0; column < similarityValues[row].length; ++column) {
                final double value = similarityValues[row][column];

                if (value < min) {
                    min = value;
                }

                if (value > max) {
                    max = value;
                }
            }
        }

        for (int row = 0; row < size; ++row) {
            for (int column = 0; column < similarityValues[row].length; ++column) {
                final double value = similarityValues[row][column];

                if (max != min) {
                    final double newSimilarityValue = (value - min) / (max - min) * 1000000;
                    similarityValues[row][column] = newSimilarityValue;
                } else {
                    similarityValues[row][column] = 1000000;
                }

            }
        }

        return similarityValues;
    }

    private boolean[][] aggregationFinished;
    private ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, MatchingCandidate>> candidateMap;
    private AtomicBoolean changed;
    private ConcurrentHashMap<ITree, MatchingCandidate> currentResultMap;
    private Map<ITree, ArrayList<ITree>> directChildrenMap1 = null;
    private Map<ITree, ArrayList<ITree>> directChildrenMap2 = null;
    private ITree[] firstAggregations;

    private ConcurrentSkipListSet<MatchingCandidate> initialList;
    private LabelConfiguration labelConfiguration;
    private Map<ITree, ArrayList<ITree>> leavesMap1 = null;

    private Map<ITree, ArrayList<ITree>> leavesMap2 = null;
    private LMatcher leafMatcher;
    private ArrayList<ITree> newNodes;
    private ArrayList<ITree> oldNodes;
    private boolean onlyOneClassPair;

    private Map<ITree, ITree> parents1;
    private Map<ITree, ITree> parents2;
    private IdentityHashMap<ITree, Mapping> resultMap;
    private ITree root1;
    private ITree root2;
    private ITree[] secondAggregations;
    private ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, Float>> similarityCache;
    private AtomicLong similarityEntries;
    private double[][] similarityScores;
    private NGramCalculator stringSim;
    private ConcurrentHashMap<String, Float> stringSimCache;
    private double weightPosition;
    private double weightSimilarity;

    SimilarityMatrixHelper(boolean[][] aggregationFinished, ITree[] firstAggregations,
                           ITree[] secondAggregations,
                           ConcurrentHashMap<ITree, MatchingCandidate> currentResultMap, AtomicBoolean changed,
                           ArrayList<ITree> oldNodes, ArrayList<ITree> newNodes,
                           IdentityHashMap<ITree, Mapping> resultMap, NGramCalculator stringSim,
                           ConcurrentHashMap<String, Float> stringSimCache, boolean onlyOneClassPair,
                           double[][] similarityScores, ConcurrentSkipListSet<MatchingCandidate> initialList,
                           ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, MatchingCandidate>> candidateMap,
                           AtomicIntegerArray foundMaxArray,
                           ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, Float>> similarityCache,
                           AtomicLong similarityEntries, Map<ITree, ITree> parents1, Map<ITree, ITree> parents2,
                           Map<ITree, ArrayList<ITree>> leavesMap1, Map<ITree, ArrayList<ITree>> leavesMap2,
                           LabelConfiguration labelConfiguration, LMatcher leafMatcher,
                           Map<ITree, ArrayList<ITree>> directChildrenMap1,
                           Map<ITree, ArrayList<ITree>> directChildrenMap2, ITree root1, ITree root2,
                           double weightSimilarity, double weightPosition) {

        this.aggregationFinished = aggregationFinished;
        this.firstAggregations = firstAggregations;
        this.secondAggregations = secondAggregations;
        this.currentResultMap = currentResultMap;
        this.changed = changed;
        this.newNodes = newNodes;
        this.oldNodes = oldNodes;
        this.similarityEntries = similarityEntries;
        this.resultMap = resultMap;
        this.stringSim = stringSim;
        this.stringSimCache = stringSimCache;
        this.onlyOneClassPair = onlyOneClassPair;
        this.similarityScores = similarityScores;
        this.initialList = initialList;
        this.similarityCache = similarityCache;
        this.parents1 = parents1;
        this.parents2 = parents2;
        this.leavesMap1 = leavesMap1;
        this.leavesMap2 = leavesMap2;
        this.labelConfiguration = labelConfiguration;
        this.leafMatcher = leafMatcher;
        this.directChildrenMap1 = directChildrenMap1;
        this.directChildrenMap2 = directChildrenMap2;
        this.candidateMap = candidateMap;
        this.root1 = root1;
        this.root2 = root2;
        this.weightSimilarity = weightSimilarity;
        this.weightPosition = weightPosition;
    }


    /**
     * Computes the similarity matrix helper.
     */
    public void call() {
        try {
            while (changed.get()) {
                changed.set(false);
                for (int i = 0; i < oldNodes.size(); i++) {
                    double maxValue = Float.MIN_VALUE;
                    int maxCount = 0;
                    int indexNew = -1;
                    for (int j = 0; j < newNodes.size(); j++) {
                        if (!aggregationFinished[i][j]) {
                            if (similarityScores[i][j] > maxValue) {
                                maxValue = similarityScores[i][j];
                                indexNew = j;
                                maxCount = 1;
                            } else if (Math.abs(similarityScores[i][j] - maxValue) < 0.00001) {
                                maxCount++;
                                indexNew = j;
                            }
                        }
                    }
                    if (maxCount == 1 && newNodes.size() > 1) {
                        for (int j = 0; j < newNodes.size(); j++) {
                            aggregationFinished[i][j] = true;
                        }
                        boolean found = true;
                        for (int k = 0; k < oldNodes.size(); k++) {
                            if (!aggregationFinished[k][indexNew]) {
                                if (i != k) {
                                    if (similarityScores[k][indexNew] >= maxValue) {
                                        found = false;
                                        break;
                                    }
                                }
                            }
                        }
                        if (found) {
                            for (int k = 0; k < oldNodes.size(); k++) {
                                aggregationFinished[k][indexNew] = true;
                            }
                        }
                    } else if (maxCount > 1) {
                        for (int j = 0; j < newNodes.size(); j++) {
                            if (similarityScores[i][j] < maxValue) {
                                aggregationFinished[i][j] = true;
                            }
                        }
                    }
                }
                if (newNodes.size() == 1) {
                    double maxValue = Float.MIN_VALUE;
                    int maxCount = 0;
                    for (int i = 0; i < oldNodes.size(); i++) {

                        if (!aggregationFinished[i][0]) {
                            if (similarityScores[i][0] > maxValue) {
                                maxValue = similarityScores[i][0];
                                maxCount = 1;
                            } else if (Math.abs(similarityScores[i][0] - maxValue) < 0.00001) {
                                maxCount++;
                            }
                        }
                    }
                    if (maxCount == 1) {
                        for (int i = 0; i < oldNodes.size(); i++) {
                            aggregationFinished[i][0] = true;
                        }
                    }

                }
                for (int i = 0; i < oldNodes.size(); i++) {
                    if (firstAggregations[i] != null) {
                        NodeAggregation tmp =
                                createAggregation((ITree) firstAggregations[i], parents1);
                        if (tmp == null) {
                            for (int j = 0; j < newNodes.size(); j++) {
                                if (!aggregationFinished[i][j]) {
                                    aggregationFinished[i][j] = true;
                                }
                            }
                        }
                        firstAggregations[i] = tmp;
                    }
                }

                for (int j = 0; j < newNodes.size(); j++) {
                    if (secondAggregations[j] != null) {
                        NodeAggregation tmp =
                                createAggregation((ITree) secondAggregations[j], parents2);
                        if (tmp == null) {
                            for (int i = 0; i < oldNodes.size(); i++) {
                                if (!aggregationFinished[i][j]) {
                                    aggregationFinished[i][j] = true;
                                }
                            }
                        }
                        secondAggregations[j] = tmp;
                    }
                }
                for (int i = 0; i < oldNodes.size(); i++) {
                    MyTimeUtil.checkCurrentTime();
                    updateSimilarityRow(aggregationFinished, similarityScores, firstAggregations,
                            secondAggregations, currentResultMap, changed, i, newNodes,
                            onlyOneClassPair, resultMap, stringSim, stringSimCache, similarityCache,
                            similarityEntries);
                }
            }
            DoubleMatrix matrix = DoubleMatrix.newMatrix(Math.max(oldNodes.size(), newNodes.size()),
                    Math.max(oldNodes.size(), newNodes.size()));
            int maxDiff = Integer.MIN_VALUE;
            double[][] posDiff = new double[oldNodes.size()][newNodes.size()];
            ArrayList<ITree> oldNodesSorted = new ArrayList<>(oldNodes);
            Collections.sort(oldNodesSorted, new IdComparator());
            HashMap<ITree, Integer> oldRankMap = new HashMap<>();
            int rank = 0;
            for (ITree node : oldNodesSorted) {
                oldRankMap.put(node, rank);
                rank++;
            }
            ArrayList<ITree> newNodesSorted = new ArrayList<>(newNodes);
            Collections.sort(newNodesSorted, new IdComparator());
            HashMap<ITree, Integer> newRankMap = new HashMap<>();
            rank = 0;
            for (ITree node : newNodesSorted) {
                newRankMap.put(node, rank);
                rank++;
            }

            for (int i = 0; i < oldNodes.size(); i++) {
                for (int j = 0; j < newNodes.size(); j++) {
                    // 修改代码
                    if (root1.getMetrics().position > oldNodes.get(i).getMetrics().position) {
                        int val = Math.abs(
                                oldRankMap.get(oldNodes.get(i)) - newRankMap.get(newNodes.get(j)));
                        posDiff[i][j] = val;
                        if (val > maxDiff) {
                            maxDiff = val;
                        }
                    } else {
                        int val = Math.abs((oldNodes.get(i).getMetrics().position - root1.getMetrics().position)
                                - (newNodes.get(j).getMetrics().position - root2.getMetrics().position));
                        posDiff[i][j] = val;
                        if (val > maxDiff) {
                            maxDiff = val;
                        }
                    }
                }
            }
            double defaultValue = 0;
            if (newNodes.size() > oldNodes.size()) {
                defaultValue = matrix.numRows() * matrix.numCols() * weightSimilarity * 1000000
                        + matrix.numRows() * matrix.numCols() * weightPosition * 1000000;
            }
            for (int i = 0; i < matrix.numRows(); i++) {
                for (int j = 0; j < matrix.numCols(); j++) {

                    matrix.set(i, j, defaultValue);
                }
            }
            List<ITree> skipList = new LinkedList<>();

            for (int i = 0; i < oldNodes.size(); i++) {
                double maxsim = 0;
                double minDiff = Integer.MAX_VALUE;
                for (int j = 0; j < newNodes.size(); j++) {

                    if (similarityScores[i][j] - maxsim > 0.001) {
                        maxsim = similarityScores[i][j];
                        minDiff = posDiff[i][j];
                    } else if (similarityScores[i][j] - maxsim < 0.001) {
                        if (minDiff > posDiff[i][j]) {
                            minDiff = posDiff[i][j];
                        }
                    }
                }
            }

            double[][] similarityScoresTransformed = performTransformation(similarityScores);

            double[][] posDiffTransformed = performTransformation(posDiff);
            for (int i = 0; i < oldNodes.size(); i++) {
                for (int j = 0; j < newNodes.size(); j++) {
                    double val = ((1000000 - similarityScoresTransformed[i][j]) * weightSimilarity)
                            + posDiffTransformed[i][j] * weightPosition;
                    matrix.set(i, j, val);
                }
            }


            int[] res = null;
            if (Math.abs(oldNodes.size() - newNodes.size()) > 2500 || oldNodes.size() > 1000
                    || newNodes.size() > 1000) {
                res = new int[oldNodes.size()];
                HashSet<Integer> alreadyDone = new HashSet<>();
                for (int i = 0; i < oldNodes.size(); i++) {
                    double min = Double.MAX_VALUE;
                    int index = -1;
                    for (int j = 0; j < newNodes.size(); j++) {
                        if (matrix.get(i, j) < min && !alreadyDone.contains(j)) {
                            min = matrix.get(i, j);
                            index = j;
                            if (min == 0) {
                                break;
                            }
                        }
                    }
                    if (min != Double.MAX_VALUE) {
                        res[i] = index;
                        alreadyDone.add(index);
                    }
                }
            } else {
                res = Hungarian.assign(matrix);
            }
            initialList.clear();

            for (int i = 0; i < oldNodes.size(); i++) {
                if (res[i] < newNodes.size()) {
                    if (!(skipList.contains(oldNodes.get(i))
                            || skipList.contains(newNodes.get(res[i])))) {
                        if (candidateMap.get(oldNodes.get(i)).get(newNodes.get(res[i])) != null) {
                            initialList.add(
                                    candidateMap.get(oldNodes.get(i)).get(newNodes.get(res[i])));
                        } else {
                            initialList.add(new MatchingCandidate(oldNodes.get(i),
                                    newNodes.get(res[i]), 0.0f));
                        }
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new InternalError(e.toString());
        }
    }

    /**
     * Creates a NodeAggregation that consists of the specified node, it's parent and all of it's
     * siblings.
     */
    private NodeAggregation createAggregation(final ITree node,
                                              final Map<ITree, ITree> parentsMap) {
        if (node.getType() == NODE_AGGREGATION_LABEL) {
            final NodeAggregation aggregation = (NodeAggregation) node;
            final ITree parent = parentsMap.get(aggregation.getAssociatedTree());

            if (parent == null) {
                return null;
            }

            aggregation.setAssociatedTree(parent);
            return aggregation;
        } else {
            final ITree parent = parentsMap.get(node);
            if (parent == null) {
                return null;
            }

            return new NodeAggregation(parent);
        }
    }

    /**
     * A similarity calculation based on a simplified ChangeDistiller matching.
     */
    private float simpleSimilarity(final ITree tree1, final ITree tree2,
                                   ConcurrentHashMap<String, Float> stringSimCache, boolean onlyOneClassPair,
                                   IdentityHashMap<ITree, Mapping> resultMap, NGramCalculator stringSim,
                                   ConcurrentHashMap<ITree, MatchingCandidate> currentMatchings) {

        float similarity = 0.0f;
        if (tree1.getType() == labelConfiguration.rootLabel
                && tree2.getType() == labelConfiguration.rootLabel) {
            return 1.0f;
        } else if (tree1.getType() == labelConfiguration.rootLabel
                || tree2.getType() == labelConfiguration.rootLabel) {
            return 0.0f;
        }
        HashSet<Mapping> currentResultSet = new HashSet<>();
        final LinkedList<MatchingCandidate> matchedLeaves = new LinkedList<>();

        Set<ITree> leaves1 = null;
        Set<ITree> unmatchedNodes1 = null;
        PostOrderSetGenerator setGeneratorNew = new PostOrderSetGenerator();

        setGeneratorNew.createSetsForNode(tree1);
        leaves1 = setGeneratorNew.getSetOfLeaves();
        unmatchedNodes1 = setGeneratorNew.getSetOfNodes();

        setGeneratorNew.createSetsForNode(tree2);
        Set<ITree> leaves2 = setGeneratorNew.getSetOfLeaves();
        final Set<ITree> unmatchedNodes2 = setGeneratorNew.getSetOfNodes();

        if (tree1.getType() == labelConfiguration.classLabel
                && tree2.getType() == labelConfiguration.classLabel) {
            if (onlyOneClassPair) {
                return 1.0f;
            }
            String sc1 = tree1.getLabel();
            String sc2 = tree2.getLabel();

            float csim = stringSim.similarity(sc1, sc2);
            if (csim > 0.6) {
                if (leaves1.size() > 3000 && leaves2.size() > 3000) {
                    float diffCount = Math.abs(leaves1.size() - leaves2.size())
                            / (float) Math.max(leaves1.size(), leaves2.size());
                    if (diffCount < 0.2) {
                        return (float) csim + (1 - diffCount) / (float) 2;
                    }
                }
            }
        } else if (tree1.getType() == labelConfiguration.classLabel
                || tree2.getType() == labelConfiguration.classLabel) {
            return 0.0f;
        }
        final int nodeCount = Math.max(unmatchedNodes1.size(), unmatchedNodes2.size());
        final Set<Type> nodeTags = new HashSet<>();
        for (final ITree firstNode : leaves1) {
            nodeTags.add(firstNode.getType());
        }
        for (final ITree secondNode : leaves2) {
            nodeTags.add(secondNode.getType());
        }
        ArrayList<ITree> leaves1tmp = new ArrayList<ITree>();
        ArrayList<ITree> leaves2tmp = new ArrayList<ITree>();
        leaves1tmp.addAll(leaves1);
        leaves2tmp.addAll(leaves2);
        if (currentMatchings.size() > 0 || resultMap.size() > 0) {
            Iterator<ITree> nodeIt = leaves1tmp.iterator();
            while (nodeIt.hasNext()) {
                ITree next = nodeIt.next();
                Mapping pair = resultMap.get(next);
                if (pair != null) {
                    nodeIt.remove();
                    if (leaves2tmp.contains(pair.second)) {
                        currentResultSet.add(pair);
                        leaves2tmp.remove(pair.second);
                    }
                } else {
                    MatchingCandidate mc = currentMatchings.get(next);
                    if (mc != null) {
                        currentResultSet.add(mc.dropValue());
                        nodeIt.remove();
                        leaves2tmp.remove(mc.second);
                    }
                }

            }
        }
        for (Type tag : nodeTags) {

            ArrayList<ITree> subLeaves1 = new ArrayList<ITree>();
            ArrayList<ITree> subLeaves2 = new ArrayList<ITree>();
            Iterator<ITree> it = leaves1tmp.iterator();
            while (it.hasNext()) {
                ITree node = it.next();
                if (node.getType() == tag) {
                    subLeaves1.add(node);
                    it.remove();
                }
            }
            it = leaves2tmp.iterator();
            while (it.hasNext()) {
                ITree node = it.next();
                if (node.getType() == tag) {
                    subLeaves2.add(node);
                    it.remove();
                }
            }

            for (final ITree firstNode : subLeaves1) {
                float maxSim = Float.MIN_VALUE;
                ArrayList<MatchingCandidate> canlist = new ArrayList<>();
                for (final ITree secondNode : subLeaves2) {
                    float sim = Float.MIN_VALUE;
                    if (labelConfiguration.labelsForStringCompare.contains(tag)) {
                        if (firstNode.getLabel() == null || secondNode.getLabel() == null) {
                            sim = 0.0f;
                        } else if (firstNode.getLabel().equals(secondNode.getLabel())) {
                            sim = 1.0f;
                        } else {

                            if (stringSimCache.get(
                                    firstNode.getLabel() + "@@" + secondNode.getLabel()) != null) {
                                sim = stringSimCache
                                        .get(firstNode.getLabel() + "@@" + secondNode.getLabel());
                            } else if (stringSimCache.get(
                                    secondNode.getLabel() + "@@" + firstNode.getLabel()) != null) {
                                sim = stringSimCache
                                        .get(secondNode.getLabel() + "@@" + firstNode.getLabel());
                            } else {
                                sim = stringSim.similarity(firstNode.getLabel(),
                                        secondNode.getLabel());
                            }
                        }
                    } else if (tag == labelConfiguration.basicTypeLabel
                            || tag == labelConfiguration.qualifierLabel) {
                        if (firstNode.getLabel().equals(secondNode.getLabel())) {
                            sim = 1.0f;
                        } else {
                            sim = 0.0f;
                        }
                    } else {
                        sim = leafMatcher.leavesSimilarity(firstNode, secondNode);

                    }
                    if (sim >= maxSim && sim > 0.0f) {
                        if (sim > maxSim && !canlist.isEmpty()) {
                            canlist.clear();
                        }
                        if (leafMatcher.match(firstNode, secondNode, sim)) {
                            final MatchingCandidate candidate =
                                    new MatchingCandidate(firstNode, secondNode, sim);
                            if (sim > maxSim) {
                                maxSim = sim;
                            }
                            canlist.add(candidate);
                        }
                    }
                }
                matchedLeaves.addAll(canlist);
            }

        }

        IdentityHashMap<ITree, Integer> orderedList1 =
                MtDiffOptimizedMatcher.getNodesInOrder(tree1);
        IdentityHashMap<ITree, Integer> orderedList2 =
                MtDiffOptimizedMatcher.getNodesInOrder(tree2);
        Collections.sort(matchedLeaves, new PairComparator(orderedList1, orderedList2));

        while (!matchedLeaves.isEmpty()) {
            final MatchingCandidate pair = matchedLeaves.pollLast();

            if (!unmatchedNodes1.remove(pair.first)) {
                continue;
            }
            if (!unmatchedNodes2.remove(pair.second)) {
                continue;
            }

            currentResultSet.add(pair.dropValue());
            similarity += pair.getValue();
        }
        InnerNodeSimilarityCalculator nodeMatcher =
                new InnerNodeSimilarityCalculator(labelConfiguration, leavesMap1, leavesMap2,
                        directChildrenMap1, directChildrenMap2, currentResultSet);
        for (final ITree firstNode : unmatchedNodes1) {
            final Iterator<ITree> iterator = unmatchedNodes2.iterator();
            if (firstNode.getChildren() == null || firstNode.getChildren().size() == 0) {
                continue;
            }
            while (iterator.hasNext()) {
                final ITree secondNode = iterator.next();
                if (firstNode.getType() != secondNode.getType()) {
                    continue;
                }
                float sim = 0.0f;
                sim = nodeMatcher.similarity(firstNode, secondNode);

                if (nodeMatcher.match(firstNode, secondNode, sim)) {
                    similarity += sim;

                    iterator.remove();
                    break;
                }
            }
        }
        matchedLeaves.clear();
        unmatchedNodes1.clear();
        unmatchedNodes2.clear();
        currentResultSet.clear();
        leaves1tmp.clear();
        leaves2tmp.clear();
        return similarity / (float) nodeCount;
    }

    private void updateSimilarityRow(boolean[][] aggregationFinished, double[][] similarityScores,
                                     ITree[] firstAggregations, ITree[] secondAggregations,
                                     ConcurrentHashMap<ITree, MatchingCandidate> currentResultMap, AtomicBoolean changed,
                                     int ipar, ArrayList<ITree> newNodes, boolean onlyOneClassPair,
                                     IdentityHashMap<ITree, Mapping> resultMap, NGramCalculator stringSim,
                                     ConcurrentHashMap<String, Float> stringSimCache,
                                     ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, Float>> similarityCache,
                                     AtomicLong similarityEntries) {
        {

            for (int j = 0; j < newNodes.size(); j++) {
                if (!aggregationFinished[ipar][j]) {
                    if (firstAggregations[ipar] != null
                            && firstAggregations[ipar].getType() != NodeAggregation.TAG) {
                        firstAggregations[ipar] = new NodeAggregation(firstAggregations[ipar]);
                    }
                    if (secondAggregations[j] != null
                            && secondAggregations[j].getType() != NodeAggregation.TAG) {
                        secondAggregations[j] = new NodeAggregation(secondAggregations[j]);
                    }
                    final NodeAggregation firstAggregation =
                            (NodeAggregation) firstAggregations[ipar];
                    final NodeAggregation secondAggregation =
                            (NodeAggregation) secondAggregations[j];
                    if (firstAggregation == null || firstAggregation.getAssociatedTree() == null
                            || secondAggregation == null
                            || secondAggregation.getAssociatedTree() == null) {
                        aggregationFinished[ipar][j] = true;
                    } else {
                        float similarity = Float.MIN_VALUE;
                        ConcurrentHashMap<ITree, Float> map =
                                similarityCache.get(firstAggregation.getAssociatedTree());
                        if (firstAggregation.getHash() == secondAggregation.getHash()) {
                            similarity = 1.0f;
                        } else if (firstAggregation.getAssociatedTree()
                                .getType() != secondAggregation.getAssociatedTree().getType()) {
                            similarity = 0.0f;

                        } else {
                            if (leavesMap1.get(firstAggregation.getAssociatedTree()).size()
                                    * leavesMap2.get(secondAggregation.getAssociatedTree())
                                            .size() > 10000) {

                                if (map != null) {
                                    Float value = map.get(secondAggregation.getAssociatedTree());
                                    if (value != null) {
                                        similarity = value;
                                    } else {
                                        similarity = simpleSimilarity(
                                                firstAggregation.getAssociatedTree(),
                                                secondAggregation.getAssociatedTree(),
                                                stringSimCache, onlyOneClassPair, resultMap,
                                                stringSim, currentResultMap);
                                        if (similarityEntries
                                                .get() < MtDiffOptimizedMatcher.SIMILIARITY_CACHE_SIZE) {
                                            map.put(secondAggregation.getAssociatedTree(),
                                                    similarity);
                                            similarityEntries.incrementAndGet();
                                        }
                                    }
                                } else {
                                    ConcurrentHashMap<ITree, Float> tmp = new ConcurrentHashMap<>();
                                    similarity =
                                            simpleSimilarity(firstAggregation.getAssociatedTree(),
                                                    secondAggregation.getAssociatedTree(),
                                                    stringSimCache, onlyOneClassPair, resultMap,
                                                    stringSim, currentResultMap);
                                    if (similarityEntries
                                            .get() < MtDiffOptimizedMatcher.SIMILIARITY_CACHE_SIZE) {
                                        similarityCache.put(firstAggregation.getAssociatedTree(),
                                                tmp);
                                        tmp.put(secondAggregation.getAssociatedTree(), similarity);
                                        similarityEntries.incrementAndGet();
                                    }
                                }
                            } else {
                                similarity = simpleSimilarity(firstAggregation.getAssociatedTree(),
                                        secondAggregation.getAssociatedTree(), stringSimCache,
                                        onlyOneClassPair, resultMap, stringSim, currentResultMap);
                            }

                        }
                        changed.set(true);
                        similarityScores[ipar][j] += similarity;
                    }

                }
            }

        }
        return;
    }

    private class IdComparator implements Comparator<ITree> {

        /**
         * Compare.
         *
         * @param o1 the o1
         * @param o2 the o2
         * @return the int
         */
        @Override
        public int compare(ITree o1, ITree o2) {
            return Integer.compare(o1.getMetrics().position,
                    o2.getMetrics().position);
        }

    }
}
