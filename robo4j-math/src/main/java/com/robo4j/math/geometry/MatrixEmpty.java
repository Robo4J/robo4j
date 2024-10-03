/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.math.geometry;

import java.lang.System.Logger;

/**
 *  Empty Matrix dimensions equal to zero
 */
public class MatrixEmpty implements Matrix {
    public static final int DIMENSION_ZERO = 0;
    @Override
    public int getRows() {
        return DIMENSION_ZERO;
    }

    @Override
    public int getColumns() {
        return DIMENSION_ZERO;
    }

    @Override
    public Number getNumber(int row, int column) {
        return DIMENSION_ZERO;
    }

    @Override
    public void transpose() {
        throw new IllegalStateException("not valid operation");
    }
}
