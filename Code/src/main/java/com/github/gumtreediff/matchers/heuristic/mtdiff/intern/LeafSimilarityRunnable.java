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

package com.github.gumtreediff.matchers.heuristic.mtdiff.intern;

import com.github.gumtreediff.matchers.heuristic.mtdiff.similarity.NGramCalculator;
import com.github.gumtreediff.tree.ITree;
import cs.model.evaluation.config.MyTimeUtil;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

public class LeafSimilarityRunnable implements Callable<LeafSimResults<ITree>> {

    private AtomicInteger counter = null;
    private int end;
    private LabelConfiguration labelConfiguration;
    private LMatcher lmatcher;
    private IdentityHashMap<ITree, Integer> orderedList1;
    private IdentityHashMap<ITree, Integer> orderedList2;
    private HashMap<String, String> renames;
    private HashSet<ITree> skipList;
    private int start;
    private NGramCalculator stringSim = new NGramCalculator(2, 1000, 10000);
    private ConcurrentHashMap<String, Float> stringSimCache;
    private ITree[] subLeaves1;
    private ArrayList<ITree> subLeaves2;

    /**
     * Instantiates a new leaf similarity runnable.
     *
     * @param stringSimCache     the string sim cache
     * @param subLeaves1         the sub leaves1
     * @param subLeaves2         the sub leaves2
     * @param counter            the counter
     * @param start              the start
     * @param end                the end
     * @param orderedList1       the ordered list1
     * @param orderedList2       the ordered list2
     * @param skipList           the skip list
     * @param lmatcher           the lmatcher
     * @param labelConfiguration the label configuration
     * @param renames            the renames
     */
    public LeafSimilarityRunnable(ConcurrentHashMap<String, Float> stringSimCache, ITree[] subLeaves1,
                                  ArrayList<ITree> subLeaves2, AtomicInteger counter, int start, int end,
                                  IdentityHashMap<ITree, Integer> orderedList1, IdentityHashMap<ITree, Integer> orderedList2,
                                  HashSet<ITree> skipList, LMatcher lmatcher, LabelConfiguration labelConfiguration,
                                  HashMap<String, String> renames) {
        super();
        this.stringSimCache = stringSimCache;
        this.subLeaves1 = subLeaves1;
        this.subLeaves2 = subLeaves2;
        this.counter = counter;
        this.start = start;
        this.end = end;
        counter.incrementAndGet();
        this.orderedList1 = orderedList1;
        this.orderedList2 = orderedList2;
        this.skipList = skipList;
        this.lmatcher = lmatcher;
        this.labelConfiguration = labelConfiguration;
        this.renames = renames;
    }


    /**
     * Compute the candidates for the mapping.
     *
     * @return the leaf sim results
     * @throws Exception the exception
     */
    @Override
    public LeafSimResults<ITree> call() throws Exception {
        try {
            ConcurrentHashMap<ITree, ArrayList<MatchingCandidate>> leafCandidateMap =
                    new ConcurrentHashMap<>();
            ConcurrentSkipListSet<MatchingCandidate> matchedLeaves =
                    new ConcurrentSkipListSet<>(new PairComparator(orderedList1, orderedList2));
            ConcurrentHashMap<String, ITree> basicTypeCache = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, ITree> basicNameCache = new ConcurrentHashMap<>();
            ConcurrentHashMap<String, ITree> basicTypeQualifierCache = new ConcurrentHashMap<>();

            for (int i = start; i < end; i++) {
                MyTimeUtil.checkCurrentTime();
                ITree firstNode = subLeaves1[i];
                if (skipList.contains(firstNode)) {
                    continue;
                }
                if (firstNode.getType() == labelConfiguration.basicTypeLabel) {
                    if (basicTypeCache.containsKey(firstNode.getLabel())) {
                        ITree oldNode = basicTypeCache.get(firstNode.getLabel());
                        ArrayList<MatchingCandidate> oldList = leafCandidateMap.get(oldNode);
                        ArrayList<MatchingCandidate> newList = new ArrayList<>();
                        if (oldList != null) {
                            for (MatchingCandidate mc : oldList) {
                                MatchingCandidate newMc =
                                        new MatchingCandidate(firstNode, mc.second, mc.getValue());
                                newList.add(newMc);
                                matchedLeaves.add(newMc);
                                ArrayList<MatchingCandidate> newToOldList = leafCandidateMap.get(mc.second);
                                newToOldList.add(newMc);
                            }
                            leafCandidateMap.put(firstNode, newList);
                            continue;
                        }
                    }
                } else if (firstNode.getType() == labelConfiguration.identifierLabel) {
                    if (basicNameCache.containsKey(firstNode.getLabel())) {
                        ITree oldNode = basicNameCache.get(firstNode.getLabel());
                        ArrayList<MatchingCandidate> oldList = leafCandidateMap.get(oldNode);
                        ArrayList<MatchingCandidate> newList = new ArrayList<>();
                        if (oldList != null) {
                            for (MatchingCandidate mc : oldList) {
                                MatchingCandidate newMc =
                                        new MatchingCandidate(firstNode, mc.second, mc.getValue());
                                newList.add(newMc);
                                matchedLeaves.add(newMc);
                                ArrayList<MatchingCandidate> newToOldList = leafCandidateMap.get(mc.second);
                                newToOldList.add(newMc);
                            }
                            leafCandidateMap.put(firstNode, newList);
                            continue;
                        }
                    }
                } else if (firstNode.getType() == labelConfiguration.qualifierLabel) {
                    if (basicTypeQualifierCache.containsKey(firstNode.getLabel())) {
                        ITree oldNode = basicTypeQualifierCache.get(firstNode.getLabel());
                        ArrayList<MatchingCandidate> oldList = leafCandidateMap.get(oldNode);
                        ArrayList<MatchingCandidate> newList = new ArrayList<>();
                        if (oldList != null) {
                            for (MatchingCandidate mc : oldList) {
                                MatchingCandidate newMc =
                                        new MatchingCandidate(firstNode, mc.second, mc.getValue());
                                newList.add(newMc);
                                matchedLeaves.add(newMc);
                                ArrayList<MatchingCandidate> newToOldList = leafCandidateMap.get(mc.second);
                                newToOldList.add(newMc);
                            }
                            leafCandidateMap.put(firstNode, newList);
                            continue;
                        }
                    }
                }

                computeLeafSimilarities(matchedLeaves, stringSimCache, leafCandidateMap, subLeaves2,
                        stringSim, firstNode, skipList, lmatcher);
                if (firstNode.getType() == labelConfiguration.basicTypeLabel) {
                    basicTypeCache.putIfAbsent(firstNode.getLabel(), firstNode);
                } else if (firstNode.getType() == labelConfiguration.identifierLabel) {
                    basicNameCache.putIfAbsent(firstNode.getLabel(), firstNode);
                } else if (firstNode.getType() == labelConfiguration.qualifierLabel) {
                    basicTypeQualifierCache.putIfAbsent(firstNode.getLabel(), firstNode);
                }
            }
            for (final Map.Entry<ITree, ArrayList<MatchingCandidate>> entry : leafCandidateMap
                    .entrySet()) {
                MyTimeUtil.checkCurrentTime();
                Iterator<MatchingCandidate> cit = entry.getValue().iterator();
                float maxSim2 = Float.MIN_VALUE;
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

            }
            basicTypeCache.clear();
            basicNameCache.clear();
            basicTypeQualifierCache.clear();
            counter.decrementAndGet();
            stringSim.clear();
            return new LeafSimResults<ITree>(leafCandidateMap, matchedLeaves);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private void computeLeafSimilarities(final ConcurrentSkipListSet<MatchingCandidate> matchedLeaves,
                                         ConcurrentHashMap<String, Float> stringSimCache,
                                         final ConcurrentHashMap<ITree, ArrayList<MatchingCandidate>> leafCandidateMap,
                                         ArrayList<ITree> subLeaves2, NGramCalculator stringSim, final ITree firstNode,
                                         HashSet<ITree> skipList, LMatcher lmatcher) {
        float maxSim = Float.MIN_VALUE;
        for (final ITree secondNode : subLeaves2) {
            if (skipList.contains(secondNode)) {
                continue;
            }
            float sim = Float.MIN_VALUE;
            if (renames != null
                    && labelConfiguration.labelsForStringCompare.contains(firstNode.getType())) {
                if (renames.get(firstNode.getLabel()) != null) {
                    if (renames.get(firstNode.getLabel()).equals(secondNode.getLabel())) {
                        sim = 1.0f;
                    }
                }

            } else if (renames != null) {
                continue;
            }
            if (sim == Float.MIN_VALUE) {
                if (secondNode.getType() == labelConfiguration.identifierLabel) {
                    if (firstNode.getLabel() == null || secondNode.getLabel() == null) {
                        sim = 0.0f;
                    } else if (firstNode.getLabel().equals(secondNode.getLabel())) {
                        sim = 1.0f;
                    } else {
                        if (stringSimCache.get(firstNode.getLabel() + "@@" + secondNode.getLabel()) != null) {
                            sim = stringSimCache.get(firstNode.getLabel() + "@@" + secondNode.getLabel());
                        } else if (stringSimCache.get(secondNode.getLabel() + "@@" + firstNode.getLabel()) != null) {
                            sim = stringSimCache.get(secondNode.getLabel() + "@@" + firstNode.getLabel());
                        } else {
                            sim = stringSim.similarity(firstNode.getLabel(), secondNode.getLabel());
                        }
                    }
                } else if (secondNode.getType() == labelConfiguration.basicTypeLabel
                        || secondNode.getType() == labelConfiguration.qualifierLabel) {
                    if (firstNode.getLabel().equals(secondNode.getLabel())) {
                        sim = 1.0f;
                    } else {
                        sim = 0.0f;
                    }
                } else {
                    sim = lmatcher.leavesSimilarity(firstNode, secondNode);

                }
            }
            if (sim >= maxSim && sim > 0.0f) {
                if (lmatcher.match(firstNode, secondNode, sim)) {
                    final MatchingCandidate candidate = new MatchingCandidate(firstNode, secondNode, sim);
                    if (sim > maxSim) {
                        maxSim = sim;
                    }
                    matchedLeaves.add(candidate);
                    insertCandidate(leafCandidateMap, firstNode, candidate, matchedLeaves);
                    insertCandidate(leafCandidateMap, secondNode, candidate, matchedLeaves);
                }

            }

        }
    }

    /**
     * Inserts a {@see MatchingCandidate} into a specified mapping.
     *
     * @param candidateMapping The mapping of a node to all possible candidates.
     * @param key              The ITree to be inserted as a key
     * @param candidate        The candidate to be inserted as a value
     */
    private void insertCandidate(final Map<ITree, ArrayList<MatchingCandidate>> candidateMapping,
                                 final ITree key, final MatchingCandidate candidate,
                                 ConcurrentSkipListSet<MatchingCandidate> matchedLeaves) {
        synchronized (candidateMapping) {
            ArrayList<MatchingCandidate> candidateSet = candidateMapping.get(key);

            if (candidateSet == null) {
                candidateSet = new ArrayList<MatchingCandidate>(10);
                candidateMapping.put(key, candidateSet);
                candidateSet.add(candidate);
            } else {
                candidateSet.add(candidate);
                Iterator<MatchingCandidate> cit = candidateSet.iterator();
                float maxSim2 = Float.MIN_VALUE;
                while (cit.hasNext()) {
                    final MatchingCandidate next = cit.next();
                    if (next.getValue() > maxSim2) {
                        maxSim2 = next.getValue();
                    }
                }
                cit = candidateSet.iterator();
                while (cit.hasNext()) {
                    final MatchingCandidate next = cit.next();
                    if (next.getValue() < maxSim2) {
                        cit.remove();
                        matchedLeaves.remove(next);
                    }
                }
            }
        }
    }

}
