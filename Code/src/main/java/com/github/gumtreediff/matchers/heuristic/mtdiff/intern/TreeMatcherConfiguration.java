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

public class TreeMatcherConfiguration {
    public final double leafThreshold;
    public final double weightPosition;
    public final double weightSimilarity;

    /**
     * Instantiates a new tree matcher configuration.
     *
     * @param leafThreshold    the leaf threshold
     * @param weightSimilarity the weight similarity
     * @param weightPosition   the weight position
     */
    public TreeMatcherConfiguration(double leafThreshold, double weightSimilarity,
                                    double weightPosition) {
        this.leafThreshold = leafThreshold;
        this.weightSimilarity = weightSimilarity;
        this.weightPosition = weightPosition;

    }

}
