package cs.sysu.algorithm.element;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculate a set of n-grams given a string
 */
public class NGramCalculator {
    private ConcurrentHashMap<String, Set<String>> tokenGramMap;

    /**
     * Instantiates a new ngram calculator.
     */
    public NGramCalculator() {
        tokenGramMap = new ConcurrentHashMap<>(10000);
    }

    /**
     * Calculate n-grams given a string value, and transform them to lowercase if needed.
     * @param string a given string
     * @param npar the n-gram size
     * @param toLowerCase if lowercase is needed.
     * @return a set of n-grams
     */
    public Set<String> calculateNGrams(final String string, final int npar, boolean toLowerCase) {
        if (tokenGramMap.containsKey(string)) {
            return tokenGramMap.get(string);
        }

        if (string.length() <= npar){
            Set<String> ret = new HashSet<>();
            if (toLowerCase)
                ret.add(string.toLowerCase());
            else
                ret.add(string);
            return ret;
        }

        final Set<String> result = new HashSet<String>();
        for (int i = 0; i < string.length() - npar + 1; ++i) {
            String substring = string.substring(i, i + npar);
            if (toLowerCase)
                result.add(substring.toLowerCase());
            else
                result.add(substring);
        }
        tokenGramMap.put(string, result);
        return result;
    }

    /**
     * Clear the cache.
     */
    public void clear() {
        tokenGramMap.clear();
    }
}
