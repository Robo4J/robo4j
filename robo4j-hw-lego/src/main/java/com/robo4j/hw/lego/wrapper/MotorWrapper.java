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
import com.robo4j.hw.lego.enums.AnalogPortEnum;

import com.robo4j.hw.lego.enums.MotorTypeEnum;
import lejos.robotics.RegulatedMotor;

/**
 * Wrapper for the Lego Motor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 23.11.2016
 */
public class MotorWrapper<Motor extends RegulatedMotor> implements LegoMotor {

	protected Motor motor;
	protected AnalogPortEnum port;
	protected MotorTypeEnum type;

	public MotorWrapper(AnalogPortEnum port, MotorTypeEnum type) {
		this.port = port;
		this.type = type;
	}

	@Override
	public AnalogPortEnum getPort() {
		return port;
	}

	@Override
	public MotorTypeEnum getType() {
		return type;
	}

	public Motor getMotor() {
		return motor;
	}

	public void setMotor(Motor unit) {
		this.motor = unit;
	}

	@Override
	public void forward() {
		motor.forward();
	}

	@Override
	public void backward() {
		motor.backward();
	}

	@Override
	public void stop() {
		motor.stop(true);
	}

	@Override
	public void rotate(int val) {
		motor.rotate(val);
	}

	@Override
	public boolean isMoving() {
		return motor.isMoving();
	}

	@Override
	public void setSpeed(int speed) {
		motor.setSpeed(speed);
	}


	@Override
	public void close() {
		motor.close();
	}
}
