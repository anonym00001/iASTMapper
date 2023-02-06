package cs.model.algorithm.matcher.rules;

import cs.model.algorithm.element.ProgramElement;

/**
 * Configuration of mapping rules for statements, tokens and inner-stmt elements.
 */
public class MatchRulesConfiguration {
    // match rule configuration for statement mapping
    private static final String[] STMT_RULE_CONFIGURATION = {
            MatchRuleNames.SAME_STMT,          // same statement
            MatchRuleNames.SAME_METHOD_BODY,   // methods with the same body
            MatchRuleNames.STMT_NAME,    // same name and same scope
            MatchRuleNames.BLOCK,              // block with parent element mapped
            MatchRuleNames.RETURN,             // return statement in the same method
            MatchRuleNames.STMT_SANDWICH,      // statements with surround statements mapped
            MatchRuleNames.DESCENDANT_STMT,    // statements with enough descendant statements mapped
            MatchRuleNames.STMT_TOKEN_DICE,    // statements with similar content
    };

    // match rule configuration for token mapping
    private static final String[] TOKEN_RULE_CONFIGURATION = {
            MatchRuleNames.TOKEN_SAME_STRUCT,   // tokens in same multi-token structure
            MatchRuleNames.TOKEN_SAME_STMT,     // tokens in the same statement with same or renamed value
            MatchRuleNames.TOKEN_SANDWICH,      // tokens with the sandwich measure
            MatchRuleNames.TOKEN_MOVE,          // move with neighbor token; same variable or literal moved intra same scope
            MatchRuleNames.NAME_TOKEN_STMT,     // name of the statement can be mapped
    };

    // configuration for inner-stmt element mapping
    private static final String[] INNER_STMT_ELE_RULE_CONFIGURATION = {
            MatchRuleNames.INNER_STMT_ELE_NAME,           // inner-stmt elements with mapped token representing their name.
            MatchRuleNames.INNER_STMT_ELE_DICE,           // inner-stmt elements with enough common tokens.
            MatchRuleNames.INNER_STMT_ELE_SANDWICH,       // inner-stmt elements with left and right tokens mapped.
            MatchRuleNames.ANONYMOUS_DEC
    };

    /**
     * Find the rule configuration for a given element
     * @param element the given statement, token or inner-stmt element.
     * @return the rule set.
     */
    public static String[] getRuleConfiguration(ProgramElement element) {
        if (element.isStmt())
            return MatchRulesConfiguration.STMT_RULE_CONFIGURATION;
        if (element.isToken())
            return MatchRulesConfiguration.TOKEN_RULE_CONFIGURATION;
        if (element.isInnerStmtElement())
            return MatchRulesConfiguration.INNER_STMT_ELE_RULE_CONFIGURATION;
        throw new RuntimeException("Unknown element type");
    }
}
