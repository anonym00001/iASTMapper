package cs.sysu.algorithm.matcher.measures.token;

import com.github.gumtreediff.tree.ITree;
import cs.sysu.algorithm.element.InnerStmtElement;
import cs.sysu.algorithm.element.ProgramElement;
import cs.sysu.algorithm.element.TokenElement;
import cs.sysu.algorithm.matcher.matchers.searchers.CandidateSetsAndMaps;
import cs.sysu.algorithm.matcher.measures.AbstractSimMeasure;
import cs.sysu.algorithm.matcher.measures.SimMeasure;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mechanism: tokens in identical multi-token structure are likely to be mapped.
 */
public class TokenStructureMeasure extends AbstractSimMeasure implements SimMeasure {

    private ITree srcMultiTokenNode = null;
    private ITree dstMultiTokenNode = null;

    @Override
    protected double calMeasureValue(ProgramElement srcEle, ProgramElement dstEle) {
        double val = 0;
        InnerStmtElement srcInnerStmtEle = ((TokenElement) srcEle).getNearestMultiTokenInnerStmtElement();
        InnerStmtElement dstInnerStmtEle = ((TokenElement) dstEle).getNearestMultiTokenInnerStmtElement();
        // If the inner-stmt element is null, no need to calculate the measure again
        if (srcInnerStmtEle.isNullElement() || dstInnerStmtEle.isNullElement())
            return val;

//        System.out.println("Src " + srcEle + "  dst " + dstEle);
//        System.out.println("inner " + srcInnerStmtEle + "  " + dstInnerStmtEle);
        if (!withIdenticalMultiTokenElement(srcInnerStmtEle, dstInnerStmtEle, srcEle, dstEle))
            return val;

        List<InnerStmtElement> srcMultiTokenElements = ((TokenElement) srcEle).getMultiTokenElementsWithToken();
        List<InnerStmtElement> dstMultiTokenElements = ((TokenElement) dstEle).getMultiTokenElementsWithToken();

//        System.out.println("inner " + srcInnerStmtEle + " " + srcMultiTokenElements.size() + " " + dstInnerStmtEle + "  " + dstMultiTokenElements.size());

        for (int i = 0; i < srcMultiTokenElements.size() && i < dstMultiTokenElements.size(); i++) {
            InnerStmtElement srcInnerStmtEle1 = srcMultiTokenElements.get(i);
            InnerStmtElement dstInnerStmtEle1 = dstMultiTokenElements.get(i);
//            System.out.println("The inner stmt is " + srcInnerStmtEle1 + " " + dstInnerStmtEle1);
            if (withIdenticalMultiTokenElement(srcInnerStmtEle1, dstInnerStmtEle1, srcEle, dstEle)) {
                val = srcInnerStmtEle1.getTokenElements().size();
                srcMultiTokenNode = srcInnerStmtEle1.getITreeNode();
                dstMultiTokenNode = dstInnerStmtEle1.getITreeNode();
            } else
                break;
        }
//        System.out.println("The value is " + srcEle + " " + dstEle + "  " +  val);
        return val;
    }

    private boolean withIdenticalMultiTokenElement(InnerStmtElement srcInnerStmtEle, InnerStmtElement dstInnerStmtEle,
                                                   ProgramElement srcEle, ProgramElement dstEle) {
        // If type of the nearest multi-token inner-stmt element is not identical, no need to calculate the measure again
//        System.out.println("Src Inner type " + srcInnerStmtEle.getNodeType() + "  " + dstInnerStmtEle.getNodeType());
        if (!srcInnerStmtEle.getNodeType().equals(dstInnerStmtEle.getNodeType()))
            return false;

        // If token size of the nearest multi-token inner-stmt element is not equal, no need to calculate the measure again
//        System.out.println("Src token size " + srcInnerStmtEle.getTokenElements().size() + "  " + dstInnerStmtEle.getTokenElements().size());
        if (srcInnerStmtEle.getTokenElements().size() != dstInnerStmtEle.getTokenElements().size())
            return false;

//         If token positions in the nearest multi-token inner-stmt element are different, no need to calculate the measure again
        int startOfSrcTokens = srcInnerStmtEle.getTokenElements().get(0).getChildIdx();
        int startOfDstTokens = dstInnerStmtEle.getTokenElements().get(0).getChildIdx();
//        System.out.println("idx is " + srcEle.getChildIdx() + " " + startOfSrcTokens + " " + dstEle.getChildIdx() + " " + startOfDstTokens);
        if (srcEle.getChildIdx() - startOfSrcTokens != dstEle.getChildIdx() - startOfDstTokens)
            return false;

        // If multi-token inner-stmt elements are identical, no need to calculate the measure again
        String srcCompStr = srcInnerStmtEle.getStringValue();
        String dstCompStr = dstInnerStmtEle.getStringValue();
//        System.out.println("The value is " + srcCompStr + " | " + dstCompStr);
        if (!srcCompStr.equals(dstCompStr))
            return false;

        return true;
    }

    public ITree getSrcMultiTokenNode() {
        return srcMultiTokenNode;
    }

    public ITree getDstMultiTokenNode() {
        return dstMultiTokenNode;
    }

    @Override
    public Set<ProgramElement> filterBadDstCandidateElements(ProgramElement srcEle, Set<ProgramElement> dstCandidates,
                                                             CandidateSetsAndMaps candidateSetsAndMaps) {
        if (!srcEle.isFromSrc())
            return null;

        TokenElement srcToken = (TokenElement) srcEle;
        Set<ProgramElement> ret = new HashSet<>();
        List<InnerStmtElement> elements = srcToken.getInnerStmtElementsWithToken();
        for (int i = elements.size() - 1; i >= 0; i--) {
            InnerStmtElement element = elements.get(i);
            String typeWithValue = element.getNodeType() + ":" + element.getStringValue();
//            System.out.println(i + " " + srcToken + " " + element + " | " + typeWithValue + " || " + ret);
            Set<ProgramElement> multiTokenElements = candidateSetsAndMaps.getSameValDstMultiTokenElements(typeWithValue);
            if (multiTokenElements.size() == 0)
                continue;
            int tokenIndexInElement = srcToken.getChildIdx() - element.getTokenElements().get(0).getChildIdx();
            for (ProgramElement multiTokenEle : multiTokenElements) {
                ret.add(multiTokenEle.getTokenElements().get(tokenIndexInElement));
            }
            break;
        }
        return ret;
    }
}
