package cs.sysu.algorithm.matcher.measures.stmt.textual;

import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.StmtElement;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

import java.util.HashMap;
import java.util.Map;

/**
 * Mechanism: mapped statements should have similar content.
 */
public class StmtIdenticalTokenMeasure extends AbstractSimMeasure implements SimMeasure {

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        if (srcEle.equalValue(dstEle)){
            if (!onlyNameAndLiteral)
                return srcEle.getTokenElements().size();
            else
                return ((StmtElement) srcEle).getNameAndLiteralNum();
        }
        return calExtremeIdenticalTokenMappings((StmtElement) srcEle, (StmtElement) dstEle);
    }

    private double calExtremeIdenticalTokenMappings(StmtElement srcEle, StmtElement dstEle){
        Map<String, Integer> srcTokenNumMap = new HashMap<>(srcEle.getTokenNumMap(onlyNameAndLiteral));
        Map<String, Integer> dstTokenNumMap = new HashMap<>(dstEle.getTokenNumMap(onlyNameAndLiteral));

        double val = 0;
        for (String tokenValue: srcTokenNumMap.keySet()) {
            int num1 = srcTokenNumMap.get(tokenValue);
            if (!dstTokenNumMap.containsKey(tokenValue))
                continue;
            int num2 = dstTokenNumMap.get(tokenValue);
            int minVal = Integer.min(num1, num2);
            val += minVal;
            srcTokenNumMap.put(tokenValue, num1 - minVal);
            dstTokenNumMap.put(tokenValue, num2 - minVal);
        }

//        for (String tokenValue: srcTokenNumMap.keySet()) {
//            Set<String> updateNames = elementMappings.getRenameStatistics().getDstNameForSrcName(tokenValue);
//            int num1 = srcTokenNumMap.get(tokenValue);
//            if (num1 == 0)
//                continue;
//            if (updateNames == null)
//                continue;
//            if (updateNames.size() == 1) {
//                String updateName = updateNames.iterator().next();
//                int num2 = 0;
//                if (dstTokenNumMap.containsKey(updateName))
//                    num2 = dstTokenNumMap.get(updateName);
//                int minVal = Integer.min(num1, num2);
//                val += minVal;
//                srcTokenNumMap.put(tokenValue, num1 - minVal);
//                dstTokenNumMap.put(tokenValue, num2 - minVal);
//            }
//        }
        return val;
    }
}
