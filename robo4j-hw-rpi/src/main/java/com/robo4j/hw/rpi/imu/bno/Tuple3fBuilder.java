/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.hw.rpi.imu.bno;

import com.robo4j.math.geometry.Tuple3f;

/**
 * Tuple3fBuilder build a Tuple3f by fixed point values abd defined Q-point
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Tuple3fBuilder {

	private final int qPoint;
	private int fixedX;
	private int fixedY;
	private int fixedZ;

	public Tuple3fBuilder(int qPoint) {
		this.qPoint = qPoint;
	}

	public Tuple3fBuilder setX(int x) {
		fixedX = x;
		return this;
	}

	public Tuple3fBuilder setY(int y) {
		fixedY = y;
		return this;
	}

	public Tuple3fBuilder setZ(int z) {
		fixedZ = z;
		return this;
	}

	public Tuple3f build(){
	    float x = ShtpUtils.intToFloat(fixedX, qPoint);
	    float y = ShtpUtils.intToFloat(fixedY, qPoint);
	    float z = ShtpUtils.intToFloat(fixedZ, qPoint);
	    return new Tuple3f(x, y, z);
    }

}
