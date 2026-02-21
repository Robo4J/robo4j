/*
 * Copyright (c) 2014, 2026, Marcus Hirt, Miroslav Wengner
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
 * Tests for ZXY Euler ↔ quaternion conversions.
 * Convention: x=heading(Z), y=pitch(X-right), z=roll(Y-forward).
 * Quaternion: x=i, y=j, z=k, t=real(w).
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class QuaternionTest {
	private static final double ERROR_EPSILON = 0.00000001;
	private static final double COS_45 = Math.cos(Math.PI / 4);
	private static final double SIN_45 = Math.sin(Math.PI / 4);

	@Test
	void testPureHeading90() {
		// 90° heading = rotation around Z → q = (0, 0, sin45, cos45)
		Tuple3d euler = new Tuple3d(Math.toRadians(90), 0, 0);
		Tuple4d q = QuaternionUtils.toQuaternion(euler);
		assertEquals(COS_45, q.t, ERROR_EPSILON);
		assertEquals(0, q.x, ERROR_EPSILON);
		assertEquals(0, q.y, ERROR_EPSILON);
		assertEquals(SIN_45, q.z, ERROR_EPSILON);
		assertRoundTrip(euler);
	}

	@Test
	void testPurePitch90() {
		// 90° pitch = rotation around X → q = (sin45, 0, 0, cos45)
		Tuple3d euler = new Tuple3d(0, Math.toRadians(90), 0);
		Tuple4d q = QuaternionUtils.toQuaternion(euler);
		assertEquals(COS_45, q.t, ERROR_EPSILON);
		assertEquals(SIN_45, q.x, ERROR_EPSILON);
		assertEquals(0, q.y, ERROR_EPSILON);
		assertEquals(0, q.z, ERROR_EPSILON);
		assertRoundTrip(euler);
	}

	@Test
	void testPureRoll90() {
		// 90° roll = rotation around Y → q = (0, sin45, 0, cos45)
		Tuple3d euler = new Tuple3d(0, 0, Math.toRadians(90));
		Tuple4d q = QuaternionUtils.toQuaternion(euler);
		assertEquals(COS_45, q.t, ERROR_EPSILON);
		assertEquals(0, q.x, ERROR_EPSILON);
		assertEquals(SIN_45, q.y, ERROR_EPSILON);
		assertEquals(0, q.z, ERROR_EPSILON);
		assertRoundTrip(euler);
	}

	@Test
	void testUpsideDown() {
		// 180° roll = upside down → q = (0, 1, 0, 0)
		Tuple4d q = new Tuple4d(0, 1, 0, 0);
		Tuple3d euler = QuaternionUtils.toEuler(q);
		assertEquals(0, euler.x, ERROR_EPSILON);           // heading = 0
		assertEquals(0, euler.y, ERROR_EPSILON);           // pitch = 0
		assertEquals(Math.PI, Math.abs(euler.z), ERROR_EPSILON);  // roll = ±180°
	}

	@Test
	void testIdentityRoundTrip() {
		assertRoundTrip(new Tuple3d(0, 0, 0));
	}

	@Test
	void testCombinedAnglesRoundTrip() {
		assertRoundTrip(new Tuple3d(Math.toRadians(45), Math.toRadians(30), Math.toRadians(60)));
	}

	private void assertRoundTrip(Tuple3d euler) {
		Tuple4d q = QuaternionUtils.toQuaternion(euler);
		Tuple3d back = QuaternionUtils.toEuler(q);
		assertEquals(euler.x, back.x, ERROR_EPSILON, "heading");
		assertEquals(euler.y, back.y, ERROR_EPSILON, "pitch");
		assertEquals(euler.z, back.z, ERROR_EPSILON, "roll");
	}
}
