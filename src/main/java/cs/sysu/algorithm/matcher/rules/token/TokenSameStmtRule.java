package cs.sysu.algorithm.matcher.rules.token;

import cs.sysu.algorithm.matcher.mappings.ElementMappings;
import cs.sysu.algorithm.matcher.measures.ElementSimMeasures;
import cs.sysu.algorithm.matcher.measures.SimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasureNames;
import cs.sysu.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.sysu.algorithm.matcher.rules.ElementMatchRule;

/**
 * Mapping rule for token
 *
 * Tokens in mapped statements are likely to be mapped.
 */
public class TokenSameStmtRule extends AbstractElementMatchRule implements ElementMatchRule {
    public  TokenSameStmtRule(){
        super();
    }
    @Override
    public boolean determineCanBeMapped(ElementSimMeasures measures, ElementMappings eleMappings) {
        SimMeasure measure1 = measures.getSimMeasure(SimMeasureNames.STMT, eleMappings);
        if (measure1.getValue() == 1.0) {
            // if two tokens have the same value or the two tokens are renamed
            SimMeasure measure2 = measures.getSimMeasure(SimMeasureNames.SAME_VALUE_RENAME, eleMappings);
            if (measure2.getValue() == 1.0)
                return true;
        }
        return false;
    }
}
