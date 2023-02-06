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

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Type;

import java.util.Comparator;
import java.util.Map;

/**
 * Comparator used for leaf matching to enforce an ordering on matched pairs.
 */
public class PairComparator implements Comparator<MatchingCandidate> {

    private Map<ITree, Integer> order1 = null;
    private Map<ITree, Integer> order2 = null;

    /**
     * Instantiates a new pair comparator.
     *
     * @param order1 the order1
     * @param order2 the order2
     */
    public PairComparator(Map<ITree, Integer> order1, Map<ITree, Integer> order2) {
        this.order1 = order1;
        this.order2 = order2;
    }


    /**
     * Compare two candidate pairs.
     *
     * @param o1 the o1
     * @param o2 the o2
     * @return the int
     */
    public int compare(final MatchingCandidate o1, final MatchingCandidate o2) {

        final int valComparisonResult = o1.getValue().compareTo(o2.getValue());
        if (valComparisonResult != 0) {
            return valComparisonResult;
        }

        // Compare tags of elements:
        Type tag1 = o1.first.getType();
        Type tag2 = o2.first.getType();
        if (tag1 != tag2) {
            return Integer.compare(tag1.hashCode(), tag2.hashCode());
        }

        int index1 = order1.get(o1.first);
        int index2 = order1.get(o2.first);

        // Tie breaker
        int index = Long.compare(index1, index2);
        if (index != 0) {
            return index;
        }

        index1 = order2.get(o1.second);
        index2 = order2.get(o2.second);
        index = Long.compare(index1, index2);
        if (index == 0) {
            return Long.compare(o2.getId(), o1.getId());

        }
        assert (index != 0);
        return index;
    }
}
