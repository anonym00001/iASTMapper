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

import com.github.gumtreediff.matchers.heuristic.mtdiff.similarity.ValueComparePair;
import com.github.gumtreediff.tree.ITree;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A class representing a matching candidate.
 */
public class MatchingCandidate extends ValueComparePair implements Comparable<MatchingCandidate> {
    private static AtomicLong counter = new AtomicLong();

    private final long id;

    /**
     * Instantiates a new matching candidate.
     *
     * @param oldElement the old element
     * @param newElement the new element
     * @param value      the value
     */
    public MatchingCandidate(final ITree oldElement, final ITree newElement, final Float value) {
        super(oldElement, newElement, value);

        id = counter.incrementAndGet();
    }


    /**
     * Compare to.
     *
     * @param object the o
     * @return the int
     */
    @Override
    public int compareTo(MatchingCandidate object) {
        return Float.compare(object.getValue(), this.getValue());
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public long getId() {
        return id;
    }


    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return "(" + this.first.toString() + ", " + this.second.toString() + ")";
    }
}
