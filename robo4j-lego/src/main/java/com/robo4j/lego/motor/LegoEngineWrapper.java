/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This LegoEngineWrapper.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.lego.motor;

import com.robo4j.commons.motor.GenericMotor;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.enums.LegoAnalogPortEnum;
import com.robo4j.lego.enums.LegoEngineEnum;
import com.robo4j.lego.enums.LegoEnginePartEnum;

import lejos.robotics.RegulatedMotor;

/**
 * @author Miro Wengner (@miragemiko)
 * @since 23.11.2016
 */
public abstract class LegoEngineWrapper<MotorType extends RegulatedMotor> implements GenericMotor, LegoEngine {

	protected MotorType unit;
	protected LegoAnalogPortEnum port;
	protected LegoEngineEnum engine;
	protected LegoEnginePartEnum part;

	public abstract LegoAnalogPortEnum getPort();

	public abstract LegoEngineEnum getEngine();

	public abstract LegoEnginePartEnum getPart();

	public MotorType getUnit() {
		return unit;
	}

	public void setUnit(MotorType unit) {
		this.unit = unit;
	}

	@Override
	public void forward() {
		unit.forward();
	}

	@Override
	public void backward() {
		unit.backward();
	}

	@Override
	public void stop() {
		unit.stop(true);
	}

	@Override
	public void rotate(int val) {
		unit.rotate(val);
	}

	@Override
	public boolean isMoving() {
		return unit.isMoving();
	}

	@Override
	public void setSpeed(int speed) {
		unit.setSpeed(speed);
	}

	@Override
	public void close() {
		unit.close();
	}
}
