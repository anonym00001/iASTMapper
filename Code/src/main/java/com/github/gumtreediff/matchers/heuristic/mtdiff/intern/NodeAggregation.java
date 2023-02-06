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

import com.github.gumtreediff.tree.FakeTree;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.Type;

import java.util.LinkedList;
import java.util.List;

/**
 * A class representing a subtree, whose nodes have been linked together. It is used by
 * {@see CdOptimizedMatcher.tree.SifeMatcher}
 * for the implementation of the best-match strategy.
 */
class NodeAggregation extends FakeTree {
    @SuppressWarnings("ucd")
    public static final Type TAG = Type.NODE_AGGREGATION_TYPE;

    private int hash = Integer.MIN_VALUE;

    private ITree subTree;

    /**
     * Instantiates a new node aggregation.
     *
     * @param subTree the sub tree
     */
    public NodeAggregation(final ITree subTree) {
        setAssociatedTree(subTree);
    }

    /**
     * Gets the associated tree.
     *
     * @return the associated tree
     */
    public ITree getAssociatedTree() {
        return subTree;
    }


    /**
     * Gets the children.
     *
     * @return the children
     */
    @Override
    public List<ITree> getChildren() {
        return null;
    }


    /**
     * Gets the hash.
     *
     * @return the hash
     */
    public int getHash() {
        return hash;
    }


//    /**
//     * Gets the id.
//     *
//     * @return the id
//     */
//    @Override
//    public int getId() {
//        return -1;
//    }


    /**
     * Gets the label.
     *
     * @return the label
     */
    @Override
    public String getLabel() {
        return null;
    }

    /**
     * Gets the tag.
     *
     * @return the tag
     */
    public Type getTag() {
        return TAG;
    }


    /**
     * Gets the type.
     *
     * @return the type
     */
    @Override
    public Type getType() {
        return TAG;
    }


    /**
     * Checks if is leaf.
     *
     * @return true, if is leaf
     */
    @Override
    public boolean isLeaf() {
        return false;
    }

    /**
     * Sets the associated tree.
     *
     * @param subTree the new associated tree
     */
    public void setAssociatedTree(final ITree subTree) {

        if (subTree != null) {
            this.subTree = subTree;
            StringBuilder builder = new StringBuilder();
            LinkedList<ITree> workList = new LinkedList<>();
            workList.add(subTree);
            while (!workList.isEmpty()) {
                ITree node = workList.removeFirst();
                builder.append(node.getType() + node.getLabel());
                workList.addAll(node.getChildren());
            }
            hash = builder.toString().hashCode();
        } else {
            throw new NullPointerException("The associated tree must not be null");
        }
    }


    /**
     * To string.
     *
     * @return the string
     */
    @Override
    public String toString() {
        return subTree.toString();
    }
}
