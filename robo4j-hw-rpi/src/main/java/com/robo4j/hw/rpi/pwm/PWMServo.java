/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.pwm;

//import com.pi4j.io.gpio.GpioController;
//import com.pi4j.io.gpio.GpioFactory;
//import com.pi4j.io.gpio.GpioPinPwmOutput;
//import com.pi4j.io.gpio.Pin;
//import com.pi4j.io.gpio.RaspiPin;
//import com.pi4j.wiringpi.Gpio;

import java.io.IOException;

import com.pi4j.Pi4J;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmType;
import com.robo4j.hw.rpi.Servo;
import com.robo4j.hw.rpi.utils.GpioPin;

/**
 * Abstraction for talking to a servo over one of the hardware PWM GPIO pins on
 * the RaspberryPi.
 * &lt;p&gt;
 * All Raspberry Pis support hardware PWM on GPIO_01.
 * &lt;/p&gt;
 * &lt;p&gt;
 * Raspberry Pi models A+, B+, 2B, 3B also support hardware PWM on the following
 * GPIO pins:
 * &lt;ul&gt;
 * &lt;li&gt;GPIO_23&lt;/li&gt;
 * &lt;li&gt;GPIO_24&lt;/li&gt;
 * &lt;li&gt;GPIO_26&lt;/li&gt;
 * &lt;/ul&gt;
 * &lt;/p&gt;
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

//	private GpioPinPwmOutput pwmPin;
	private final Pwm pwm;

	private boolean inverted;
	private float input;

	/**
	 * Constructor.
	 * 
	 * @param pin
	 *            the GpioPin to use for PWM.
	 * @param inverted
	 *            if the servo input should be inverted.
	 */
	public PWMServo(GpioPin gpioPin, boolean inverted) {

		var pi4jRpiContext = Pi4J.newAutoContext();
		var pwmConfig = Pwm.newConfigBuilder(pi4jRpiContext)
				.id("PWMServo-"+gpioPin.address())
				.address(gpioPin.address())
				.frequency(PWM_FREQUENCY)
				.pwmType(PwmType.HARDWARE)
				.dutyCycle(CLOCK_DIVISOR)
				.build();
		this.pwm = pi4jRpiContext.create(pwmConfig);


//		this.inverted = inverted;
//		GpioController gpio = GpioFactory.getInstance();
//		pwmPin = gpio.provisionPwmOutputPin(pin);
//
//		if (!GpioFactory.getDefaultProvider().getName().equals(pin.getProvider())) {
//			throw new UnsupportedOperationException(
//					"Pin provider is set up to be " + GpioFactory.getDefaultProvider().getName() + ". You cannot use " + pin.getProvider());
//		}
		// Servo -> mark:space
//		Gpio.pwmSetMode(Gpio.PWM_MODE_MS);
//		Gpio.pwmSetClock(CLOCK_DIVISOR);
//		Gpio.pwmSetRange(RANGE);
	}

	/**
	 * Constructor.
	 * 
	 * @param pinAddress
	 *            the RaspiPin pin address.
	 * @param inverted
	 *            if the servo input should be inverted.
	 */
	public PWMServo(int pinAddress, boolean inverted) {
		this(GpioPin.getByAddress(pinAddress), inverted);
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

	/**
	 *
	 * @param input duty cycle
	 *
	 *
	 * @throws IOException exception
	 */
	@Override
	public void setInput(float input) throws IOException {
		this.input = input;
		float correctedInput = isInverted() ? -1 * input : input;
		float targetOnTime = 0.0015f + (correctedInput * 0.001f) / 2;
//		var pwm = Math.round((RANGE_F * (targetOnTime / ACTUAL_PERIOD)));
		var dutyCycle = RANGE_F * (targetOnTime / ACTUAL_PERIOD);
//		pwmPin.setPwm(pwm);
		this.pwm.on(dutyCycle);
	}
}
