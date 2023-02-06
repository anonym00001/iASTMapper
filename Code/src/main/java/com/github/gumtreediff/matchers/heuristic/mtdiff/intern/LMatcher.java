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

import com.github.gumtreediff.matchers.heuristic.mtdiff.similarity.GaussianFloatSimilarityMeasure;
import com.github.gumtreediff.matchers.heuristic.mtdiff.similarity.GaussianIntSimilarityMeasure;
import com.github.gumtreediff.matchers.heuristic.mtdiff.similarity.NGramCalculator;
import com.github.gumtreediff.tree.ITree;

import java.math.BigInteger;

public class LMatcher {

    private GaussianFloatSimilarityMeasure floatSim = new GaussianFloatSimilarityMeasure(1);
    private GaussianIntSimilarityMeasure intSim = new GaussianIntSimilarityMeasure(1);
    private LabelConfiguration labelConfiguration;

    private NGramCalculator stringSim = new NGramCalculator(2, 10, 10);

    private final double threshold;

    /**
     * Instantiates a new leaf matcher.
     *
     * @param labelConfiguration the label configuration
     * @param threshold          the threshold to determine matching leaves
     */
    public LMatcher(LabelConfiguration labelConfiguration, double threshold) {
        this.labelConfiguration = labelConfiguration;
        this.threshold = threshold;
    }

    /**
     * Leaves similarity.
     *
     * @param first  the first
     * @param second the second
     * @return the float
     */
    public float leavesSimilarity(ITree first, ITree second) {
        if (first.getType() != second.getType()) {
            return 0.0f;
        }
        if (first.getChildren().size() != 0) {
            return 0.0f;
        }
        if (second.getChildren().size() != 0) {
            return 0.0f;
        }
        if (labelConfiguration.labelsForValueCompare.contains(first.getType())) {
            if (first.getLabel().equals(second.getLabel())) {
                return 1.0f;
            }
            return 0.0f;
        } else if (labelConfiguration.labelsForRealCompare.contains(first.getType())) {
            Double firstDoubleValue = new Double(first.getLabel());
            Double secondDoubleValue = new Double(second.getLabel());
            return floatSim.similarity(firstDoubleValue, secondDoubleValue);
        } else if (labelConfiguration.labelsForIntCompare.contains(first.getType())) {
            try {
                BigInteger firstValue = new BigInteger(first.getLabel());
                BigInteger secondValue = new BigInteger(second.getLabel());
                if (firstValue.compareTo(secondValue) == 0) {
                    return 1.0f;
                }
                return intSim.similarity(firstValue, secondValue);
            } catch (NumberFormatException e) {
                try {
                    Double firstDoubleValue = new Double(first.getLabel());
                    Double secondDoubleValue = new Double(second.getLabel());
                    return floatSim.similarity(firstDoubleValue, secondDoubleValue);
                } catch (NullPointerException e2) {
                    return 0.0f;
                }
            }
        } else if (labelConfiguration.labelsForStringCompare.contains(first.getType())) {
            return stringSim.similarity(first.getLabel(), second.getLabel());
        } else if (labelConfiguration.labelsForBoolCompare.contains(first.getType())) {
            if (first.getLabel().equals(second.getLabel())) {
                return 1.0f;
            }
            return 0.5f;
        } else {
            return 1.0f;
        }
    }

    /**
     * Determine whether two leaves are a match.
     *
     * @param first      the first
     * @param second     the second
     * @param similarity the similarity
     * @return true, if successful
     */
    public boolean match(ITree first, ITree second, float similarity) {
        return similarity >= threshold;
    }
}
