package cs.sysu.algorithm.ttmap;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
import cs.sysu.algorithm.utils.BinarySearch;

import java.util.*;

/**
 * A mapping between ITree object and its position.
 */
public class TreePositionIndex {
    private List<ITree> postOrderTrees;
    private List<ITree> postOrderRawTrees;
    private List<Integer> nodeStartPositions;
    private List<Integer> rawNodeStartPositions;

    private Map<Integer, List<ITree>> posTreesMap;      // map of start positions and ITree nodes
    private Map<Integer, List<ITree>> posRawTreesMap;   // map of start positions and raw ITree nodes
    private final ITree rawTreeRoot;         // root of the raw ITree
    private Set<Integer> rawTreePosSet;      // start positions of ITree objects
    private Set<Integer> rawTreeEndPosSet;   // end positions of ITree objects

    public TreePositionIndex(ITree root, ITree rawTreeRoot){
        this.rawTreeRoot = rawTreeRoot;
        postOrderTrees = TreeUtils.postOrder(root);
        postOrderRawTrees = TreeUtils.postOrder(rawTreeRoot);
        initTrees();
    }

    private void initTrees(){
        Set<Integer> posSet = new HashSet<>();
        Set<Integer> rawPosSet = new HashSet<>();
        rawTreePosSet = new HashSet<>();
        rawTreeEndPosSet = new HashSet<>();
        posTreesMap = new HashMap<>();
        posRawTreesMap = new HashMap<>();

        for (ITree t: postOrderTrees){
            posSet.add(t.getPos());
            if (!posTreesMap.containsKey(t.getPos()))
                posTreesMap.put(t.getPos(), new ArrayList<>());
            posTreesMap.get(t.getPos()).add(t);
        }

        for (ITree t: postOrderRawTrees){
            rawPosSet.add(t.getPos());
            if (!posRawTreesMap.containsKey(t.getPos()))
                posRawTreesMap.put(t.getPos(), new ArrayList<>());
            posRawTreesMap.get(t.getPos()).add(t);
        }

        nodeStartPositions = new ArrayList<>(posSet);
        Collections.sort(nodeStartPositions);

        rawNodeStartPositions = new ArrayList<>(rawPosSet);
        Collections.sort(rawNodeStartPositions);

        for (ITree t: rawTreeRoot.preOrder()){
            rawTreePosSet.add(t.getPos());
            rawTreeEndPosSet.add(t.getEndPos());
//            System.out.println("The T is " + t + " T getPos : " + t.getPos() + " T getEndPos: " + t.getEndPos());
        }
    }

    /**
     * Whether a given character position is the start position of an ITree node.
     */
    public boolean isStartPosOfNode(int pos) {
        return rawTreePosSet.contains(pos);
    }

    /**
     * Whether a given character position is the end position of an ITree node.
     */
    public boolean isEndPosOfNode(int endPos){
        return rawTreeEndPosSet.contains(endPos);
    }

    /**
     * Find the direct relevant node of the token in the raw tree.
     */
    public ITree findRawTreeOfToken(TokenRange range){
        return findITreeOfToken(range, posRawTreesMap, rawNodeStartPositions);
    }

    /**
     * Find the direct relevant node of the token in the current used tree.
     */
    public ITree findITreeOfToken(TokenRange range){
        return findITreeOfToken(range, posTreesMap, nodeStartPositions);
    }

    private static ITree findITreeOfToken(TokenRange range, Map<Integer, List<ITree>> posTreesMap, List<Integer> nodeStartPositions){
        int pos = range.first;
        if (posTreesMap.containsKey(pos)){
            ITree t = posTreesMap.get(pos).get(0);
//            System.out.println("ITree pos " + posTreesMap.get(pos) + " ITree " + t);
            if (t.getEndPos() < range.second)
                throw new RuntimeException("Find Tree of Word Error!");
            return t;
        }
        int index = BinarySearch.indexOfNumberSmallerOrEqual(nodeStartPositions, pos);
        while (index >= 0){
            int curPos = nodeStartPositions.get(index);
            List<ITree> treeList = posTreesMap.get(curPos);
            for (ITree t: treeList){
                int endPos = t.getEndPos();
                if (endPos >= range.second)
                    return t;
            }
            index --;
        }
        return null;
    }
}
