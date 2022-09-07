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

import com.github.gumtreediff.matchers.heuristic.mtdiff.similarity.InnerNodeSimilarityCalculator;
import com.github.gumtreediff.tree.ITree;
import cs.sysu.evaluation.config.MyTimeUtil;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class InnerMatcherMtDiffRunnable implements Callable<ArrayList<MatchingCandidate>> {

    private AtomicInteger counter = null;
    private int end;
    private InnerNodeSimilarityCalculator imatcher;
    private int start;
    private ArrayList<ITree> unmatchedNodesOrdered1;
    private ArrayList<ITree> unmatchedNodesOrdered2;
    private final double innerNodeThreshold = 0.099f;
    private final double weightValue = 0.40f;
    private final double weightChildren = 0.25f;


    /**
     * Instantiates the runnable for matching inner nodes.
     *
     * @param unmatchedNodesOrdered1 the unmatched nodes ordered1
     * @param unmatchedNodesOrdered2 the unmatched nodes ordered2
     * @param counter                the counter
     * @param start                  the start
     * @param end                    the end
     * @param imatcher               the imatcher
     */
    public InnerMatcherMtDiffRunnable(ArrayList<ITree> unmatchedNodesOrdered1,
                                      ArrayList<ITree> unmatchedNodesOrdered2, AtomicInteger counter, int start, int end,
                                      InnerNodeSimilarityCalculator imatcher) {
        super();
        this.unmatchedNodesOrdered1 = unmatchedNodesOrdered1;
        this.unmatchedNodesOrdered2 = unmatchedNodesOrdered2;
        this.imatcher = imatcher;
        this.counter = counter;
        this.start = start;
        this.end = end;
        counter.incrementAndGet();
    }


    /**
     * Find the candidates for the mapping.
     *
     * @return the array list
     * @throws Exception the exception
     */
    @Override
    public ArrayList<MatchingCandidate> call() throws Exception {
        try {
            ArrayList<MatchingCandidate> candidates = new ArrayList<>();
            for (int i = start; i < end; i++) {
                ITree firstNode = unmatchedNodesOrdered1.get(i);
                ArrayList<MatchingCandidate> tmp =
                        computeInnerMatchingCandidate(firstNode, unmatchedNodesOrdered2, imatcher);
                candidates.addAll(tmp);
                MyTimeUtil.checkCurrentTime();
            }
            counter.decrementAndGet();
            return candidates;

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }

    private ArrayList<MatchingCandidate> computeInnerMatchingCandidate(ITree firstNode,
                                                                       ArrayList<ITree> unmatchedNodesOrdered2, InnerNodeSimilarityCalculator imatcher) {
        ArrayList<MatchingCandidate> list = new ArrayList<>();
        for (int j = 0; j < unmatchedNodesOrdered2.size(); j++) {
            final ITree secondNode = unmatchedNodesOrdered2.get(j);
            if (firstNode.getType() == secondNode.getType()) {
                float labelSim = 0.0f;
                labelSim = imatcher.similarity(firstNode, secondNode);
                //float combinedSim = imatcher.newSimilarity(firstNode, secondNode, labelSim);
                float childSim = imatcher.mtDiffSimilarity(firstNode, secondNode, labelSim);
                double res = (labelSim * weightValue + childSim * weightChildren) / 2;
                if (res >= innerNodeThreshold) {
                    list.add(new MatchingCandidate(firstNode, secondNode, (float) res));
                }
            }
        }
        return list;
    }

}
