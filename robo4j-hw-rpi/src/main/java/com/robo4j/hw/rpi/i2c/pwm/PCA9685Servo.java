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
package com.robo4j.hw.rpi.i2c.pwm;

import java.io.IOException;

import com.robo4j.hw.rpi.Servo;
import com.robo4j.hw.rpi.i2c.pwm.PWMPCA9685Device.PWMChannel;

/**
 * Simple wrapper class for simplifying working with a PWM Channel having a
 * servo. Adds commonly found RC features such as dual rate, center trim and
 * expo.
 * 
 * Note, if the PWM frequency of the PWM device is changed, all related servos
 * must be reset.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class PCA9685Servo implements Servo {
	private static final int TRIM_STEPS = 200;

	private final PWMChannel channel;
	private boolean invert;
	private int min;
	private int max;
	private float trim = 0.0f;
	private float dualRate = 1.0f;
	private float expo = 0.0f;
	private volatile float input = 0.0f;

	/**
	 * Constructs a servo wrapper instance for the specified channel.
	 * 
	 * @param channel
	 */
	public PCA9685Servo(PWMChannel channel) {
		this.channel = channel;
		reset();
	}

	/**
	 * Sets the normalized input to this servo, between -1 (min) and 1 (max).
	 * 
	 * @param newInput
	 *            normalized input between -1 and 1.
	 * 
	 * @throws IOException
	 *             if there was a problem communicating with the device.
	 */
	@Override
	public void setInput(float input) throws IOException {
		float actualInput = calculateExpo(input);
		actualInput = (actualInput * dualRate) + trim / TRIM_STEPS;
		
		actualInput = (actualInput + 1.0f)/2;
		if (invert) {
			actualInput = 1.0f - actualInput;
		}	
		int width = Math.round((max - min) * actualInput) + min;
		channel.setPWM(0, width);
		this.input = input;
	}


	@Override
	public float getInput() throws IOException {
		return input;
	}
	
	/**
	 * Sets dual rate. This can be used to limit, or expand, the output range of
	 * your servo. 1 (100%) is the default. Be careful when setting values
	 * higher than 1 so that you do not damage your servo.
	 * 
	 * @param dualRate
	 *            the dual rate multiplier to use.
	 */
	public void setDualRate(float dualRate) {
		this.dualRate = dualRate;
	}

	/**
	 * Sets the expo to use. This can be used to limit or increase the
	 * sensitivity around the center. Valid values are [-1, 1] with 0 being the
	 * default.
	 * 
	 * @param expo
	 */
	public void setExpo(float expo) {
		this.expo = expo;
	}

	/**
	 * Resets the servo to all base settings.
	 */
	public void reset() {
		double frequency = channel.getPWMDevice().getPWMFrequency();
		// Literature on RC servos says a 1ms pulse is minimum, 1,5ms is centered and 2ms is max. 
		min = calculatePulseWidth(1, frequency);
		max = calculatePulseWidth(2, frequency);
	}


	@Override
	public float getTrim() {
		return trim;
	}

	/**
	 * Sets the trim. This will translate the entire output curve.
	 * 
	 * @param steps
	 *            the absolute position to set the trim to.
	 */
	@Override
	public void setTrim(float trim) {
		this.trim = trim;
	}

	/**
	 * If set to true, input will be treated as inverted for this servo.
	 * 
	 * @param invert
	 */
	@Override
	public void setInverted(boolean invert) {
		this.invert = invert;
	}

	/**
	 * If set to true, input will be treated as inverted for this servo.
	 */
	@Override
	public boolean isInverted() {
		return invert;
	}

	@Override
	public String toString() {
		return String.format("Servo on ch %d [min:%d, max:%d, invert:%s, trim:%f, dualrate:%f, expo:%f]", 
				channel.getChannelID(), min, max, invert, trim, dualRate, expo);
	}
	
	private float calculateExpo(float input) {
		if (expo == 0) {
			return input;
		}
		float c = expo / 2;
		return c * input * input * input + (1 - c) * input;
	}
	
	private static int calculatePulseWidth(double millis, double frequency) {
		return (int) (Math.round(4096 * millis * frequency / 1000));
	}

	public float getDualRate() {
		return dualRate;
	}

	public float getExpo() {
		return expo;
	}
}
