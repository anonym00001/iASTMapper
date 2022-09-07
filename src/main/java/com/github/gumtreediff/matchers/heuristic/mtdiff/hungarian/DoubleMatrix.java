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

/**
 * An abstract class representing a matrix of type double.
 */
public abstract class DoubleMatrix {

    /**
     * New matrix.
     *
     * @param matrix the matrix
     * @return the double matrix
     */
    public static DoubleMatrix newMatrix(final double[][] matrix) {
        final DoubleMatrix result = newMatrix(matrix.length, matrix[0].length);
        for (int i = 0; i < matrix.length; ++i) {
            for (int j = 0; j < matrix[0].length; ++j) {
                result.set(i, j, matrix[i][j]);
            }
        }
        return result;
    }

    /**
     * New matrix.
     *
     * @param rows the rows
     * @param cols the cols
     * @return the double matrix
     */
    public static DoubleMatrix newMatrix(final int rows, final int cols) {
        return new InMemoryDoubleMatrix(rows, cols);
    }

    private final int cols;

    private final int rows;

    protected DoubleMatrix(final int rows, final int cols) {
        this.rows = rows;
        this.cols = cols;
    }

    /**
     * Releases the resources hold by this matrix.
     */
    public abstract void finish();

    /**
     * Returns the element in the specified row and column.
     *
     * @param row the row
     * @param col the col
     * @return the double
     */
    public abstract double get(final int row, final int col);

    /**
     * Num cols.
     *
     * @return the int
     */
    public int numCols() {
        return cols;
    }

    /**
     * Num rows.
     *
     * @return the int
     */
    public int numRows() {
        return rows;
    }

    /**
     * Sets the element in the specified row and column.
     *
     * @param row the row
     * @param col the col
     * @param val the val
     */
    public abstract void set(final int row, final int col, final double val);
}
