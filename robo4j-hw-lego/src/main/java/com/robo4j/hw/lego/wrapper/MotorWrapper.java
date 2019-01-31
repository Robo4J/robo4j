/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.ILegoMotor;
import com.robo4j.hw.lego.enums.AnalogPortEnum;

import com.robo4j.hw.lego.enums.MotorTypeEnum;
import com.robo4j.hw.lego.provider.MotorProvider;
import lejos.robotics.RegulatedMotor;

/**
 * Wrapper for any LegoMindstorm Motor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class MotorWrapper<Motor extends RegulatedMotor> implements ILegoMotor {

	protected Motor motor;
	protected AnalogPortEnum port;
	protected MotorTypeEnum type;

	@SuppressWarnings("unchecked")
	public MotorWrapper(MotorProvider provider,  AnalogPortEnum port, MotorTypeEnum type) {
		this.motor = (Motor)provider.create(port, type);
		this.port = port;
		this.type = type;
	}

	public MotorWrapper(Motor motor, AnalogPortEnum port, MotorTypeEnum type) {
		this.motor = motor;
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
