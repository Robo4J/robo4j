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
 * Converts between quaternions and Euler angles using the ZXY decomposition
 * (vehicle convention).
 * <p>
 * Assumes a right-handed coordinate system with X=right, Y=forward, Z=up
 * (the BNO08x sensor frame). Euler angles are returned as:
 * <ul>
 *   <li>Heading (yaw): rotation around Z-up, range ±180°</li>
 *   <li>Pitch: rotation around X-right (nose up/down), range ±90°</li>
 *   <li>Roll: rotation around Y-forward (banking), range ±180°</li>
 * </ul>
 * Quaternion layout in {@link Tuple4d}: x=i, y=j, z=k, t=real (w).
 *
 * @author Marcus Hirt (@hirt)
 */
public final class QuaternionUtils {

	/**
	 * Converts a unit quaternion to Euler angles using ZXY decomposition.
	 * The returned Tuple3d contains: x=heading, y=pitch, z=roll (all in radians).
	 *
	 * @param quaternion the quaternion (x=i, y=j, z=k, t=real).
	 * @return Euler angles: x=heading (±π), y=pitch (±π/2), z=roll (±π).
	 */
	public static Tuple3d toEuler(Tuple4d quaternion) {
		double x = quaternion.x;
		double y = quaternion.y;
		double z = quaternion.z;
		double w = quaternion.t;

		// Normalize
		double norm = Math.sqrt(w * w + x * x + y * y + z * z);
		if (norm > 0) {
			w /= norm;
			x /= norm;
			y /= norm;
			z /= norm;
		}

		// ZXY Euler decomposition
		double sinPitch = 2.0 * (y * z + w * x);
		sinPitch = Math.max(-1.0, Math.min(1.0, sinPitch));

		Tuple3d result = new Tuple3d();
		result.x = Math.atan2(2.0 * (w * z - x * y), 1.0 - 2.0 * (x * x + z * z));
		result.y = Math.asin(sinPitch);
		result.z = Math.atan2(2.0 * (w * y - x * z), 1.0 - 2.0 * (x * x + y * y));
		return result;
	}

	/**
	 * Converts Euler angles to a quaternion using ZXY decomposition.
	 *
	 * @param euler x=heading, y=pitch, z=roll (all in radians).
	 * @return the quaternion (x=i, y=j, z=k, t=real).
	 */
	public static Tuple4d toQuaternion(Tuple3d euler) {
		return toQuaternion(euler.x, euler.y, euler.z);
	}

	/**
	 * Converts Euler angles to a quaternion using ZXY decomposition.
	 *
	 * @param heading rotation around Z-up in radians.
	 * @param pitch   rotation around X-right in radians.
	 * @param roll    rotation around Y-forward in radians.
	 * @return the quaternion (x=i, y=j, z=k, t=real).
	 */
	public static Tuple4d toQuaternion(double heading, double pitch, double roll) {
		double cH = Math.cos(heading / 2);
		double sH = Math.sin(heading / 2);
		double cP = Math.cos(pitch / 2);
		double sP = Math.sin(pitch / 2);
		double cR = Math.cos(roll / 2);
		double sR = Math.sin(roll / 2);

		// ZXY: q = qZ * qX * qY
		Tuple4d quat = new Tuple4d();
		quat.t = cH * cP * cR - sH * sP * sR;
		quat.x = cH * sP * cR - sH * cP * sR;
		quat.y = cH * cP * sR + sH * sP * cR;
		quat.z = cH * sP * sR + sH * cP * cR;
		return quat;
	}
}
