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

public final class GaussianFloatSimilarityMeasure {

    private final float sigma;

    /**
     * Instantiates a new gaussian float similarity measure.
     *
     * @param sigma the sigma
     */
    public GaussianFloatSimilarityMeasure(final float sigma) {
        this.sigma = sigma;
    }

    /**
     * Similarity for two doubles.
     *
     * @param f1 the f1
     * @param f2 the f2
     * @return the similarity
     */
    public float similarity(final double f1, final double f2) {
        final double x = (f1 - f2) / sigma;

        return (float) Math.exp(-0.5 * x * x);
    }

    /**
     * Similarity for two floats.
     *
     * @param f1 the f1
     * @param f2 the f2
     * @return the similarity
     */
    public float similarity(final float f1, final float f2) {
        final float x = (f1 - f2) / sigma;

        return (float) Math.exp(-0.5 * x * x);
    }
}
