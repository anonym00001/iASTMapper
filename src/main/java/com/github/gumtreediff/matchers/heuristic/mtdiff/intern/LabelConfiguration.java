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

import com.github.gumtreediff.tree.Type;

import java.util.HashSet;

public class LabelConfiguration {
    public final Type basicTypeLabel;
    public final Type classLabel;
    public final Type identifierLabel;
    HashSet<Type> labelsForBoolCompare;
    HashSet<Type> labelsForIntCompare;

    HashSet<Type> labelsForRealCompare;
    public HashSet<Type> labelsForStringCompare;
    public HashSet<Type> labelsForValueCompare;
    public final Type qualifierLabel;
    final Type rootLabel;

    /**
     * Instantiates a new label configuration.
     *
     * @param labelsForValueCompare the labels for value compare
     */
    public LabelConfiguration(HashSet<Type> labelsForValueCompare) {
        this(null, null, null, null, null,
                labelsForValueCompare, new HashSet<Type>(), new HashSet<Type>(),
                new HashSet<Type>(), new HashSet<Type>());

    }

    /**
     * Instantiates a new label configuration.
     *
     * @param identifierLabel        the identifier label
     * @param rootLabel              the root label
     * @param classLabel             the class label
     * @param basicTypeLabel         the basic type label
     * @param qualifierLabel         the qualifier label
     * @param labelsForValueCompare  the labels for value compare
     * @param labelsForRealCompare   the labels for real compare
     * @param labelsForIntCompare    the labels for int compare
     * @param labelsForStringCompare the labels for string compare
     * @param labelsForBoolCompare   the labels for bool compare
     */
    public LabelConfiguration(Type identifierLabel, Type rootLabel, Type classLabel, Type basicTypeLabel,
                              Type qualifierLabel, HashSet<Type> labelsForValueCompare,
                              HashSet<Type> labelsForRealCompare, HashSet<Type> labelsForIntCompare,
                              HashSet<Type> labelsForStringCompare, HashSet<Type> labelsForBoolCompare) {
        this.identifierLabel = identifierLabel;
        this.rootLabel = rootLabel;
        this.classLabel = classLabel;
        this.labelsForValueCompare = labelsForValueCompare;
        this.labelsForBoolCompare = labelsForBoolCompare;
        this.labelsForRealCompare = labelsForRealCompare;
        this.labelsForIntCompare = labelsForIntCompare;
        this.labelsForStringCompare = labelsForStringCompare;
        this.labelsForBoolCompare = labelsForBoolCompare;
        this.basicTypeLabel = basicTypeLabel;
        this.qualifierLabel = qualifierLabel;
    }
}
