package cs.model.algorithm.matcher.measures;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.languageutils.typechecker.JavaNodeTypeChecker;
import cs.model.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.matchers.searchers.CandidateSetsAndMaps;

import java.util.Set;


/**
 * Interface of similarity measure between two elements.
 *
 * How to add new similarity measure:
 * 1. Add a measure class, extend AbstractSimMeasure and implement SimMeasure
 * 2. Add a measure name for the class in SimMeasureNames.java
 * 3. Add measure instance creation to calSimMeasure method in ElementSimMeasures.java
 * 4. Add the measure name to SimMeasureConfiguration.
 *    If the measure is used for statement,
 */
public interface SimMeasure {

    // consider only name and literal when counting identical tokens in a statement
    boolean onlyNameAndLiteral = true;

    // ngram size when calculating similarity of strings and tokens.
    int ngramSize = 2;

    // type checker
    JavaNodeTypeChecker typeChecker = StaticNodeTypeChecker.getConfigNodeTypeChecker();

    /**
     * Calculate similarity measures before mappings are calculated
     **/
    void calSimMeasure(ProgramElement srcEle, ProgramElement dstEle);

    /**
     * Filter the bad candidate target elements
     * @param srcEle source element
     * @param dstCandidates target candidate elements
     * @param candidateSetsAndMaps cache sets and maps to help us calculate the candidates
     * @return the good candidates or empty list if no good candidates are found.
     */
    Set<ProgramElement> filterBadDstCandidateElements(ProgramElement srcEle, Set<ProgramElement> dstCandidates,
                                                      CandidateSetsAndMaps candidateSetsAndMaps);

    /**
     * Compare value of similarity
     */
    int compare(SimMeasure measure);

    /**
     * Set similarity value
     */
    void setValue(double value);

    /**
     * Get similarity value
     */
    double getValue();

    /**
     * Element mappings for calculate PM, EXCHANGE, STMT measure
     */
    void setElementMappings(ElementMappings elementMappings);
}
