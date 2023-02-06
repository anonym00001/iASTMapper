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

import java.util.Comparator;
import java.util.Map;

class BreadthFirstComparator<T> implements Comparator<T> {
    private Map<T, Integer> order;

    BreadthFirstComparator(Map<T, Integer> order) {
        this.order = order;
    }


    /**
     * Compare two objects based on a specified order.
     *
     * @param o1 the o1
     * @param o2 the o2
     * @return the int
     */
    @Override
    public int compare(T o1, T o2) {
        int index1 = order.get(o1);
        int index2 = order.get(o2);
        return Integer.compare(index1, index2);
    }

}
