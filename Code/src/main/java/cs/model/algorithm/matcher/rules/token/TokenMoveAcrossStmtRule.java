package cs.model.algorithm.matcher.rules.token;

import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;
import cs.model.algorithm.ttmap.TokenTypeCalculator;

/**
 * Mapping rule for token
 *
 * When move across different statement, the two tokens can be mapped if:
 * 1. it is not a special token, e.g., public, private
 * 2. it is variable and moves intra same scope
 * 3. it is literal and moves intra same scope
 * 4. it is moved with neighbor token
 */
public class TokenMoveAcrossStmtRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  TokenMoveAcrossStmtRule(){
        super();
    }

    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        TokenElement srcTokenEle = (TokenElement) measures.getSrcEle();
        TokenElement dstTokenEle = (TokenElement) measures.getDstEle();
        SimMeasure measure1 = measures.getSimMeasure(SimMeasureNames.STMT, eleMappings);
        if (measure1.getValue() == 0.0) {

            if (srcTokenEle.getStmtElement().isDeclaration() || dstTokenEle.getStmtElement().isDeclaration())
                return false;

            // special token case
            if (isSpecialToken(srcTokenEle))
                return false;

            String srcTokenType = srcTokenEle.getTokenType();
            SimMeasure measure2 = measures.getSimMeasure(SimMeasureNames.SAME_VALUE_RENAME, eleMappings);
            if (measure2.getValue() == 1.0)
                return checkReasonableMapCondition(srcTokenType, srcTokenEle, measures, eleMappings);
        }
        return false;
    }

    private boolean checkReasonableMapCondition(String srcTokenType, TokenElement srcTokenEle,
                                                ElementSimMeasures measures, ElementMappings eleMappings) {
        // variable move in the same scope
        if (srcTokenType.equals(TokenTypeCalculator.VAR_NAME))
            return moveIntraScope(measures, eleMappings);

        // method name move in the same scope
        if (srcTokenType.equals(TokenTypeCalculator.METHOD_NAME))
            return moveIntraScope(measures, eleMappings);

        // literal move in the same scope
        if (srcTokenEle.isLiteral())
            return moveIntraScope(measures, eleMappings);

        if (srcTokenEle.getStringValue().equals("="))
            return false;

        // surrounding token is also mapped.
        if (moveWithNeighborToken(measures, eleMappings))
            return true;
        return false;
    }

    // Some tokens are not likely to moved across different statements.
    // Including public, protected and private.
    private static boolean isSpecialToken(TokenElement srcElement) {
        String value = srcElement.getStringValue();
        return value.equals("protected") || value.equals("public") ||
                value.equals("private") || value.equals("static");
    }
}
