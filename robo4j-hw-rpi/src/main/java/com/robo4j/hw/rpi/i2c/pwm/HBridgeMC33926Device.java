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
package com.robo4j.hw.rpi.i2c.pwm;

import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.robo4j.hw.rpi.Motor;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device.PWMChannel;

/**
 * Motor controller for the Pololu H-bridge motor controller based on
 * Freescale's MC33926.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HBridgeMC33926Device implements Motor {
	private final String name;
	private final PWMChannel channel;
	private final boolean invert;
	private final GpioPinDigitalOutput in1;
	private final GpioPinDigitalOutput in2;

	private Direction direction = Direction.FORWARD;
	private float speed = 0;

	public enum Direction {
		FORWARD, REVERSE
	}

	public HBridgeMC33926Device(String name, PWMChannel channel, Pin in1, Pin in2, boolean invert) {
		this.name = name;
		this.channel = channel;
		this.invert = invert;
		GpioController gpio = GpioFactory.getInstance();
		this.in1 = gpio.provisionDigitalOutputPin(in1, "IN1", PinState.LOW);
		this.in2 = gpio.provisionDigitalOutputPin(in2, "IN2", PinState.HIGH);
		setDirection(Direction.FORWARD);
	}

	public String getName() {
		return name;
	}

	@Override
	public float getSpeed() throws IOException {
		return (this.getDirection() == Direction.FORWARD ? 1 : -1) * internalGetSpeed();
	}

	@Override
	public void setSpeed(float speed) throws IOException {
		if (speed < 0) {
			if (this.direction != Direction.REVERSE) {
				setDirection(Direction.REVERSE);
			}
		} else {
			if (this.direction != Direction.FORWARD) {
				setDirection(Direction.FORWARD);
			}
		}
		internalSetSpeed(speed);
	}
	
	private void internalSetSpeed(float speed) throws IOException {
		int width = Math.round(speed * 4095);
		channel.setPWM(0, width);
		this.speed = speed;
	}

	private float internalGetSpeed() {
		return speed;
	}

	private Direction getDirection() {
		return direction;
	}

	private void setDirection(Direction direction) {
		boolean forward = direction == Direction.FORWARD;
		if (invert) {
			forward = !forward;
		}

		if (forward) {
			in1.setState(PinState.HIGH);
			in2.setState(PinState.LOW);
		} else {
			in1.setState(PinState.LOW);
			in2.setState(PinState.HIGH);
		}
		this.direction = direction;
	}
}
