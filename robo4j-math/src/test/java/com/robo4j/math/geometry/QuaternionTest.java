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
package com.robo4j.math.geometry;

import org.junit.Assert;
import org.junit.Test;

/**
 * Matrix tests.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class QuaternionTest {
	public final static double ERROR_EPSILON = 0.00000001;
	
	@Test
	public void testConversions() {
		Tuple3d euler = new Tuple3d(0, Math.toRadians(90), 0);
		Tuple4d quaternion = QuaternionUtils.toQuaternion(euler);
		Assert.assertEquals(0.7071067811865476, quaternion.t, ERROR_EPSILON);
		Assert.assertEquals(0.7071067811865476, quaternion.x, ERROR_EPSILON);
		Tuple3d eulerBack = QuaternionUtils.toEuler(quaternion);
		Assert.assertEquals(euler.x, eulerBack.x, ERROR_EPSILON);
		Assert.assertEquals(euler.y, eulerBack.y, ERROR_EPSILON);
		Assert.assertEquals(euler.z, eulerBack.z, ERROR_EPSILON);	
	}
}
