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
package com.robo4j.units.rpi.roboclaw;

/**
 * Event for controlling the {@link RoboClawRCTankUnit}.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MotionEvent {
	private final float speed;
	private final float direction;

	public MotionEvent(float speed, float direction) {
		this.speed = speed;
		this.direction = direction;
	}

	public float getSpeed() {
		return speed;
	}

	public float getDirection() {
		return direction;
	}

	public String toString() {
		return String.format("Speed: %f, Direction: %f", speed, direction);
	}
}
