
/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Matrix tests.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class MatrixTest {
	static Integer [] TESTVALUES_3D_INT = new Integer[] {2, 3, 5, 7, 11, 13, 17, 19, 23};
	static Float [] TESTVALUES_3D_FLOAT = new Float[] {2.0f, 3.0f, 5.0f, 7.0f, 11.0f, 13.0f, 17.0f, 19.0f, 23.0f};
	static Double [] TESTVALUES_3D_DOUBLE = new Double[] {2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0};
	static Integer [] TESTVALUES_4D_INT = new Integer[] {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53};
	static Float [] TESTVALUES_4D_FLOAT = new Float[] {2.0f, 3.0f, 5.0f, 7.0f, 11.0f, 13.0f, 17.0f, 19.0f, 23.0f, 29.0f, 31.0f, 37.0f, 41.0f, 43.0f, 47.0f, 53.0f};
	static Double [] TESTVALUES_4D_DOUBLE = new Double[] {2.0, 3.0, 5.0, 7.0, 11.0, 13.0, 17.0, 19.0, 23.0, 29.0, 31.0, 37.0, 41.0, 43.0, 47.0, 53.0};
	
	@Test
	public void testCreation() {
		Matrix matrix = createMatrix(TESTVALUES_3D_INT);
		assertEquals(3, matrix.getRows());
		assertEquals(3, matrix.getColumns());
		validate(matrix, TESTVALUES_3D_INT);
		
		matrix = createMatrix(TESTVALUES_3D_FLOAT);
		assertEquals(3, matrix.getRows());
		assertEquals(3, matrix.getColumns());
		validate(matrix, TESTVALUES_3D_FLOAT);
		
		matrix = createMatrix(TESTVALUES_3D_DOUBLE);
		assertEquals(3, matrix.getRows());
		assertEquals(3, matrix.getColumns());
		validate(matrix, TESTVALUES_3D_DOUBLE);
		
		// 4D
		matrix = createMatrix(TESTVALUES_4D_INT);
		assertEquals(4, matrix.getRows());
		assertEquals(4, matrix.getColumns());
		validate(matrix, TESTVALUES_4D_INT);
		
		matrix = createMatrix(TESTVALUES_4D_FLOAT);
		assertEquals(4, matrix.getRows());
		assertEquals(4, matrix.getColumns());
		validate(matrix, TESTVALUES_4D_FLOAT);
		
		matrix = createMatrix(TESTVALUES_4D_DOUBLE);
		assertEquals(4, matrix.getRows());
		assertEquals(4, matrix.getColumns());
		validate(matrix, TESTVALUES_4D_DOUBLE);
	}
	
	@Test
	void testTranspose() {
		Matrix matrix = createMatrix(TESTVALUES_3D_INT);
		Matrix matrixTransposed = createMatrix(TESTVALUES_3D_INT);
		matrixTransposed.transpose();
		validateTranspose(matrix, matrixTransposed);

		matrix = createMatrix(TESTVALUES_3D_FLOAT);
		matrixTransposed = createMatrix(TESTVALUES_3D_FLOAT);
		matrixTransposed.transpose();
		validateTranspose(matrix, matrixTransposed);

		matrix = createMatrix(TESTVALUES_3D_DOUBLE);
		matrixTransposed = createMatrix(TESTVALUES_3D_DOUBLE);
		matrixTransposed.transpose();
		validateTranspose(matrix, matrixTransposed);

		// 4D
		matrix = createMatrix(TESTVALUES_4D_INT);
		matrixTransposed = createMatrix(TESTVALUES_4D_INT);
		matrixTransposed.transpose();
		validateTranspose(matrix, matrixTransposed);

		matrix = createMatrix(TESTVALUES_4D_FLOAT);
		matrixTransposed = createMatrix(TESTVALUES_4D_FLOAT);
		matrixTransposed.transpose();
		validateTranspose(matrix, matrixTransposed);

		matrix = createMatrix(TESTVALUES_4D_DOUBLE);
		matrixTransposed = createMatrix(TESTVALUES_4D_DOUBLE);
		matrixTransposed.transpose();
		validateTranspose(matrix, matrixTransposed);
	}

	@Test
	void testSysout() {
		System.out.println(createMatrix(TESTVALUES_3D_INT));
		System.out.println(createMatrix(TESTVALUES_3D_FLOAT));
		System.out.println(createMatrix(TESTVALUES_3D_DOUBLE));
		System.out.println(createMatrix(TESTVALUES_4D_INT));
		System.out.println(createMatrix(TESTVALUES_4D_FLOAT));
		System.out.println(createMatrix(TESTVALUES_4D_DOUBLE));
	}
	
	private void validate(Matrix matrix, Number[] numbers) {
		for (int i = 0; i < matrix.getRows(); i++) {
			for (int j = 0; j < matrix.getColumns(); j++) {
				assertEquals(numbers[i * matrix.getColumns() + j], matrix.getNumber(i, j));
			}
		}
	}

	private void validateTranspose(Matrix matrix, Matrix transpose) {		
		for (int i = 0; i < matrix.getRows(); i++) {
			for (int j = 0; j < matrix.getColumns(); j++) {
				assertEquals(matrix.getNumber(i, j), transpose.getNumber(j, i));
			}
		}
	}

	
	private Matrix createMatrix(Number[] testValues) {
		if (testValues.length == 9) {
			if (testValues.getClass() == Integer[].class) {
				return new Matrix3i(toIntArray(testValues));
			}
			if (testValues.getClass() == Float[].class) {
				return new Matrix3f(toFloatArray(testValues));
			}
			if (testValues.getClass() == Double[].class) {
				return new Matrix3d(toDoubleArray(testValues));				
			}
		}
		if (testValues.length == 16) {
			if (testValues.getClass() == Integer[].class) {
				return new Matrix4i(toIntArray(testValues));
			}
			if (testValues.getClass() == Float[].class) {
				return new Matrix4f(toFloatArray(testValues));
			}
			if (testValues.getClass() == Double[].class) {
				return new Matrix4d(toDoubleArray(testValues));				
			}
		}
		
		return null;
	}

	private static int[] toIntArray(Number[] testValues) {
		int [] vals = new int[testValues.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = testValues[i].intValue();
		}
		return vals;
	}
	private static float[] toFloatArray(Number[] testValues) {
		float [] vals = new float[testValues.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = testValues[i].intValue();
		}
		return vals;
	}
	private static double[] toDoubleArray(Number[] testValues) {
		double [] vals = new double[testValues.length];
		for (int i = 0; i < vals.length; i++) {
			vals[i] = testValues[i].intValue();
		}
		return vals;
	}
	
}
