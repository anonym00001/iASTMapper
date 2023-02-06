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

import com.github.gumtreediff.tree.Type;

import java.util.Comparator;
import java.util.Map.Entry;

public class TagComparator implements Comparator<Entry<Type, Integer>> {

    private LabelConfiguration labelConfiguration;

    /**
     * Instantiates a new tag comparator.
     *
     * @param labelConfiguration the label configuration
     */
    public TagComparator(LabelConfiguration labelConfiguration) {
        this.labelConfiguration = labelConfiguration;
    }


    /**
     * Compare two tags.
     *
     * @param o1 the o1
     * @param o2 the o2
     * @return the int
     */
    @Override
    public int compare(Entry<Type, Integer> o1, Entry<Type, Integer> o2) {
        if (((Entry<Type, Integer>) (o1)).getKey() == labelConfiguration.identifierLabel) {
            return Integer.MIN_VALUE;
        } else if (((Entry<Type, Integer>) (o2)).getKey() == labelConfiguration.identifierLabel) {
            return Integer.MAX_VALUE;
        } else {
            return (((o1)).getValue()).compareTo(((o2)).getValue());
        }
    }
}
