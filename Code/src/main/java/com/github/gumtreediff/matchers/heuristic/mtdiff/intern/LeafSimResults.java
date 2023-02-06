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

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class LeafSimResults<T> {
    public final ConcurrentHashMap<T, ArrayList<MatchingCandidate>> subleafCandidateMap;
    public final ConcurrentSkipListSet<MatchingCandidate> submatchedLeaves;

    LeafSimResults(ConcurrentHashMap<T, ArrayList<MatchingCandidate>> subleafCandidateMap,
                   ConcurrentSkipListSet<MatchingCandidate> submatchedLeaves) {
        super();
        this.subleafCandidateMap = subleafCandidateMap;
        this.submatchedLeaves = submatchedLeaves;
    }

}
