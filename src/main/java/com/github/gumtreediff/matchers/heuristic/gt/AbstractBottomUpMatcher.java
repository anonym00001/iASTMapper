/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with GumTree.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2011-2016 Jean-Rémy Falleri <jr.falleri@gmail.com>
 * Copyright 2011-2016 Floréal Morandat <florealm@gmail.com>
 */

package com.github.gumtreediff.matchers.heuristic.gt;

import com.github.gumtreediff.contrib.MyITreeMap;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.optimal.zs.ZsMatcher;
import com.github.gumtreediff.tree.ITree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractBottomUpMatcher {
    public static int SIZE_THRESHOLD =
            Integer.parseInt(System.getProperty("gt.bum.szt", "1000"));
    public static double SIM_THRESHOLD =
            Double.parseDouble(System.getProperty("gt.bum.smt", "0.5"));

    protected abstract static class Implementation {
        protected final ITree src;
        protected final ITree dst;
        protected final MappingStore mappings;

        public Implementation(ITree src, ITree dst, MappingStore mappings) {
            this.src = src;
            this.dst = dst;
            this.mappings = mappings;
        }

        protected List<ITree> getDstCandidates(ITree src) {
            List<ITree> seeds = new ArrayList<>();
            for (ITree c : src.getDescendants()) {
                if (mappings.isSrcMapped(c))
                    seeds.add(mappings.getDstForSrc(c));
            }
            List<ITree> candidates = new ArrayList<>();
            Set<ITree> visited = new HashSet<>();
            for (ITree seed : seeds) {
                while (seed.getParent() != null) {
                    ITree parent = seed.getParent();
                    if (visited.contains(parent))
                        break;
                    visited.add(parent);
                    if (parent.getType() == src.getType() && !(mappings.isDstMapped(parent) || parent.isRoot()))
                        candidates.add(parent);
                    seed = parent;
                }
            }

            return candidates;
        }

//        protected void lastChanceMatch(ITree src, ITree dst) {
//
//            if (src.getMetrics().size < AbstractBottomUpMatcher.SIZE_THRESHOLD
//                    || dst.getMetrics().size < AbstractBottomUpMatcher.SIZE_THRESHOLD) {
//                Matcher m = new ZsMatcher();
//                MappingStore zsMappings = m.match(src, dst, new MappingStore(src, dst));
//                for (Mapping candidate : zsMappings) {
//                    ITree srcCand = candidate.first;
//                    ITree dstCand = candidate.second;
//                    if (mappings.isMappingAllowed(srcCand, dstCand))
//                        mappings.addMapping(srcCand, dstCand);
//                }
//            }
//        }

//        private Set<Integer> getCannotRemoveNodes(ITree node, boolean isSrc, MyITreeMap originTreeMap){
//            Set<ITree> ret = new HashSet<>();
//            ITree p = node.getParent();
//            node.setParent(null);
//            for (ITree t: node.postOrder()){
//                if (isSrc && !mappings.isSrcMapped(t))
//                    ret.addAll(t.getParents());
//                else if (!isSrc && !mappings.isDstMapped(t))
//                    ret.addAll(t.getParents());
//            }
//
//            Set<Integer> ret2 = new HashSet<>();
//            for (ITree t: ret){
//                if (t == null)
//                    continue;
//                ret2.add(originTreeMap.getIdOfTree(t));
//            }
//            node.setParent(p);
//            return ret2;
//        }

        private void getMappedTreeIndexInPreOrder(ITree node, boolean isSrc,
                                                  Set<Integer> mappedTreeIndexes,
                                                  MyITreeMap treeMap){
            int index  = 0;
            for (ITree t: node.preOrder()){
                treeMap.addTree(t, index);
                if ((isSrc && mappings.isSrcMapped(t)) || (!isSrc && mappings.isDstMapped(t)))
                    mappedTreeIndexes.add(index);
                index ++;
            }
        }

        private void removeMatched(ITree copyNode, Set<Integer> mappedTreeIndexes, MyITreeMap treeMap){
            int index = 0;
            List<ITree> treesToRemove = new ArrayList<>();
            for (ITree t: copyNode.preOrder()){
                treeMap.addTree(t, index);
                if (mappedTreeIndexes.contains(index))
                    treesToRemove.add(t);
                index ++;
            }
            for (ITree t: treesToRemove){
                if (t.getParent() != null) {
                    t.getParent().getChildren().remove(t);
                    t.setParent(null);
                }
            }
        }

        protected void lastChanceMatch(ITree src, ITree dst) {
            Set<Integer> mappedSrcITreeIndexes = new HashSet<>();
            Set<Integer> mappedDstITreeIndexes = new HashSet<>();
            MyITreeMap srcMap = new MyITreeMap();
            MyITreeMap dstMap = new MyITreeMap();
            getMappedTreeIndexInPreOrder(src, true, mappedSrcITreeIndexes, srcMap);
            getMappedTreeIndexInPreOrder(dst, false, mappedDstITreeIndexes, dstMap);
            ITree cSrc = src.deepCopy();
            ITree cDst = dst.deepCopy();
            MyITreeMap cSrcMap = new MyITreeMap();
            MyITreeMap cDstMap = new MyITreeMap();
            removeMatched(cSrc, mappedSrcITreeIndexes, cSrcMap);
            removeMatched(cDst, mappedDstITreeIndexes, cDstMap);

            if (cSrc.getMetrics().size < AbstractBottomUpMatcher.SIZE_THRESHOLD
                    || cDst.getMetrics().size < AbstractBottomUpMatcher.SIZE_THRESHOLD) {
                Matcher m = new ZsMatcher();
                MappingStore zsMappings = m.match(cSrc, cDst, new MappingStore(cSrc, cDst));
                for (Mapping candidate : zsMappings) {
                    ITree srcCand = candidate.first;
                    ITree dstCand = candidate.second;
                    int copySrcCandId = cSrcMap.getIdOfTree(srcCand);
                    int copyDstCandId = cDstMap.getIdOfTree(dstCand);
                    ITree originSrcCand = srcMap.getITreeById(copySrcCandId);
                    ITree originDstCand = dstMap.getITreeById(copyDstCandId);

                    if (src == originSrcCand || dst == originDstCand)
                        continue;

                    if (mappings.isMappingAllowed(originSrcCand, originDstCand)) {
                        mappings.addMapping(originSrcCand, originDstCand);
                    }
                }
            }
        }
    }
}