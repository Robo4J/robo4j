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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi.pwm;

import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.wiringpi.Gpio;
import com.robo4j.hw.rpi.Servo;

/**
 * Abstraction for talking to a servo over one of the hardware PWM GPIO pins on
 * the RaspberryPi.
 * <p>
 * All Raspberry Pis support hardware PWM on GPIO_01.
 * </p>
 * <p>
 * Raspberry Pi models A+, B+, 2B, 3B also support hardware PWM on the following
 * GPIO pins:
 * <ul>
 * <li>GPIO_23</li>
 * <li>GPIO_24</li>
 * <li>GPIO_26</li>
 * </ul>
 * </p>
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class PWMServo implements Servo {
	// Raspberry Pi base frequency
	private final static int BASE_FREQUENCY = 19_200_000;
	private final static int RANGE = 1000;
	private final static float RANGE_F = RANGE;

	// Servo Frequency in Hz
	private final static int PWM_FREQUENCY = 50;

	// Clock divisor to use
	private final static int CLOCK_DIVISOR = BASE_FREQUENCY / (RANGE * PWM_FREQUENCY);

	// Select divisor and range to make the actual frequency as close to the
	// PWM_FREQUENCY as possible
	private final static float ACTUAL_FREQUENCY = BASE_FREQUENCY / (float) (RANGE * CLOCK_DIVISOR);
	private final static float ACTUAL_PERIOD = 1 / ACTUAL_FREQUENCY;

	private GpioPinPwmOutput pwmPin;

	private boolean inverted;
	private float input;

	/**
	 * Constructor.
	 * 
	 * @param pin
	 *            the GpioPin to use for PWM.
	 */
	public PWMServo(Pin pin, boolean inverted) {
		this.inverted = inverted;
		GpioController gpio = GpioFactory.getInstance();
		pwmPin = gpio.provisionPwmOutputPin(pin);

		if (GpioFactory.getDefaultProvider().getName().equals(pin.getProvider())) {
			throw new UnsupportedOperationException("Pin provider is set up to be " + GpioFactory.getDefaultProvider().getName() + ". You cannot use " + pin.getProvider());
		}
		// Servo -> mark:space
		Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
		Gpio.pwmSetClock(CLOCK_DIVISOR);
		Gpio.pwmSetRange(RANGE);
	}

	@Override
	public boolean isInverted() {
		return inverted;
	}

	@Override
	public void setInverted(boolean inverted) {
		this.inverted = inverted;
	}

	@Override
	public void setTrim(float trim) {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public float getTrim() {
		throw new UnsupportedOperationException("Not implemented yet!");
	}

	@Override
	public float getInput() throws IOException {
		return input;
	}

	@Override
	public void setInput(float input) throws IOException {
		this.input = input;
		float correctedInput = isInverted() ? -1 * input : input;
		float targetOnTime = 0.0015f + (correctedInput * 0.001f) / 2;
		int pwm = Math.round((RANGE_F * (targetOnTime / ACTUAL_PERIOD)));
		pwmPin.setPwm(pwm);
	}
}
