package cs.model.algorithm.matcher.mappings;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.model.algorithm.element.ElementTreeBuilder;
import cs.model.algorithm.element.ElementTreeUtils;
import cs.model.algorithm.element.ProgramElement;
import cs.model.algorithm.element.TokenElement;
import cs.model.algorithm.matcher.matchers.InnerStmtEleMatcher;
import cs.model.algorithm.matcher.matchers.searchers.BestMappingSearcher;
import cs.model.algorithm.ttmap.TokenRange;
import cs.model.algorithm.ttmap.TokenRangeTypeMap;
import cs.model.algorithm.ttmap.TreeTokensMap;

import java.util.*;

/**
 * Transformer between mappings of tree nodes and mappings of program elements.
 */
public class MappingTransformer {

    /**
     * Get element mappings based on mappings of ITree nodes
     *
     * @param ms mappings of ITree nodes
     * @param srcRoot root node of source ITree
     * @param dstRoot root node of target ITree
     * @param srcTrtMap token type map of source file
     * @param dstTrtMap token type map of target file
     * @param srcTtMap tree token map of source file
     * @param dstTtMap tree token map of target file
     * @return the generated element mappings
     */
    public static ElementMappings getElementMappingsFromTreeMappings(MappingStore ms, ITree srcRoot, ITree dstRoot,
                                                                     TokenRangeTypeMap srcTrtMap, TokenRangeTypeMap dstTrtMap,
                                                                     TreeTokensMap srcTtMap, TreeTokensMap dstTtMap){
        ElementMappings ret = new ElementMappings();
        ProgramElement srcRootEle = ElementTreeBuilder.buildElementTree(srcRoot, srcTtMap, srcTrtMap,true);
        ProgramElement dstRootEle = ElementTreeBuilder.buildElementTree(dstRoot, dstTtMap, dstTrtMap,false);
        ret.addMapping(srcRootEle, dstRootEle);
        List<ProgramElement> srcStmtElements = ElementTreeUtils.getAllStmtsPreOrder(srcRootEle);
        List<ProgramElement> dstStmtElements = ElementTreeUtils.getAllStmtsPreOrder(dstRootEle);
        Map<ITree, ProgramElement> dstNodeElementMap = new HashMap<>();
        Map<TokenRange, ProgramElement> dstRangeEleMap = new HashMap<>();
        for (ProgramElement element: dstStmtElements){
            ITree node = element.getITreeNode();
            dstNodeElementMap.put(node, element);

            List<TokenElement> tokenElements = element.getTokenElements();
            for (TokenElement tokenEle: tokenElements){
                dstRangeEleMap.put(tokenEle.getTokenRange(), tokenEle);
            }
        }
        for (ProgramElement element: srcStmtElements){
            ITree node = element.getITreeNode();
            if (ms.isSrcMapped(node)){
                ProgramElement dstEle = dstNodeElementMap.get(ms.getDstForSrc(node));
                ret.addMapping(element, dstEle);
            }

            List<TokenElement> tokenElements = element.getTokenElements();
            for (TokenElement tokenEle: tokenElements){
                TokenRange srcTokenRange = tokenEle.getTokenRange();
                TokenRange dstTokenRange = TreeTokensMap.findMappedRange(ms, srcTokenRange, true, srcTtMap, dstTtMap);
                if (dstTokenRange == null)
                    continue;
                if (!dstRangeEleMap.containsKey(dstTokenRange))
                    continue;
                ProgramElement dstTokenEle = dstRangeEleMap.get(dstTokenRange);
                ret.addMapping(tokenEle, dstTokenEle);
            }
        }
        return ret;
    }

    /**
     * Transform element mappings to mappings of tree nodes
     * @param originMappings element mappings
     * @param srcRootEle source root element
     * @param dstRootEle target root element
     * @return the output mappings of tree nodes
     */
    public static MappingStore elementMappingsToTreeMappings(ElementMappings originMappings,
                                                             ProgramElement srcRootEle, ProgramElement dstRootEle) {
        return originMappings.toMappingStore(srcRootEle.getITreeNode(), dstRootEle.getITreeNode());
    }
}
