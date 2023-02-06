/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with GumTree. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2015-2016 Georg Dotzler <georg.dotzler@fau.de>
 * Copyright 2015-2016 Marius Kamp <marius.kamp@fau.de>
*/

package com.github.gumtreediff.matchers.heuristic.mtdiff.intern;

import com.github.gumtreediff.tree.ITree;

import java.util.*;

public class PostOrderSetGenerator {
    private IdentityHashMap<ITree, ArrayList<ITree>> directChildren;
    private Set<ITree> leafSet;

    private IdentityHashMap<ITree, ArrayList<ITree>> leaves;
    private Set<ITree> nodeSet;

    private LinkedList<ITree> parentNodeStack;

    private Stack<Integer> sizeStack = new Stack<>();

    /**
     * Generates sets for a given root node.
     *
     * @param root the root
     */
    public void createSetsForNode(final ITree root) {
        nodeSet = new LinkedHashSet<ITree>();
        leafSet = new LinkedHashSet<ITree>();
        leaves = new IdentityHashMap<>();
        directChildren = new IdentityHashMap<>();
        parentNodeStack = new LinkedList<>();
        visit(root);
    }

    /**
     * Return a map of node to direct children by this visitor.
     *
     * @return the direct children map
     */
    public IdentityHashMap<ITree, ArrayList<ITree>> getDirectChildrenMap() {
        return directChildren;
    }

    /**
     * Return a map of node to leaves by this visitor.
     *
     * @return the leave map
     */
    public IdentityHashMap<ITree, ArrayList<ITree>> getLeaveMap() {
        return leaves;
    }

    /**
     * Return a list of all leaves visited by this visitor.
     *
     * @return the sets the of leaves
     */
    public Set<ITree> getSetOfLeaves() {
        return leafSet;
    }

    /**
     * Return a list of all nodes visited by this visitor.
     *
     * @return the sets the of nodes
     */
    public Set<ITree> getSetOfNodes() {
        return nodeSet;
    }

    private void visit(ITree node) {
        sizeStack.push(nodeSet.size());
        ArrayList<ITree> tmp = new ArrayList<>();
        leaves.put(node, tmp);
        parentNodeStack.push(node);
        tmp = new ArrayList<>();
        directChildren.put(node, tmp);
        tmp.addAll(node.getChildren());
        for (ITree child : node.getChildren()) {
            visit(child);
        }
        int size = sizeStack.pop();
        if (size == nodeSet.size()) {
            leafSet.add(node);
            for (ITree parentNode : parentNodeStack) {
                if (parentNode != node) {
                    ArrayList<ITree> list = leaves.get(parentNode);
                    assert (list != null);
                    list.add(node);
                }
            }
        }
        nodeSet.add(node);
        parentNodeStack.pop();
    }
}
