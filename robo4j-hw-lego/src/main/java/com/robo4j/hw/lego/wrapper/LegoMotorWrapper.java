/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoMotorWrapper.java  is part of robo4j.
 * module: robo4j-hw-lego
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.LegoMotor;
import com.robo4j.hw.lego.enums.LegoAnalogPortEnum;

import com.robo4j.hw.lego.enums.MotorTypeEnum;
import lejos.robotics.RegulatedMotor;

/**
 * Wrapper for the Lego Motor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 23.11.2016
 */
public abstract class LegoMotorWrapper<Motor extends RegulatedMotor> implements LegoMotor {

	protected Motor motor;
	protected LegoAnalogPortEnum port;
	protected MotorTypeEnum motorType;

	public abstract LegoAnalogPortEnum getPort();

	public abstract MotorTypeEnum getMotorType();


	public Motor getMotor() {
		return unit;
	}

	public void setMotor(Motor unit) {
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
