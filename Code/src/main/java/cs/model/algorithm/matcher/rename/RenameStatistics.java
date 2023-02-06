package cs.model.algorithm.matcher.rename;

import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.mappings.ElementMappings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class RenameStatistics {
    private Map<String, Map<String, Integer>> overallRenameStatistics;

    public RenameStatistics() {
        overallRenameStatistics = new HashMap<>();
    }

    public void addRenameByNewTokenMapping(TokenElement srcTokenEle, TokenElement dstTokenEle) {
        String srcTokenStr = srcTokenEle.getStringValue();
        String dstTokenStr = dstTokenEle.getStringValue();
        if (srcTokenStr.equals(dstTokenStr))
            return;
//        System.out.println("isName ? " + srcTokenEle.isName());
        if (!srcTokenEle.isName())
            return;

        if (!overallRenameStatistics.containsKey(srcTokenStr))
            overallRenameStatistics.put(srcTokenStr, new HashMap<>());
        if (!overallRenameStatistics.get(srcTokenStr).containsKey(dstTokenStr))
            overallRenameStatistics.get(srcTokenStr).put(dstTokenStr, 0);
        int num = overallRenameStatistics.get(srcTokenStr).get(dstTokenStr);
        overallRenameStatistics.get(srcTokenStr).put(dstTokenStr, num + 1);
//        System.out.println("value is " + srcTokenStr + " " + dstTokenStr + " " + overallRenameStatistics.get(srcTokenStr));
    }

    public void removeRenameByTokenMapping(TokenElement srcTokenEle, TokenElement dstTokenEle) {
        String srcTokenStr = srcTokenEle.getStringValue();
        String dstTokenStr = dstTokenEle.getStringValue();
        if (srcTokenStr.equals(dstTokenStr))
            return;

        if (overallRenameStatistics.containsKey(srcTokenStr)) {
            if (overallRenameStatistics.get(srcTokenStr).containsKey(dstTokenStr)) {
                int tmp = overallRenameStatistics.get(srcTokenStr).get(dstTokenStr);
                if (tmp == 1)
                    overallRenameStatistics.get(srcTokenStr).remove(dstTokenStr);
                else if (tmp > 1)
                    overallRenameStatistics.get(srcTokenStr).put(dstTokenStr, tmp - 1);
            }
        }
    }

    public Set<String> getDstNameForSrcName(String srcName) {
        if (!overallRenameStatistics.containsKey(srcName))
            return null;
        return new HashSet<>(overallRenameStatistics.get(srcName).keySet());
    }

    public Set<String> getDstNameForSrcNameWithoutCurTokenPairs(String srcName, ElementMappings currentMappings,
                                                                TokenElement srcToken, TokenElement dstToken) {

        if (!overallRenameStatistics.containsKey(srcName))
            return null;
        // debug repair the bug that needn't consider tokens that is non-name token
        if (!srcToken.isName())
            return null;
        Map<String, Integer> dstNameMap = new HashMap<>(overallRenameStatistics.get(srcName));

//        System.out.println("Src Token is " + srcToken + "| " + srcToken.getTokenType() + " Dst " + dstToken + "| " + dstToken.getTokenType() + " " + dstNameMap);
        if (currentMappings.isMapped(srcToken)) {
            ProgramElement tmpDstToken = currentMappings.getMappedElement(srcToken);
            if (!srcToken.equalValue(tmpDstToken)) {
                String tmpDstName = tmpDstToken.getStringValue();
                dstNameMap.put(tmpDstName, dstNameMap.get(tmpDstName) - 1);
            }
        }
        if (currentMappings.isMapped(dstToken)) {
            ProgramElement tmpSrcToken = currentMappings.getMappedElement(dstToken);
//            System.out.println("Src Token is " + tmpSrcToken);
            if (tmpSrcToken.equalValue(srcToken) && tmpSrcToken != srcToken) {
                String dstName = dstToken.getStringValue();
                dstNameMap.put(dstName, dstNameMap.get(dstName) - 1);
            }
        }

        Set<String> ret = new HashSet<>();
        for (String name: dstNameMap.keySet()) {
            if (dstNameMap.get(name) > 0)
                ret.add(name);
        }
        return ret;
    }
}
