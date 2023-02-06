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
 * Copyright 2015-2016 Georg Dotzler <georg.dotzler@fau.de>
 * Copyright 2015-2016 Marius Kamp <marius.kamp@fau.de>
 */

package com.github.gumtreediff.matchers.heuristic.mtdiff.hungarian;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * An implementation of the so-called Hungarian Algorithm (Kuhn-Munkres algorithm) for the linear
 * assignment problem. It is based on http://csclab.murraystate.edu/bob.pilgrim/445/munkres.html
 */
public final class Hungarian {
    private static final class Pair {
        public int col;
        public int row;

        public Pair(final int row, final int col) {
            this.row = row;
            this.col = col;
        }
    }

    private static final double EPSILON = 1e-8;

    private static void addSmallest(final DoubleMatrix matrix, final ByteMatrix mask,
            final boolean[] rowCover, final boolean[] colCover) {
        // Step 6
        final int n = matrix.numRows();

        final double minVal = findSmallest(matrix, rowCover, colCover);

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (rowCover[i] && colCover[j]) {
                    matrix.set(i, j, matrix.get(i, j) + minVal);
                }
                if (!rowCover[i] && !colCover[j]) {
                    matrix.set(i, j, matrix.get(i, j) - minVal);
                }
            }
        }
    }

    private static void alternatingSeries(final DoubleMatrix matrix, final ByteMatrix mask,
            final boolean[] rowCover, final boolean[] colCover, final int pathRow0,
            final int pathCol0) {
        // Step 5
        final ArrayList<Pair> path = new ArrayList<>();
        int pathCount = 1;
        path.add(new Pair(pathRow0, pathCol0));

        boolean done = false;
        do {
            int jvar = path.get(pathCount - 1).col;
            int ivar = findStarInCol(mask, jvar);
            if (ivar > -1) {
                pathCount += 1;
                path.add(new Pair(ivar, jvar));
            } else {
                done = true;
            }

            if (!done) {
                ivar = path.get(pathCount - 1).row;
                jvar = findPrimeInRow(mask, ivar);
                pathCount += 1;
                path.add(new Pair(ivar, jvar));
            }
        } 
        while (!done);

        augmentPath(mask, pathCount, path);
        clearCovers(rowCover, colCover);
        erasePrimes(mask);
        coverColumns(matrix, mask, rowCover, colCover);
    }

    /**
     * Find a minimizing solution for the linear assignment problem specified by the matrix.
     *
     * @param matrix the matrix
     * @return An array with an element for each row containing the assigned column of the matrix.
     */
    public static int[] assign(final DoubleMatrix matrix) {
        final DoubleMatrix matrixNxN = makeNxN(matrix);
        try {
            subtractSmallest(matrixNxN);

            final ByteMatrix mask = ByteMatrix.newMatrix(matrixNxN.numRows(), matrixNxN.numCols());
            try {
                findZeros(matrixNxN, mask);
                return readResult(mask);
            } finally {
                mask.finish();
            }
        } finally {
            if (matrix != matrixNxN) {
                matrixNxN.finish();
            }
        }
    }

    private static void augmentPath(final ByteMatrix mask, final int pathCount,
            final ArrayList<Pair> path) {
        for (int p = 0; p < pathCount; ++p) {
            final Pair pathElem = path.get(p);
            if (mask.get(pathElem.row, pathElem.col) == 1) {
                mask.set(pathElem.row, pathElem.col, (byte) 0);
            } else {
                mask.set(pathElem.row, pathElem.col, (byte) 1);
            }
        }
    }

    private static void clearCovers(final boolean[] rowCover, final boolean[] colCover) {
        for (int k = 0; k < rowCover.length; ++k) {
            rowCover[k] = false;
            colCover[k] = false;
        }
    }

    private static void coverColumns(final DoubleMatrix matrix, final ByteMatrix mask,
            final boolean[] rowCover, final boolean[] colCover) {
        // Step 3
        final int n = matrix.numRows();

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (mask.get(i, j) == 1) {
                    colCover[j] = true;
                }
            }
        }

        int colCount = 0;
        for (int j = 0; j < n; ++j) {
            if (colCover[j]) {
                colCount += 1;
            }
        }
        if (colCount < n) {
            findNonCovered(matrix, mask, rowCover, colCover);
        }
        // Else finished
    }

    private static void erasePrimes(final ByteMatrix mask) {
        final int n = mask.numRows();

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (mask.get(i, j) == 2) {
                    mask.set(i, j, (byte) 0);
                }
            }
        }
    }

    // == Helpers ==
    private static Pair findAZero(final DoubleMatrix matrix, final ByteMatrix mask,
            final boolean[] rowCover, final boolean[] colCover) {
        final int n = matrix.numRows();

        int ivar = 0;
        int row = -1;
        int col = -1;
        boolean done = false;
        do {
            for (int j = 0; (j < n) && !done; ++j) {
                if ((matrix.get(ivar, j) < EPSILON) && !rowCover[ivar] && !colCover[j]) {
                    row = ivar;
                    col = j;
                    done = true;
                }
            }
            ivar += 1;
            if (ivar >= n) {
                done = true;
            }
        } 
        while (!done);

        return new Pair(row, col);
    }

    private static void findNonCovered(final DoubleMatrix matrix, final ByteMatrix mask,
            final boolean[] rowCover, final boolean[] colCover) {
        // Step 4
        int row = -1;
        int col = -1;
        boolean done = false;
        do {
            final Pair zero = findAZero(matrix, mask, rowCover, colCover);
            row = zero.row;
            col = zero.col;

            if (row == -1) {
                // done = true;
                addSmallest(matrix, mask, rowCover, colCover);
            } else {
                mask.set(row, col, (byte) 2);
                int starInRow = findStarInRow(mask, row);
                if (starInRow >= 0) {
                    rowCover[row] = true;
                    colCover[starInRow] = false;
                } else {
                    done = true;
                    alternatingSeries(matrix, mask, rowCover, colCover, row, col);
                }
            }

        }
        while (!done);
    }

    private static int findPrimeInRow(final ByteMatrix mask, final int row) {
        return findXInRow(mask, row, 2);
    }

    private static double findSmallest(final DoubleMatrix matrix, final boolean[] rowCover,
            final boolean[] colCover) {
        final int n = matrix.numRows();

        double minValue = Double.MAX_VALUE;
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (!rowCover[i] && !colCover[j]) {
                    minValue = Math.min(minValue, matrix.get(i, j));
                }
            }
        }
        return minValue;
    }

    private static int findStarInCol(final ByteMatrix mask, final int col) {
        final int n = mask.numRows();

        int row = -1;
        for (int i = 0; i < n; ++i) {
            if (mask.get(i, col) == 1) {
                row = i;
            }
        }
        return row;
    }

    private static int findStarInRow(final ByteMatrix mask, final int row) {
        return findXInRow(mask, row, 1);
    }

    private static int findXInRow(final ByteMatrix mask, final int row, final int xpar) {
        final int n = mask.numRows();

        int col = -1;
        for (int j = 0; j < n; ++j) {
            if (mask.get(row, j) == xpar) {
                col = j;
            }
        }
        return col;
    }

    private static void findZeros(final DoubleMatrix matrix, final ByteMatrix mask) {
        // Step 2
        final int n = matrix.numRows();
        final boolean[] rowCover = new boolean[n];
        final boolean[] colCover = new boolean[n];

        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((matrix.get(i, j) < EPSILON) && !rowCover[i] && !colCover[j]) {
                    mask.set(i, j, (byte) 1);
                    rowCover[i] = true;
                    colCover[j] = true;
                }
            }
        }
        Arrays.fill(rowCover, false);
        Arrays.fill(colCover, false);

        coverColumns(matrix, mask, rowCover, colCover);
    }

    private static DoubleMatrix makeNxN(final DoubleMatrix matrix) {
        assert matrix.numRows() > 0;
        if (matrix.numRows() == matrix.numCols()) {
            return matrix;
        }

        // KxM -> NxN with N = max(K, M)
        final int n = Math.max(matrix.numRows(), matrix.numCols());
        final DoubleMatrix result = DoubleMatrix.newMatrix(n, n);
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if ((i >= matrix.numRows()) || (j >= matrix.numCols())) {
                    result.set(i, j, 0);
                } else {
                    result.set(i, j, matrix.get(i, j));
                }
            }
        }
        return result;
    }

    private static int[] readResult(final ByteMatrix mask) {
        // Last step
        final int n = mask.numRows();

        final int[] result = new int[n];
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n; ++j) {
                if (mask.get(i, j) == 1) {
                    result[i] = j;
                    break;
                }
            }
        }

        return result;
    }

    private static void subtractSmallest(final DoubleMatrix matrix) {
        // Step 1
        final int n = matrix.numRows();
        for (int i = 0; i < n; ++i) {
            double minValue = matrix.get(i, 0);
            for (int j = 1; j < n; ++j) {
                minValue = Math.min(minValue, matrix.get(i, j));
            }
            for (int j = 0; j < n; ++j) {
                matrix.set(i, j, matrix.get(i, j) - minValue);
            }
        }
    }

    private Hungarian() {}
}
