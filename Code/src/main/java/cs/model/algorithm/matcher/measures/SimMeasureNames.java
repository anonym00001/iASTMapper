package cs.model.algorithm.matcher.measures;

/**
 * To use a SimMeasure, it must have a measure name.
 * Then, add measure instance creation in ElementSimMeasures.java
 */
public class SimMeasureNames {
    // element similarity measure
    public final static String E_TYPE = "E_TYPE";
    public final static String ANCESTOR = "E_ANCESTOR";

    // statement similarity measure
    public final static String SAME_STMT = "SAME_STMT";
    public final static String SAME_METHOD_BODY = "SAME_METHOD_BODY";
    public final static String PM = "PM";
    public final static String NAME = "NAME";
    public final static String NIT = "NIT";
    public final static String JAVADOC = "JAVADOC";
    public final static String EXCHANGE = "EXC";
    public final static String STMT_SANDWICH = "STMT_SANDWICH";
    public final static String MS = "MS";
    public final static String DICE = "DICE";
    public final static String DM = "DM";
    public final static String NGRAM = "NGRAM";
    public final static String RETURN_STMT = "RETURN";

    // token similarity measure
    public final static String TYPE = "T_TYPE";
    public final static String STMT = "STMT";
    public final static String SAME_VALUE_RENAME = "SAME_VALUE_OR_RENAME";
    public final static String TOKEN_NEIGHBOR = "TOKEN_NEIGHBOR";
    public final static String STRUCT = "SAME_STRUCT";
    public final static String TOKEN_SANDWICH = "TOKEN_SANDWICH";
    public final static String TOKEN_SCOPE = "TOKEN_SCOPE";
    public final static String TOKEN_NGRAM = "TOKEN_NGRAM";
    public final static String TOKEN_RENAME = "TOKEN_RENAME";
    public final static String STMT_NAME_TOKEN = "STMT_NAME_TOKEN";
    public final static String TOKEN_LRB = "TOKEN_LRB";
    public final static String INNERSTMT = "INNERSTMT";

    // similarity measures for inner-stmt element
    public final static String INNER_STMT_ELE_NAME = "INNER_STMT_ELE_NAME";
    public final static String INNER_STMT_ELE_DICE = "INNER_STMT_ELE_DICE";
    public final static String INNER_STMT_ELE_SANDWICH = "INNER_STMT_ELE_SANDWICH";
    public final static String INNER_STMT_SAME_STMT = "INNER_STMT_SAME_STMT";
    public final static String METHOD_INVOCATION_SAME_NAME = "METHOD_INVOCATION_SAME_NAME";
}
