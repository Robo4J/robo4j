/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This RoboClawEngine.java  is part of robo4j.
 * module: robo4j-hw-rpi
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
package com.robo4j.hw.rpi.pwm.roboclaw;

import java.io.IOException;

import com.robo4j.hw.rpi.IMotor;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device.PWMChannel;
import com.robo4j.hw.rpi.i2c.pwm.Servo;

/**
 * A RoboClaw engine controller, controlled with a standard servo PWM signal.
 * 
 * @author Marcus Hirt
 */
public class RoboClawEngine implements IMotor {
	private final String name;
	private final Servo servo;

	public RoboClawEngine(String name, PWMChannel channel, boolean invert) {
		this.name = name;
		this.servo = new Servo(channel);
		this.servo.setInverted(invert);
		try {
			this.servo.setInput(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public void setSpeed(float speed) throws IOException {
		servo.setInput(speed);
	}

	@Override
	public float getSpeed() throws IOException {
		return servo.getInput();
	}
}
