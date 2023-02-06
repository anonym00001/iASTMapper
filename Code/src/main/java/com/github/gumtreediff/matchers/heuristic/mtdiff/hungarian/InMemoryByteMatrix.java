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

package com.github.gumtreediff.matchers.heuristic.mtdiff.hungarian;

final class InMemoryByteMatrix extends ByteMatrix {
    private final byte[][] data;

    @SuppressWarnings("ucd")
    InMemoryByteMatrix(final int rows, final int cols) {
        super(rows, cols);
        data = new byte[rows][cols];
    }


    /**
     * Finish the matrix.
     */
    @Override
    public void finish() {
    }


    /**
     * Gets the matrix entry.
     *
     * @param row the row
     * @param col the col
     * @return the byte
     */
    @Override
    public byte get(final int row, final int col) {
        return data[row][col];
    }


    /**
     * Sets the matrix entry.
     *
     * @param row the row
     * @param col the col
     * @param val the val
     */
    @Override
    public void set(final int row, final int col, final byte val) {
        data[row][col] = val;
    }
}
