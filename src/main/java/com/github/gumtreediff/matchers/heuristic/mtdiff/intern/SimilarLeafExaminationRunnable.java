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
import com.github.gumtreediff.matchers.heuristic.mtdiff.similarity.NGramCalculator;
import com.github.gumtreediff.tree.ITree;
import cs.sysu.evaluation.config.MyTimeUtil;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLong;

public class SimilarLeafExaminationRunnable implements Callable<Set<MatchingCandidate>> {

    private ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, MatchingCandidate>> candidateMap =
            new ConcurrentHashMap<>();
    private AtomicInteger count;
    private Map<ITree, ArrayList<ITree>> directChildrenMap1 = null;
    private Map<ITree, ArrayList<ITree>> directChildrenMap2 = null;
    private ConcurrentSkipListSet<MatchingCandidate> initialList;

    private HashSet<MatchingCandidate> initialListOld;
    private LabelConfiguration labelConfiguration;
    private Map<ITree, ArrayList<ITree>> leavesMap1 = null;
    private Map<ITree, ArrayList<ITree>> leavesMap2 = null;
    private LMatcher leafMatcher;
    private ArrayList<ITree> newNodes;
    private ArrayList<ITree> oldNodes;
    private boolean onlyOneClassPair;
    private IdentityHashMap<ITree, Integer> orderedListNew;
    private IdentityHashMap<ITree, Integer> orderedListOld;
    private Map<ITree, ITree> parents1;
    private Map<ITree, ITree> parents2;
    private IdentityHashMap<ITree, Mapping> resultMap;
    private ITree root1;
    private ITree root2;
    private ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, Float>> similarityCache;
    private AtomicLong similarityEntries;
    private NGramCalculator stringSim = new NGramCalculator(2, 10, 10);
    private ConcurrentHashMap<String, Float> stringSimCache;
    private double weightPosition;
    private double weightSimilarity;

    /**
     * Instantiates a new optimized tree difference runnable.
     *
     * @param oldNodes the old nodes
     * @param newNodes the new nodes
     * @param initialListOld the initial list old
     * @param count the count
     * @param stringSimCache the string sim cache
     * @param onlyOneClassPair the only one class pair
     * @param orderedListOld the ordered list old
     * @param orderedListNew the ordered list new
     * @param resultMap the result map
     * @param similarityCache the similarity cache
     * @param similarityEntries the similarity entries
     * @param parents1 the parents1
     * @param parents2 the parents2
     * @param leavesMap1 the leaves map1
     * @param leavesMap2 the leaves map2
     * @param labelConfiguration the label configuration
     * @param leafMatcher the l matcher
     * @param directChildrenMap1 the direct children map1
     * @param directChildrenMap2 the direct children map2
     * @param root1 the root1
     * @param root2 the root2
     * @param weightSimilarity the weight similarity
     * @param weightPosition the weight position
     */
    public SimilarLeafExaminationRunnable(ArrayList<ITree> oldNodes, ArrayList<ITree> newNodes,
                                          HashSet<MatchingCandidate> initialListOld, AtomicInteger count,
                                          ConcurrentHashMap<String, Float> stringSimCache, boolean onlyOneClassPair,
                                          IdentityHashMap<ITree, Integer> orderedListOld,
                                          IdentityHashMap<ITree, Integer> orderedListNew,
                                          IdentityHashMap<ITree, Mapping> resultMap,
                                          ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, Float>> similarityCache,
                                          AtomicLong similarityEntries, Map<ITree, ITree> parents1, Map<ITree, ITree> parents2,
                                          Map<ITree, ArrayList<ITree>> leavesMap1, Map<ITree, ArrayList<ITree>> leavesMap2,
                                          LabelConfiguration labelConfiguration, LMatcher leafMatcher,
                                          Map<ITree, ArrayList<ITree>> directChildrenMap1,
                                          Map<ITree, ArrayList<ITree>> directChildrenMap2, ITree root1, ITree root2,
                                          double weightSimilarity, double weightPosition) {
        super();
        this.oldNodes = oldNodes;
        this.newNodes = newNodes;
        this.initialListOld = initialListOld;
        this.count = count;
        this.similarityCache = similarityCache;
        this.stringSimCache = stringSimCache;
        this.onlyOneClassPair = onlyOneClassPair;
        this.orderedListOld = orderedListOld;
        this.orderedListNew = orderedListNew;
        this.resultMap = resultMap;
        count.incrementAndGet();
        this.similarityEntries = similarityEntries;
        this.labelConfiguration = labelConfiguration;
        this.leafMatcher = leafMatcher;
        this.leavesMap1 = leavesMap1;
        this.leavesMap2 = leavesMap2;
        this.parents1 = parents1;
        this.parents2 = parents2;
        this.directChildrenMap1 = directChildrenMap1;
        this.directChildrenMap2 = directChildrenMap2;
        this.root1 = root1;
        this.root2 = root2;
        this.weightSimilarity = weightSimilarity;
        this.weightPosition = weightPosition;
    }


    /**
     * Call.
     *
     * @return the sets the
     * @throws Exception the exception
     */
    @Override
    public Set<MatchingCandidate> call() throws Exception {
        MyTimeUtil.checkCurrentTime();
        try {
            initialList =
                    new ConcurrentSkipListSet<>(new PairComparator(orderedListOld, orderedListNew));
            initialList.addAll(initialListOld);
            BreadthFirstComparator<ITree> compOld =
                    new BreadthFirstComparator<ITree>(orderedListOld);
            Collections.sort(oldNodes, compOld);
            compOld = null;
            BreadthFirstComparator<ITree> compNew =
                    new BreadthFirstComparator<ITree>(orderedListNew);
            Collections.sort(newNodes, compNew);
            compNew = null;
            boolean[][] aggregationFinished = new boolean[oldNodes.size()][newNodes.size()];
            double[][] similarityScores = new double[oldNodes.size()][newNodes.size()];
            ITree[] firstAggregations = new ITree[oldNodes.size()];
            ITree[] secondAggregations = new ITree[newNodes.size()];
            final ConcurrentSkipListSet<MatchingCandidate> resultList =
                    new ConcurrentSkipListSet<>(new PairComparator(orderedListOld, orderedListNew));
            final ConcurrentHashMap<ITree, MatchingCandidate> currentResultMap =
                    new ConcurrentHashMap<>();
            AtomicBoolean[] doneOld = new AtomicBoolean[oldNodes.size()];
            for (int i = 0; i < oldNodes.size(); i++) {
                doneOld[i] = new AtomicBoolean();
            }
            for (MatchingCandidate mc : initialList) {

                ConcurrentHashMap<ITree, MatchingCandidate> tmp = new ConcurrentHashMap<>();
                ConcurrentHashMap<ITree, MatchingCandidate> tmp2 = candidateMap.put(mc.first, tmp);
                if (tmp2 != null) {
                    tmp = tmp2;
                }
                tmp.put(mc.second, mc);
            }
            for (MatchingCandidate mc : initialList) {

                ConcurrentHashMap<ITree, MatchingCandidate> tmp = candidateMap.get(mc.first);
                if (tmp == null) {
                    tmp = new ConcurrentHashMap<>();
                    ConcurrentHashMap<ITree, MatchingCandidate> tmp2 =
                            candidateMap.putIfAbsent(mc.first, tmp);
                    if (tmp2 != null) {
                        tmp = tmp2;
                    }
                }
                tmp.put(mc.second, mc);
            }
            for (int i = 0; i < oldNodes.size(); i++) {
                firstAggregations[i] = oldNodes.get(i);
            }
            for (int j = 0; j < newNodes.size(); j++) {
                secondAggregations[j] = newNodes.get(j);
            }
            for (int i = 0; i < oldNodes.size(); i++) {
                ITree oldNode = oldNodes.get(i);
                ConcurrentHashMap<ITree, MatchingCandidate> tmp = candidateMap.get(oldNode);
                if (tmp != null) {
                    for (int j = 0; j < newNodes.size(); j++) {
                        ITree newNode = newNodes.get(j);
                        MatchingCandidate mc = tmp.get(newNode);
                        if (mc == null) {
                            aggregationFinished[i][j] = true;
                            similarityScores[i][j] = Float.MIN_VALUE;
                        } else {
                            aggregationFinished[i][j] = false;
                            similarityScores[i][j] = mc.getValue();
                        }
                    }
                } else {
                    for (int j = 0; j < newNodes.size(); j++) {
                        aggregationFinished[i][j] = true;
                        similarityScores[i][j] = Float.MIN_VALUE;
                    }
                }
            }
            AtomicBoolean changed = new AtomicBoolean(true);
            AtomicIntegerArray foundMaxArray = new AtomicIntegerArray(oldNodes.size());

            new SimilarityMatrixHelper(aggregationFinished, firstAggregations, secondAggregations,
                    currentResultMap, changed, oldNodes, newNodes, resultMap, stringSim,
                    stringSimCache, onlyOneClassPair, similarityScores, initialList, candidateMap,
                    foundMaxArray, similarityCache, similarityEntries, parents1, parents2,
                    leavesMap1, leavesMap2, labelConfiguration, leafMatcher, directChildrenMap1,
                    directChildrenMap2, root1, root2, weightSimilarity, weightPosition).call();

            resultList.addAll(initialList);
            count.decrementAndGet();
            stringSim.clear();
            initialList.clear();
            for (Entry<ITree, ConcurrentHashMap<ITree, MatchingCandidate>> entry : candidateMap
                    .entrySet()) {
                if (entry.getValue() != null) {
                    entry.getValue().clear();
                }
            }
            candidateMap.clear();
            firstAggregations = null;
            secondAggregations = null;
            similarityScores = null;
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
