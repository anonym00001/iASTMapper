package at.algorithm.softwaredynamics.util;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.ITree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class IJMTreeUtil {
    public static void removeMatched(ITree copyRoot, List<ITree> originTreesInPostOrder,
                                     Map<ITree, Integer> treePosMap,
                                     MappingStore mappings, boolean isSrc){
        List<ITree> treesToRemove = new ArrayList<>();
        int pos = 0;
        for (ITree t: copyRoot.postOrder()){
            treePosMap.put(t, pos);
            ITree originTree = originTreesInPostOrder.get(pos);
            if (isSrc && mappings.isSrcMapped(originTree))
                treesToRemove.add(t);
            if (!isSrc && mappings.isDstMapped(originTree))
                treesToRemove.add(t);
            pos ++;
        }

        for (ITree t : treesToRemove) {
            if (t.getParent() != null)
                t.getParent().getChildren().remove(t);
            t.setParent((ITree) null);
        }
        copyRoot.setMetrics(null);
    }

    public static void calTreePosMap(ITree copyRoot, Map<ITree, Integer> treePosMap){
        int pos = 0;
        for (ITree t: copyRoot.postOrder()){
            treePosMap.put(t, pos);
            pos++;
        }
    }
}
