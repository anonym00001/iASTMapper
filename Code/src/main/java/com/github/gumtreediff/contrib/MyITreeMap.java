package com.github.gumtreediff.contrib;

import com.github.gumtreediff.tree.ITree;

import java.util.HashMap;
import java.util.Map;

public class MyITreeMap {
    private Map<Integer, ITree> idTreeMap;
    private Map<ITree, Integer> treeIdMap;

    public MyITreeMap(){
        idTreeMap = new HashMap<>();
        treeIdMap = new HashMap<>();
    }

    public void addTree(ITree t, int index){
        idTreeMap.put(index, t);
        treeIdMap.put(t, index);
    }

    public int getIdOfTree(ITree t){
        return treeIdMap.get(t);
    }

    public ITree getITreeById(int id){
        return idTreeMap.get(id);
    }
}
