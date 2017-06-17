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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.rpi.i2c.magnetometer;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.robo4j.math.geometry.Matrix3i;
import com.robo4j.math.geometry.Tuple3i;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class MagnetometerLSM303MatrixTest {

	private static boolean active = true;
	private static ExecutorService executor = Executors.newFixedThreadPool(1);
	private static int maxX;
	private static int minX;
	private static int maxY;
	private static int minY;
	private static int maxZ;
	private static int minZ;
	private static Matrix3i matrix3i = null;

	public static void main(String[] args) throws Exception {
		if (args.length < 2) {
			System.out.println(
					"Usage: MagnetometerLSM303Test <delay between reads (ms)> <print every Nth read> [<print style (pretty|raw|csv)>] ");
			System.exit(1);
		}
		int delay = Integer.parseInt(args[0]);
		int modulo = Integer.parseInt(args[1]);
		MagnetometerLSM303Device device = new MagnetometerLSM303Device();

		System.out.println(String.format("Press Enter to start delay: %d, modulo: %d", delay, modulo));
		System.out.println("Press Enter: next simulation create biasVector, matrix: ");
		System.in.read();

		executor.submit(() -> {
			int count = 0;
			while (active) {
				if (count % modulo == 0) {
					try {
						Tuple3i val = device.readRaw();

						maxX = val.x > 0 && val.x > maxX ? val.x : maxX;
						minX = val.x < 0 && val.x < minX ? val.x : minX;
						maxY = val.y > 0 && val.y > maxY ? val.y : maxY;
						minY = val.y < 0 && val.y < minY ? val.y : minY;
						maxZ = val.z > 0 && val.z > maxZ ? val.z : maxZ;
						minZ = val.z < 0 && val.z < minZ ? val.z : minZ;

						if (matrix3i == null) {
							//@formatter:off
                            matrix3i = new Matrix3i(
                                    getAxesArray(val)[0],
                                    getAxesArray(val)[1],
                                    getAxesArray(val)[2],
                                    getAxesArray(val)[3],
                                    getAxesArray(val)[4],
                                    getAxesArray(val)[5],
                                    getAxesArray(val)[6],
                                    getAxesArray(val)[7],
                                    getAxesArray(val)[8]);
                            //@formatter:on
						} else {
							//@formatter:off
                            Matrix3i m = new Matrix3i(
                                    getAxesArray(val)[0],
                                    getAxesArray(val)[1],
                                    getAxesArray(val)[2],
                                    getAxesArray(val)[3],
                                    getAxesArray(val)[4],
                                    getAxesArray(val)[5],
                                    getAxesArray(val)[6],
                                    getAxesArray(val)[7],
                                    getAxesArray(val)[8]);
                            //@formatter:on
							matrix3i = matrix3i.diff(m);
						}

						System.out.println("Matrix Value: " + matrix3i);
						Thread.sleep(delay);
					} catch (IOException | InterruptedException e) {
						System.err.println("error: " + e);
					}
				}
				count++;
			}
		});
		System.in.read();
		active = false;
		executor.shutdown();

		int biasX = minX + maxX;
		int biasY = minY + maxY;
		int biasZ = minZ + maxZ;
		Tuple3i biasVector = new Tuple3i(biasX, biasY, biasZ);

		System.out.println("Results:");
		System.out.println("Bias Vector (Bv): " + biasVector);
		System.out.println("Matrix (M) : " + matrix3i);
		System.out.println("BiasedResult (R): R=M*Bv' : " + matrix3i.multiply(biasVector));
		System.out.println("simulation end");

	}

	private static int[] getAxesArray(Tuple3i val) {
		int[] result = new int[9];
		result[0] = val.x * val.x;
		result[1] = val.x * val.y;
		result[2] = val.x * val.z;
		result[3] = val.y * val.x;
		result[4] = val.y * val.y;
		result[5] = val.y * val.z;
		result[6] = val.z * val.x;
		result[7] = val.z * val.y;
		result[8] = val.z * val.z;
		return result;
	}
}
