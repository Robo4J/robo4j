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
package com.robo4j.hw.rpi.pwm.roboclaw;

import jdk.jfr.*;

@Name("coffe.engine.EngineChange")
@Label("Engine Change")
@Category("Coff-E")
@Description("An event for engine changes")
@StackTrace(false)
public class EngineEvent extends Event {

	@Label("Direction")
	@Description("The steering direction (degrees[-180, 180])")
	private float direction;

	@Label("Speed")
	@Description("The speed [0,1]")
	private float speed;

	static {
		FlightRecorder.register(EngineEvent.class);
	}

	public float getDirection() {
		return direction;
	}

	public void setDirection(float direction) {
		this.direction = direction;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}
}
