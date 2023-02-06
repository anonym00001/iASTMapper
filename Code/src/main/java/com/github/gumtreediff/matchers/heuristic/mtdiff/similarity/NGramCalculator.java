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

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class NGramCalculator {
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Float>> cache;
    private final int ngram;

    private final int subMapSize;

    /**
     * Instantiates a new ngram calculator.
     *
     * @param ngram the ngram
     */
    public NGramCalculator(final int ngram) {
        this.ngram = ngram;
        cache = new ConcurrentHashMap<>(10000);
        this.subMapSize = 3500200;
    }

    /**
     * Instantiates a new ngram calculator.
     *
     * @param ngram      the n
     * @param cacheSize  the cache size
     * @param subMapSize the sub map size
     */
    public NGramCalculator(final int ngram, int cacheSize, int subMapSize) {
        this.ngram = ngram;
        cache = new ConcurrentHashMap<>(cacheSize);
        this.subMapSize = subMapSize;
    }

    private HashSet<String> calculateNGrams(final String string, final int npar) {
        final HashSet<String> result = new HashSet<String>();

        for (int i = 0; i < string.length() - npar + 1; ++i) {
            result.add(string.substring(i, i + npar));
        }

        return result;
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        for (Entry<String, ConcurrentHashMap<String, Float>> e : cache.entrySet()) {
            if (e.getValue() != null) {
                e.getValue().clear();
            }
        }
        cache.clear();
    }

    /**
     * Compute the similarity.
     *
     * @param first  the first
     * @param second the second
     * @return the float
     */
    public float similarity(final String first, final String second) {
        if (first.equals(second)) {
            return 1.0f;
        } else if (first.equals("") || second.equals("")) {
            return 0.0f;
        } else {
            ConcurrentHashMap<String, Float> map2 = cache.get(first);
            if (map2 != null) {
                Float result = map2.get(second);
                if (result != null) {
                    return result;
                }
            } else {
                map2 = new ConcurrentHashMap<>();
                cache.put(first, map2);
            }

            final int m = Math.min(ngram, Math.min(first.length(), second.length()));
            final HashSet<String> firstNGrams = calculateNGrams(first, m);
            final HashSet<String> secondNGrams = calculateNGrams(second, m);
            final int firstSize = firstNGrams.size();
            final int secondSize = secondNGrams.size();
            firstNGrams.retainAll(secondNGrams);

            float result = 2.0f * firstNGrams.size() / (firstSize + secondSize);
            if (map2.size() < subMapSize) {
                map2.put(second, result);
            }

            return result;
        }
    }
}
