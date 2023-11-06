/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
 * Class to help convert from quaternions to euler angles and vice versa.
 * 
 * @author Marcus
 */
public final class QuaternionUtils {
	public final static double DEGREES_PRECISION_AT_POLES = Math.toRadians(0.5);
	/**
	 * This is the factor to test.
	 */
	private final static double EPSILON_TEST = Math.sin((Math.PI / 2.0) - DEGREES_PRECISION_AT_POLES) / 2.0;

	/**
	 * Returns the quaternion as Euler angles. x = heading, y = roll, z = pitch
	 * from a quaternion.
	 * 
	 * @param quaternion
	 *            the quaternion for which to calculate the euler angles.
	 * @return the Euler angles.
	 */
	public static Tuple3d toEuler(Tuple4d quaternion) {
		Tuple3d result = new Tuple3d();
		double sqw = quaternion.t * quaternion.t;
		double sqx = quaternion.x * quaternion.x;
		double sqy = quaternion.y * quaternion.y;
		double sqz = quaternion.z * quaternion.z;
		double unit = sqx + sqy + sqz + sqw;
		double test = quaternion.x * quaternion.y + quaternion.z * quaternion.t;
		if (test > EPSILON_TEST * unit) {
			// singularity at north pole
			result.x = (2.0 * Math.atan2(quaternion.x, quaternion.t));
			result.y = 0;
			result.z = Math.PI / 2.0;
		} else if (test < -EPSILON_TEST * unit) {
			// singularity at south pole
			result.x = -2.0 * Math.atan2(quaternion.x, quaternion.t);
			result.y = 0;
			result.z = -Math.PI / 2;
		} else {
			result.x = Math.atan2(2.0 * quaternion.y * quaternion.t - 2.0 * quaternion.x * quaternion.z, sqx - sqy - sqz + sqw);
			result.y = Math.atan2(2.0 * quaternion.x * quaternion.t - 2.0 * quaternion.y * quaternion.z, -sqx + sqy - sqz + sqw);
			result.z = Math.asin(2.0 * test / unit);
		}
		return result;
	}

	/**
	 * Returns the Euler angles as a quaternion.
	 * 
	 * @param euler
	 *            x = heading, y = roll, z = pitch, all in radians.
	 * @return the Euler angles as a quaternion.
	 */
	public static Tuple4d toQuaternion(Tuple3d euler) {
		return toQuaternion(euler.x, euler.y, euler.z);
	}

	/**
	 * Returns a quaternion describing the rotation.
	 * 
	 * @param heading
	 *            the heading change in radians.
	 * @param roll
	 *            the roll change in radians.
	 * @param pitch
	 *            the pitch change in radians.
	 * @return the resulting quaternion.
	 */
	public static Tuple4d toQuaternion(double heading, double roll, double pitch) {
		Tuple4d quat = new Tuple4d();
		double c1 = Math.cos(heading / 2);
		double s1 = Math.sin(heading / 2);
		double c2 = Math.cos(pitch / 2);
		double s2 = Math.sin(pitch / 2);
		double c3 = Math.cos(roll / 2);
		double s3 = Math.sin(roll / 2);
		double c1c2 = c1 * c2;
		double s1s2 = s1 * s2;
		quat.t = c1c2 * c3 - s1s2 * s3;
		quat.x = c1c2 * s3 + s1s2 * c3;
		quat.y = s1 * c2 * c3 + c1 * s2 * s3;
		quat.z = c1 * s2 * c3 - s1 * c2 * s3;
		return quat;
	}
}
