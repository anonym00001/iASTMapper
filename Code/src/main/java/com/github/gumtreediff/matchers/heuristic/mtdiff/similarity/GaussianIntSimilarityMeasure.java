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

package com.github.gumtreediff.matchers.heuristic.mtdiff.similarity;

import java.math.BigInteger;

public final class GaussianIntSimilarityMeasure {

    private final float sigma;

    /**
     * Instantiates a new gaussian int similarity measure.
     *
     * @param sigma the sigma
     */
    public GaussianIntSimilarityMeasure(final float sigma) {
        this.sigma = sigma;
    }

    /**
     * Similarity of two big integers.
     *
     * @param i1 the i1
     * @param i2 the i2
     * @return the similarity
     */
    public float similarity(final BigInteger i1, final BigInteger i2) {
        final double x = i1.subtract(i2).doubleValue() / sigma;

        return (float) Math.exp(-0.5 * x * x);
    }

    /**
     * Similarity of two long values.
     *
     * @param i1 the i1
     * @param i2 the i2
     * @return the similarity
     */
    public float similarity(final long i1, final long i2) {
        return similarity(BigInteger.valueOf(i1), BigInteger.valueOf(i2));
    }
}
