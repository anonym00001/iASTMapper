package cs.model.algorithm.matcher.rules;

import cs.model.algorithm.languageutils.typechecker.JavaNodeTypeChecker;
import cs.model.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;


/**
 * Interface of element mapping rule
 *
 * Each rule determines if two elements satisfy a specific condition
 * so that they can be mapped.
 */
public interface ElementMatchRule {

    // type checker
    JavaNodeTypeChecker typeChecker = StaticNodeTypeChecker.getConfigNodeTypeChecker();

    // threshold for ngram between two statements
    double stmtNgramThreshold = 0.6;

    // threshold for the ratio of common descendant statements.
    double stmtDMThreshold = 0.5;

    // threshold for the common tokens in inner-stmt elements.
    double InnerStmtEleDiceThreshold = 0.5;

    // at least 50% of the tokens appearing in one of the statement have identical tokens in another statement
    double stmtDiceThreshold0 = 0.7;

    // at least 50% of the tokens appearing in one of the statement have identical tokens in another statement
    double stmtDiceThreshold1 = 0.5;

    // at least 80% of the tokens appearing in one of the statement have identical tokens in another statement
    double stmtDiceThreshold2 = 0.8;

    // Token Ngram measure
    double tokenNgramThreshold = 0.8; // only very similar token can be mapped.


    /**
     * Determine if the two elements related to given measures can be mapped.
     * @param measures the given measures
     * @param eleMappings current element mappings
     * @return whether the two elements can be mapped
     */
    boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings);

}
