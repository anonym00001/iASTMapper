package cs.model.algorithm.matcher.rules;

/**
 * Names of mapping rules.
 *
 * Adding a new rule must add the rule name to this class.
 */
public class MatchRuleNames {
    // Stmt Rules
    public static final String SAME_STMT = "SameStmt";
    public static final String SAME_METHOD_BODY = "SameMethodBody";
    public static final String STMT_NAME = "StmtSameName";
    public static final String BLOCK = "Block";
    public static final String RETURN = "ReturnStmt";
    public static final String STMT_NGRAM_DICE = "StmtNgramDice";
    public static final String STMT_SANDWICH = "StmtSandwich";
    public static final String DESCENDANT_STMT = "StmtDescendantMapped";
    public static final String STMT_TOKEN_DICE = "StmtTokenDice";

    // Token Rules
    public static final String TOKEN_SAME_STRUCT = "TokenSameStructure";
    public static final String TOKEN_SAME_STMT = "TokenSameStmt";
    public static final String TOKEN_SANDWICH = "TokenSandwich";
    public static final String TOKEN_MOVE = "TokenMove";
    public static final String NAME_TOKEN_STMT = "NameTokeOfStmt";
    public static final String TOKEN_LRB = "TOKEN_LRB";

    // Rules for inner-stmt elements
    public static final String INNER_STMT_ELE_NAME = "InnerStmtEleCommonName";
    public static final String INNER_STMT_ELE_DICE = "InnerStmtEleTokenDice";
    public static final String INNER_STMT_ELE_SANDWICH = "InnerStmtEleSandwich";
    public static final String METHOD_INVOCATION_SAME_NAME = "MethodInvocationSameName";
    public static final String ANONYMOUS_DEC = "AnonymousDec";
}
