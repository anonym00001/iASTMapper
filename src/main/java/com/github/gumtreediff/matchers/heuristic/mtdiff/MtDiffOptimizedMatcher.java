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
package com.github.gumtreediff.matchers.heuristic.mtdiff;

import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.heuristic.mtdiff.intern.*;
import com.github.gumtreediff.matchers.heuristic.mtdiff.similarity.InnerNodeSimilarityCalculator;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Type;
import cs.sysu.evaluation.config.MyTimeUtil;
import cs.sysu.algorithm.utils.GumTreeException;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This implements MTDIFF.
 */
public class MtDiffOptimizedMatcher implements Matcher {

    private static final int HASH_MAP_SIZE = 50000;
    public static final int SIMILIARITY_CACHE_SIZE = 100000;

    /**
     * Gets the nodes in order.
     *
     * @param tree the tree
     * @return the nodes in order
     */
    public static IdentityHashMap<ITree, Integer> getNodesInOrder(ITree tree) {
        IdentityHashMap<ITree, Integer> nodes = new IdentityHashMap<>();
        ConcurrentLinkedQueue<ITree> workList = new ConcurrentLinkedQueue<ITree>();
        workList.add(tree);
        int counter = 0;
        while (!workList.isEmpty()) {
            ITree node = (ITree) workList.remove();
            nodes.put(node, counter);
            counter++;
            for (ITree child : node.getChildren()) {
                workList.add(child);
            }
        }
        return nodes;
    }

    private static int numberOfNodesWithTag(ITree tree, Type tag) {
        ConcurrentLinkedQueue<ITree> workList = new ConcurrentLinkedQueue<ITree>();
        workList.add(tree);
        int counter = 0;
        while (!workList.isEmpty()) {
            ITree node = (ITree) workList.remove();
            if (node.getType() == tag) {
                counter++;
            }
            for (ITree child : node.getChildren()) {
                workList.add(child);
            }
        }
        return counter;
    }

    private TreeMatcherConfiguration configuration;
    private ExecutorService executorService;

    private LabelConfiguration labelConfiguration;

    private AtomicLong similarityEntries = new AtomicLong(0);
    private ITree src;
    private ITree dst;
    private MappingStore mappings;

//    /**
//     * Instantiates the MTDIFF matcher.
//     *
//     * @param src the src
//     * @param dst the dst
//     * @param store the store
//     */
//    public MtDiffOptimizedMatcher(ITree src, ITree dst, MappingStore store) {
//        this.src = src;
//        this.dst = dst;
//        this.mappings = store;
//    }

    /**
     * Match with MTDIFF.
     * @return
     */
    @Override
    public MappingStore match(ITree src, ITree dst, MappingStore mappings) {
        this.src = src;
        this.dst = dst;
        this.mappings = mappings;
        try {
            MyTimeUtil.setStartMatchTime();
            computeMatchingPairs();

        } catch (Exception e) {
            e.printStackTrace();
            throw new GumTreeException(e.getMessage());
        }

        return mappings;
    }

    public ITree getSrc() {
        return src;
    }

    public ITree getDst() {
        return dst;
    }

    private void computeMatchingPairs() throws Exception {
        final LMatcher lMatcher = new LMatcher(labelConfiguration, configuration.leafThreshold);
        IdentityHashMap<ITree, Integer> orderedList1 = getNodesInOrder(getSrc());
        IdentityHashMap<ITree, Integer> orderedList2 = getNodesInOrder(getDst());
        final ConcurrentSkipListSet<MatchingCandidate> matchedLeaves =
                new ConcurrentSkipListSet<>(new PairComparator(orderedList1, orderedList2));
        IdentityHashMap<ITree, Mapping> resultMap = new IdentityHashMap<>();
        Set<ITree> leaves1 = null;
        PostOrderSetGenerator setGeneratorNew = new PostOrderSetGenerator();
        int countClasses1 = numberOfNodesWithTag(getSrc(), labelConfiguration.classLabel);
        int countClasses2 = numberOfNodesWithTag(getDst(), labelConfiguration.classLabel);
        boolean onlyOneClassPair = false;
        if (countClasses1 == 1 && countClasses2 == 1) {
            onlyOneClassPair = true;
        }

        setGeneratorNew.createSetsForNode(getSrc());
        leaves1 = setGeneratorNew.getSetOfLeaves();
        final Set<ITree> unmatchedNodes1 = setGeneratorNew.getSetOfNodes();

        final IdentityHashMap<ITree, ArrayList<ITree>> leavesMap1 = setGeneratorNew.getLeaveMap();
        final IdentityHashMap<ITree, ArrayList<ITree>> directChildrenMap1 =
                setGeneratorNew.getDirectChildrenMap();
        setGeneratorNew.createSetsForNode(getDst());
        Set<ITree> leaves2 = setGeneratorNew.getSetOfLeaves();
        final Set<ITree> unmatchedNodes2 = setGeneratorNew.getSetOfNodes();

        final IdentityHashMap<ITree, ArrayList<ITree>> leavesMap2 = setGeneratorNew.getLeaveMap();
        final IdentityHashMap<ITree, ArrayList<ITree>> directChildrenMap2 =
                setGeneratorNew.getDirectChildrenMap();

        final Set<Type> nodeTags = new HashSet<>();
        HashMap<Type, Integer> tagSizeMap = new HashMap<>();
        for (final ITree firstNode : leaves1) {
            nodeTags.add(firstNode.getType());
            if (tagSizeMap.get(firstNode.getType()) == null) {
                tagSizeMap.put(firstNode.getType(), 1);
            } else {
                tagSizeMap.put(firstNode.getType(), tagSizeMap.get(firstNode.getType()) + 1);
            }
        }

        for (final ITree secondNode : leaves2) {
            nodeTags.add(secondNode.getType());
            if (tagSizeMap.get(secondNode.getType()) == null) {
                tagSizeMap.put(secondNode.getType(), 1);
            } else {
                tagSizeMap.put(secondNode.getType(), tagSizeMap.get(secondNode.getType()) + 1);
            }
        }

        List<Entry<Type, Integer>> list = new LinkedList<>(tagSizeMap.entrySet());
        Collections.sort(list, new TagComparator(labelConfiguration));
        final ConcurrentHashMap<String, Float> stringSimCache =
                new ConcurrentHashMap<>(HASH_MAP_SIZE);

        ArrayList<ITree> leaves1tmp = new ArrayList<ITree>();
        ArrayList<ITree> leaves2tmp = new ArrayList<ITree>();
        leaves1tmp.addAll(leaves1);
        leaves2tmp.addAll(leaves2);

        HashSet<ITree> skipList = new HashSet<>();

        leaves1 = null;
        leaves2 = null;

        for (Mapping m : this.mappings.asSet()) {
            resultMap.put(m.first, m);
            skipList.add(m.first);
            skipList.add(m.second);
            unmatchedNodes1.remove(m.first);
            unmatchedNodes2.remove(m.second);
        }

        IdentityHashMap<ITree, ITree> parents1 = getParents(getSrc());
        IdentityHashMap<ITree, ITree> parents2 = getParents(getDst());

        matchLeaves(orderedList1, orderedList2, matchedLeaves, resultMap, unmatchedNodes1,
                unmatchedNodes2, onlyOneClassPair, list, stringSimCache, leaves1tmp, leaves2tmp,
                skipList, lMatcher, parents1, parents2, leavesMap1, leavesMap2, directChildrenMap1,
                directChildrenMap2, null);
        HashSet<Mapping> resultSet = new HashSet<>();
        resultSet.addAll(resultMap.values());

        ArrayList<ITree> unmatchedNodesOrdered1 = null;
        ArrayList<ITree> unmatchedNodesOrdered2 = null;
        unmatchedNodesOrdered1 = getUnmachedNodesInOrder(getSrc(), unmatchedNodes1);
        unmatchedNodesOrdered2 = getUnmachedNodesInOrder(getDst(), unmatchedNodes2);
        assert (unmatchedNodes1.size() == 0);
        assert (unmatchedNodes2.size() == 0);
        matchInnerNodes(orderedList1, orderedList2, resultMap, unmatchedNodesOrdered1,
                unmatchedNodesOrdered2, leavesMap1, directChildrenMap1, leavesMap2,
                directChildrenMap2, skipList, resultSet);

        resultSet = new HashSet<>();
        resultSet.addAll(resultMap.values());
        resultMap.clear();
        for (Mapping map : resultSet) {
            mappings.addMapping(map.first, map.second);
        }
    }

    private IdentityHashMap<ITree, ITree> getParents(ITree tree) {
        IdentityHashMap<ITree, ITree> parentMap = new IdentityHashMap<>();
        LinkedList<ITree> workList = new LinkedList<>();
        workList.add(tree);
        while (!workList.isEmpty()) {
            ITree node = workList.removeFirst();
            for (ITree child : node.getChildren()) {
                parentMap.put(child, node);
                workList.add(child);
            }
        }
        return parentMap;
    }

    private ArrayList<ITree> getUnmachedNodesInOrder(ITree tree, Set<ITree> unorderedNodes) {
        ArrayList<ITree> nodes = new ArrayList<>();
        ConcurrentLinkedQueue<ITree> workList = new ConcurrentLinkedQueue<ITree>();
        workList.add(tree);
        while (!workList.isEmpty()) {
            ITree node = (ITree) workList.remove();
            if (unorderedNodes.contains(node)) {
                nodes.add(node);
                unorderedNodes.remove(node);
            }
            for (ITree child : node.getChildren()) {
                workList.add(child);
            }
        }
        return nodes;
    }

    /**
     * Initializes MTDIFF.
     *
     * @param executorService the executor service
     * @param configuration the configuration
     * @param labelConfiguration the label configuration
     */
    public void initMtDiff(ExecutorService executorService, TreeMatcherConfiguration configuration,
            LabelConfiguration labelConfiguration) {
        this.executorService = executorService;
        this.configuration = configuration;
        this.labelConfiguration = labelConfiguration;
    }

    private void matchInnerNodes(IdentityHashMap<ITree, Integer> orderedList1,
                                 IdentityHashMap<ITree, Integer> orderedList2, IdentityHashMap<ITree, Mapping> resultMap,
                                 ArrayList<ITree> unmatchedNodesOrdered1, ArrayList<ITree> unmatchedNodesOrdered2,
                                 IdentityHashMap<ITree, ArrayList<ITree>> leavesMap1,
                                 IdentityHashMap<ITree, ArrayList<ITree>> directChildrenMap1,
                                 IdentityHashMap<ITree, ArrayList<ITree>> leavesMap2,
                                 IdentityHashMap<ITree, ArrayList<ITree>> directChildrenMap2, HashSet<ITree> skipList,
                                 HashSet<Mapping> resultSet) throws InterruptedException, ExecutionException {
        InnerNodeSimilarityCalculator innerMatcher2 =
                new InnerNodeSimilarityCalculator(labelConfiguration, leavesMap1, leavesMap2,
                        directChildrenMap1, directChildrenMap2, resultSet);
        if (unmatchedNodesOrdered1 == null) {
            return;
        }
        // final long time = System.currentTimeMillis();

        AtomicInteger innerMatchCounter = new AtomicInteger(0);
        int start = 0;
        int step = Math.max(unmatchedNodesOrdered1.size() / 16, 1);

        LinkedList<Future<ArrayList<MatchingCandidate>>> results = new LinkedList<>();
        while (start + step < unmatchedNodesOrdered1.size()) {
            InnerMatcherMtDiffRunnable runnable =
                    new InnerMatcherMtDiffRunnable(unmatchedNodesOrdered1, unmatchedNodesOrdered2,
                            innerMatchCounter, start, start + step, innerMatcher2);
            if (executorService != null) {
                results.add(executorService.submit(runnable));
            } else {
                FutureTask<ArrayList<MatchingCandidate>> task = new FutureTask<>(runnable);
                task.run();
                results.add(task);
            }
            start += step;
        }
        InnerMatcherMtDiffRunnable runnable =
                new InnerMatcherMtDiffRunnable(unmatchedNodesOrdered1, unmatchedNodesOrdered2,
                        innerMatchCounter, start, unmatchedNodesOrdered1.size(), innerMatcher2);
        if (executorService != null) {
            results.add(executorService.submit(runnable));
        } else {
            FutureTask<ArrayList<MatchingCandidate>> task = new FutureTask<>(runnable);
            task.run();
            results.add(task);
        }
        LinkedList<MatchingCandidate> candidateList = new LinkedList<>();
        for (Future<ArrayList<MatchingCandidate>> fu : results) {
            ArrayList<MatchingCandidate> candidates;
            try {
                candidates = fu.get();
                candidateList.addAll(candidates);
                candidates.clear();
            } catch (InterruptedException e) {
                e.printStackTrace();
                throw e;
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw e;
            }

        }

        while (innerMatchCounter.get() > 0) {
            assert (false);
            Thread.yield();
        }
        Collections.sort(candidateList, new PairComparator(orderedList1, orderedList2));
        while ((unmatchedNodesOrdered1 != null && unmatchedNodesOrdered2 != null)
                && candidateList.size() > 0) {
            MatchingCandidate can = candidateList.pollLast();

            if (unmatchedNodesOrdered1.contains(can.first)
                    && unmatchedNodesOrdered2.contains(can.second)) {
                unmatchedNodesOrdered1.remove(can.first);
                unmatchedNodesOrdered2.remove(can.second);
                if (!skipList.contains(can.first) && !skipList.contains(can.second)) {
                    resultMap.put(can.first, new Mapping(can.first, can.second));
                }
            }
        }
    }

    private void matchLeaves(IdentityHashMap<ITree, Integer> orderedList1,
                             IdentityHashMap<ITree, Integer> orderedList2,
                             final ConcurrentSkipListSet<MatchingCandidate> matchedLeaves,
                             IdentityHashMap<ITree, Mapping> resultMap, Set<ITree> unmatchedNodes1,
                             Set<ITree> unmatchedNodes2, boolean onlyOneClassPair,
                             List<Entry<Type, Integer>> list, ConcurrentHashMap<String, Float> stringSimCache,
                             ArrayList<ITree> leaves1tmp, ArrayList<ITree> leaves2tmp, HashSet<ITree> skipList,
                             LMatcher leafMatcher, Map<ITree, ITree> parents1, Map<ITree, ITree> parents2,
                             Map<ITree, ArrayList<ITree>> leavesMap1, Map<ITree, ArrayList<ITree>> leavesMap2,
                             Map<ITree, ArrayList<ITree>> directChildrenMap1,
                             Map<ITree, ArrayList<ITree>> directChildrenMap2, HashMap<String, String> renames)
            throws Exception, InterruptedException, ExecutionException {
        ConcurrentHashMap<ITree, ConcurrentHashMap<ITree, Float>> similarityCache =
                new ConcurrentHashMap<>(SIMILIARITY_CACHE_SIZE / 1000);
        for (Entry<Type, Integer> tagEntry : list) {
            Type tag = tagEntry.getKey();
            final ConcurrentHashMap<ITree, ArrayList<MatchingCandidate>> leafCandidateMap =
                    new ConcurrentHashMap<>();
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

            ITree[] nodes = subLeaves1.toArray(new ITree[subLeaves1.size()]);
            subLeaves1.clear();
            AtomicInteger leafSimCounter = new AtomicInteger(0);
            int start = 0;
            int step = Math.max(nodes.length / 16, 1);
            LinkedList<Future<LeafSimResults<ITree>>> leafSimResults = new LinkedList<>();
            while (start + step < nodes.length) {
                LeafSimilarityRunnable runnable = new LeafSimilarityRunnable(stringSimCache, nodes,
                        subLeaves2, leafSimCounter, start, start + step, orderedList1, orderedList2,
                        skipList, leafMatcher, labelConfiguration, renames);
                if (executorService != null) {
                    leafSimResults.add(executorService.submit(runnable));
                } else {
                    FutureTask<LeafSimResults<ITree>> task = new FutureTask<>(runnable);
                    task.run();
                    leafSimResults.add(task);
                }
                start += step;
            }
            LeafSimilarityRunnable runnable = new LeafSimilarityRunnable(stringSimCache, nodes,
                    subLeaves2, leafSimCounter, start, nodes.length, orderedList1, orderedList2,
                    skipList, leafMatcher, labelConfiguration, renames);
            if (executorService != null) {
                leafSimResults.add(executorService.submit(runnable));
            } else {
                FutureTask<LeafSimResults<ITree>> task = new FutureTask<>(runnable);
                task.run();
                leafSimResults.add(task);
            }
            for (Future<LeafSimResults<ITree>> fu : leafSimResults) {

                LeafSimResults<ITree> leafSimRes;
                try {

                    leafSimRes = fu.get();

                    matchedLeaves.addAll(leafSimRes.submatchedLeaves);
                    leafSimRes.submatchedLeaves.clear();

                    for (final Entry<ITree, ArrayList<MatchingCandidate>> entry : leafSimRes.subleafCandidateMap
                            .entrySet()) {
                        ArrayList<MatchingCandidate> myList = leafCandidateMap
                                .putIfAbsent(entry.getKey(), new ArrayList<>(entry.getValue()));
                        if (myList != null) {
                            myList.addAll(entry.getValue());
                        }
                    }
                    leafSimRes.subleafCandidateMap.clear();
                    leafSimRes.submatchedLeaves.clear();

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    throw e;
                }

            }

            while (leafSimCounter.get() > 0) {
                assert (false);
                Thread.yield();
            }
            leafSimResults.clear();
            subLeaves2.clear();
            nodes = null;
            // }
            Map<ITree, ArrayList<MatchingCandidate>> tieCandidates = new HashMap<>();
            for (final Entry<ITree, ArrayList<MatchingCandidate>> entry : leafCandidateMap
                    .entrySet()) {

                Iterator<MatchingCandidate> cit = entry.getValue().iterator();
                float maxSim2 = Float.MIN_VALUE; // Highest value
                while (cit.hasNext()) {
                    final MatchingCandidate next = cit.next();
                    if (next.getValue() > maxSim2) {
                        maxSim2 = next.getValue();
                    }
                }
                cit = entry.getValue().iterator();
                while (cit.hasNext()) {
                    final MatchingCandidate next = cit.next();
                    if (next.getValue() < maxSim2) {
                        cit.remove();
                        matchedLeaves.remove(next);
                    }
                }

                if (entry.getValue().size() > 1) {
                    tieCandidates.put(entry.getKey(), entry.getValue());
                } else {
                    entry.getValue().clear();
                }

            }
            leafCandidateMap.clear();
            leafSimResults = null;

            @SuppressWarnings("unchecked")
            Entry<ITree, ArrayList<MatchingCandidate>>[] array =
                    (Entry<ITree, ArrayList<MatchingCandidate>>[]) tieCandidates.entrySet()
                            .toArray(new Entry[tieCandidates.size()]);

            LinkedList<MatchingCandidate> candidateList = new LinkedList<>();

            IdentityHashMap<ITree, LinkedHashSet<ITree>> nodeMappings = new IdentityHashMap<>();
            IdentityHashMap<ITree, HashSet<MatchingCandidate>> nodeMCs = new IdentityHashMap<>();

            for (Entry<ITree, ArrayList<MatchingCandidate>> tieEntries : array) {
                for (MatchingCandidate mc : tieEntries.getValue()) {
                    if (!nodeMappings.containsKey(mc.first)
                            && !(nodeMappings.containsKey(mc.second))) {
                        LinkedHashSet<ITree> tmp = new LinkedHashSet<>();
                        nodeMappings.put(mc.first, tmp);
                        nodeMappings.put(mc.second, tmp);
                        tmp.add(mc.second);
                        tmp.add(mc.first);
                        HashSet<MatchingCandidate> canList = new HashSet<>();
                        nodeMCs.put(mc.first, canList);
                        nodeMCs.put(mc.second, canList);
                        canList.add(mc);
                    } else if (nodeMappings.containsKey(mc.first)
                            && !nodeMappings.containsKey(mc.second)) {
                        LinkedHashSet<ITree> tmp = nodeMappings.get(mc.first);
                        assert (tmp != null);
                        tmp.add(mc.second);
                        nodeMappings.put(mc.second, tmp);
                        assert (nodeMCs.get(mc.first) != null);
                        nodeMCs.get(mc.first).add(mc);
                        nodeMCs.put(mc.second, nodeMCs.get(mc.first));
                    } else if (!nodeMappings.containsKey(mc.first)
                            && nodeMappings.containsKey(mc.second)) {
                        LinkedHashSet<ITree> tmp = nodeMappings.get(mc.second);
                        assert (tmp != null);
                        tmp.add(mc.first);
                        nodeMappings.put(mc.first, tmp);
                        assert (nodeMCs.get(mc.second) != null);
                        nodeMCs.get(mc.second).add(mc);
                        nodeMCs.put(mc.first, nodeMCs.get(mc.second));
                    } else {
                        LinkedHashSet<ITree> tmpOld = nodeMappings.get(mc.first);
                        LinkedHashSet<ITree> tmpNew = nodeMappings.get(mc.second);
                        assert (tmpOld != null && tmpNew != null);
                        if (tmpOld != tmpNew) {
                            tmpOld.addAll(tmpNew);

                            for (ITree node : tmpNew) {
                                nodeMappings.put(node, tmpOld);
                            }
                            assert (nodeMCs.get(mc.first) != null);
                            HashSet<MatchingCandidate> oldList = nodeMCs.get(mc.first);
                            nodeMCs.get(mc.first).addAll(nodeMCs.get(mc.second));
                            for (ITree node : tmpNew) {
                                nodeMCs.put(node, oldList);
                            }
                            nodeMCs.get(mc.first).add(mc);
                        } else {
                            assert (nodeMCs.get(mc.first) == nodeMCs.get(mc.second));
                            nodeMCs.get(mc.first).add(mc);
                        }
                    }
                }
                if (tieEntries.getValue() != null) {
                    tieEntries.getValue().clear();
                }

            }

            array = null;
            ArrayList<ArrayList<ITree>> oldNodeArray = new ArrayList<>();
            ArrayList<ArrayList<ITree>> newNodeArray = new ArrayList<>();
            ArrayList<HashSet<MatchingCandidate>> mcList = new ArrayList<>();

            for (Entry<ITree, LinkedHashSet<ITree>> entry : nodeMappings.entrySet()) {
                if (entry.getValue().size() > 0) {
                    ArrayList<ITree> listOld = new ArrayList<>();
                    ArrayList<ITree> listNew = new ArrayList<>();
                    for (ITree node : entry.getValue()) {
                        if (orderedList1.containsKey(node)) {
                            listOld.add(node);
                        } else {
                            listNew.add(node);
                        }
                    }
                    mcList.add(nodeMCs.get(entry.getKey()));
                    oldNodeArray.add(listOld);
                    newNodeArray.add(listNew);
                    entry.getValue().clear();
                }
            }

            nodeMappings.clear();
            nodeMappings = null;

            AtomicInteger treeDiffCounter = new AtomicInteger(0);
            LinkedList<Future<Set<MatchingCandidate>>> diffResultList = new LinkedList<>();
            for (int i = 0; i < oldNodeArray.size(); i++) {
                ArrayList<ITree> newNodeList = newNodeArray.get(i);
                ArrayList<ITree> oldNodeList = oldNodeArray.get(i);
                HashSet<MatchingCandidate> subList = mcList.get(i);
                SimilarLeafExaminationRunnable treeDiffRunnable =
                        new SimilarLeafExaminationRunnable(oldNodeList, newNodeList, subList,
                                treeDiffCounter, stringSimCache, onlyOneClassPair, orderedList1,
                                orderedList2, resultMap, similarityCache, similarityEntries,
                                parents1, parents2, leavesMap1, leavesMap2, labelConfiguration,
                                leafMatcher, directChildrenMap1, directChildrenMap2, getSrc(),
                                getDst(), configuration.weightSimilarity,
                                configuration.weightPosition);
                if (oldNodeList.size() * newNodeList.size() > 10000000) {
                    while (treeDiffCounter.get() > 1) {
                        Thread.yield();
                    }
                    for (int j = 0; j < i; j++) {
                        HashSet<MatchingCandidate> subListX = mcList.get(j);
                        matchedLeaves.removeAll(subListX);
                    }
                    for (int j = 0; j < i; j++) {
                        newNodeArray.get(j).clear();
                        oldNodeArray.get(j).clear();
                        mcList.get(j).clear();
                    }
                    similarityCache.clear();
                    similarityEntries.set(SIMILIARITY_CACHE_SIZE);
                    matchedLeaves.removeAll(subList);
                    if (oldNodeList.size() * newNodeList.size() < 12000000) {

                        FutureTask<Set<MatchingCandidate>> task =
                                new FutureTask<>(treeDiffRunnable);
                        task.run();
                        diffResultList.add(task);
                    } else {

                        LinkedList<MatchingCandidate> tmpCandidates = new LinkedList<>();
                        tmpCandidates.addAll(subList);
                        subList.clear();
                        Collections.sort(tmpCandidates,
                                new PairComparator(orderedList1, orderedList2));
                        while (!tmpCandidates.isEmpty()) {
                            final MatchingCandidate pair = tmpCandidates.pollLast();
                            if (pair.first == null) {
                                assert (false);
                            }
                            if (!unmatchedNodes1.contains(pair.first)) {
                                continue;
                            }
                            if (!unmatchedNodes2.contains(pair.second)) {
                                continue;
                            }

                            unmatchedNodes1.remove(pair.first);
                            unmatchedNodes2.remove(pair.second);

                            Mapping tmp = resultMap.get(pair.first);
                            assert (tmp == null);
                            resultMap.put(pair.first, pair);
                        }
                        tmpCandidates.clear();
                        for (Future<Set<MatchingCandidate>> fu : diffResultList) {
                            Set<MatchingCandidate> diffResult;
                            try {
                                diffResult = fu.get();
                                candidateList.addAll(diffResult);
                                diffResult.clear();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                throw e;
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                                throw e;
                            }

                        }
                        Collections.sort(candidateList,
                                new PairComparator(orderedList1, orderedList2));
                        while (!candidateList.isEmpty()) {
                            final MatchingCandidate pair = candidateList.pollLast();
                            if (pair.first == null) {
                                assert (false);
                            }
                            if (!unmatchedNodes1.contains(pair.first)) {
                                continue;
                            }
                            if (!unmatchedNodes2.contains(pair.second)) {
                                continue;
                            }

                            unmatchedNodes1.remove(pair.first);
                            unmatchedNodes2.remove(pair.second);

                            Mapping tmp = resultMap.get(pair.first);
                            assert (tmp == null);
                            resultMap.put(pair.first, pair);
                        }
                        treeDiffCounter.decrementAndGet();
                    }
                    newNodeArray.get(i).clear();
                    oldNodeArray.get(i).clear();
                    similarityEntries.set(0);
                } else {
                    if (executorService != null) {
                        diffResultList.add(executorService.submit(treeDiffRunnable));
                    } else {
                        FutureTask<Set<MatchingCandidate>> task =
                                new FutureTask<>(treeDiffRunnable);
                        task.run();
                        candidateList.addAll(task.get());
                        task.get().clear();
                    }
                }

            }

            for (int i = 0; i < oldNodeArray.size(); i++) {
                HashSet<MatchingCandidate> subList = mcList.get(i);
                matchedLeaves.removeAll(subList);
            }
            candidateList.addAll(matchedLeaves);
            matchedLeaves.clear();
            for (Future<Set<MatchingCandidate>> fu : diffResultList) {
                Set<MatchingCandidate> diffResult;
                try {
                    diffResult = fu.get();
                    candidateList.addAll(diffResult);
                    diffResult.clear();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw e;
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    throw e;
                }

            }

            while (leafSimCounter.get() > 0) {
                assert (false);
                Thread.yield();
            }
            for (int j = 0; j < oldNodeArray.size(); j++) {
                newNodeArray.get(j).clear();
                oldNodeArray.get(j).clear();
                mcList.get(j).clear();
            }
            for (Entry<ITree, HashSet<MatchingCandidate>> e : nodeMCs.entrySet()) {
                if (e.getValue() != null) {
                    e.getValue().clear();
                }
            }

            Collections.sort(candidateList, new PairComparator(orderedList1, orderedList2));
            while (!candidateList.isEmpty()) {
                final MatchingCandidate pair = candidateList.pollFirst();
                if (pair.first == null) {
                    assert (false);
                }
                if (!unmatchedNodes1.contains(pair.first)) {
                    continue;
                }
                if (!unmatchedNodes2.contains(pair.second)) {
                    continue;
                }

                unmatchedNodes1.remove(pair.first);
                unmatchedNodes2.remove(pair.second);
                Mapping tmp = resultMap.get(pair.first);
                assert (tmp == null);
                resultMap.put(pair.first, pair);
            }
            matchedLeaves.clear();
        }
        for (Entry<ITree, ConcurrentHashMap<ITree, Float>> e : similarityCache.entrySet()) {
            e.getValue().clear();
        }
        similarityCache.clear();
        similarityEntries.set(0);
    }
}
