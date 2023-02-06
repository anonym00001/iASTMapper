package cs.model.algorithm.matcher.rules.token;

import cs.model.algorithm.matcher.mappings.ElementMappings;
import cs.model.algorithm.matcher.measures.ElementSimMeasures;
import cs.model.algorithm.matcher.measures.SimMeasure;
import cs.model.algorithm.matcher.measures.SimMeasureNames;
import cs.model.algorithm.matcher.rules.AbstractElementMatchRule;
import cs.model.algorithm.matcher.rules.ElementMatchRule;

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
