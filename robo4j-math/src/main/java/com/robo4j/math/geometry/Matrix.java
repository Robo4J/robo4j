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

/**
 * Things common for all matrices.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface Matrix {

	/**
	 * @return the number of elements in the matrix.
	 */
	int getRows();

	/**
	 * @return the number of columns in the matrix.
	 */
	int getColumns();

	/**
	 * Returns the number at the location. For performance, you should probably
	 * use the primitive-type-specific getValue in the particular matrix
	 * implementation.
	 * 
	 * @param row
	 *            the row for which to retrieve the value.
	 * @param column
	 *            the column for which to retrieve the value.
	 * @return the Number at the position specified.
	 */
	Number getNumber(int row, int column);
	
	/**
	 * Transposes the matrix.
	 */
	void transpose();
}
