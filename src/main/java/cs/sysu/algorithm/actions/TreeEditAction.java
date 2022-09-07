package cs.sysu.algorithm.actions;

import com.github.gumtreediff.actions.model.*;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;
import cs.sysu.algorithm.languageutils.typechecker.JavaNodeTypeChecker;
import cs.sysu.algorithm.languageutils.typechecker.StaticNodeTypeChecker;
import cs.sysu.algorithm.utils.RangeCalculator;
import cs.sysu.utils.Pair;

/**
 * Edit actions on ITree nodes.
 */
public class TreeEditAction {
    private TreeEditType editType;
    private ITree srcNode = null;
    private ITree dstNode = null;
    private String srcContent = null;
    private String dstContent = null;

    private Pair<Integer, Integer> srcLineRange = null;
    private Pair<Integer, Integer> dstLineRange = null;

    private static JavaNodeTypeChecker jdtChecker = StaticNodeTypeChecker.getConfigNodeTypeChecker();

    /**
     * Constructor
     * @param action an action object defined in GumTree
     * @param ms mappings defined in GumTree
     * @param srcRc range calculator for the source file
     * @param dstRc range calculator for the target file
     */
    public TreeEditAction(Action action, MappingStore ms,
                          RangeCalculator srcRc, RangeCalculator dstRc){
        extractActionInfo(ms, action);
        calCodeRange(srcRc, dstRc);

        if (srcNode != null)
            srcContent = srcRc.getFileContent().substring(srcNode.getPos(), srcNode.getEndPos());
        if (dstNode != null)
            dstContent = dstRc.getFileContent().substring(dstNode.getPos(), dstNode.getEndPos());
    }

    public boolean isJavadocRelated() {
        ITree node = srcNode != null ? srcNode : dstNode;
        while (node != null) {
            if (jdtChecker.isJavaDoc(node))
                return true;
            node = node.getParent();
        }
        return false;
    }

    private void calCodeRange(RangeCalculator srcRc, RangeCalculator dstRc){
        srcLineRange = srcRc.getLineRangeOfNode(srcNode);
        dstLineRange = dstRc.getLineRangeOfNode(dstNode);
    }

    private void extractActionInfo(MappingStore ms, Action action){
        if (action instanceof Insert){
            editType = TreeEditType.INSERT;
            dstNode = action.getNode();
            srcNode = null;
        } else if (action instanceof Delete){
            editType = TreeEditType.DELETE;
            srcNode = action.getNode();
            dstNode = null;
        } else if (action instanceof Move){
            editType = TreeEditType.MOVE_TREE;
            srcNode = action.getNode();
            dstNode = ms.getDstForSrc(srcNode);
        } else {
            if (!(action instanceof Update))
                throw new RuntimeException("Unknown edit operation!");
            editType = TreeEditType.UPDATE;
            srcNode = action.getNode();
            dstNode = ms.getDstForSrc(srcNode);
        }
    }

    public String toString() {
        String ret = "" + editType + "\t";
        if (srcNode != null){
            ret += "[type: " + JavaNodeTypeChecker.getITreeNodeTypeName(srcNode) + "] ";
            ret += "(line: -" + srcLineRange.first + ")";
        }
        if (dstNode != null){
            if (srcNode != null)
                ret += " => ";
            ret += "[type: " + JavaNodeTypeChecker.getITreeNodeTypeName(dstNode) + "] ";
            ret += "(line: +" + dstLineRange.first + ")";
        }
        ret += "\t";
        if (srcContent != null) {
            ret += "SRC: " + srcContent.split("\n")[0];
        }
        if (dstContent != null) {
            if (srcContent != null)
                ret += " => ";
            ret += "DST: " + dstContent.split("\n")[0];
        }
        ret += "\n";
        return ret;
    }
}
