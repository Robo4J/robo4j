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

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;

/**
 * Abstraction for talking to a PCA9685 PWM/Servo driver. For example an
 * Adafruit 16 channel I2C PWM driver breakout board.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
// Not using all commands - yet.
@SuppressWarnings("unused")
public class PWMPCA9685Device extends AbstractI2CDevice {
	private static final int DEFAULT_I2C_ADDRESS = 0x40;
	private static final int DEFAULT_FREQUENCY = 50;

	private static final double PRESCALE_FACTOR = 25000000.0 / 4096.0;

	private static final int MODE1 = 0x00;
	private static final int MODE2 = 0x01;
	private static final int SUBADR1 = 0x02;
	private static final int SUBADR2 = 0x03;
	private static final int SUBADR13 = 0x04;
	private static final int PRESCALE = 0xFE;
	private static final int LED0_ON_L = 0x06;
	private static final int LED0_ON_H = 0x07;
	private static final int LED0_OFF_L = 0x08;
	private static final int LED0_OFF_H = 0x09;
	private static final int ALL_LED_ON_L = 0xFA;
	private static final int ALL_LED_ON_H = 0xFB;
	private static final int ALL_LED_OFF_L = 0xFC;
	private static final int ALL_LED_OFF_H = 0xFD;

	private static final int RESTART = 0x80;
	private static final int SLEEP = 0x10;
	private static final int ALLCALL = 0x01;
	private static final int INVRT = 0x10;
	private static final int OUTDRV = 0x04;

	private double frequency = Double.NaN;

	/**
	 * Constructs a PWM device using the default settings. (I2CBUS.BUS_1, 0x40)
	 * 
	 * @throws IOException
	 *             if there was communication problem
	 */
	public PWMPCA9685Device() throws IOException {
		// 0x40 is the default address used by the AdaFruit PWM board.
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS);
	}

	/**
	 * Creates a software interface to an Adafruit 16 channel I2C PWM driver
	 * board (PCA9685).
	 * 
	 * @param bus
	 *            the I2C bus to use.
	 * @param address
	 *            the address to use.
	 * 
	 * @see I2CBus
	 * 
	 * @throws IOException
	 *             if there was communication problem
	 */
	public PWMPCA9685Device(int bus, int address) throws IOException {
		super(bus, address);
		initialize();
	}

	/**
	 * Sets all PWM channels to the provided settings.
	 * 
	 * @param on
	 *            when to turn on the signal [0, 4095]
	 * @param off
	 *            when to turn off the signal [0, 4095]
	 * 
	 * @throws IOException
	 *             if there was a problem communicating with the device.
	 */
	public void setAllPWM(int on, int off) throws IOException {
		writeByte(ALL_LED_ON_L, (byte) (on & 0xFF));
		writeByte(ALL_LED_ON_H, (byte) (on >> 8));
		writeByte(ALL_LED_OFF_L, (byte) (off & 0xFF));
		writeByte(ALL_LED_OFF_H, (byte) (off >> 8));
	}

	/**
	 * Sets the PWM frequency to use. This is common across all channels. For
	 * controlling RC servos, 50Hz is a good starting point.
	 * 
	 * @param frequency
	 *            the PWM frequency to use, in Hz.
	 * @throws IOException
	 *             if a problem occurred accessing the device.
	 */
	public void setPWMFrequency(double frequency) throws IOException {
		double prescaleval = PRESCALE_FACTOR / frequency;
		prescaleval -= 1.0;
		double prescale = Math.floor(prescaleval + 0.5);
		int oldmode = i2cDevice.read(MODE1);
		int newmode = (oldmode & 0x7F) | 0x10;
		writeByte(MODE1, (byte) newmode);
		writeByte(PRESCALE, (byte) (Math.floor(prescale)));
		writeByte(MODE1, (byte) oldmode);
		sleep(50);
		writeByte(MODE1, (byte) (oldmode | 0x80));
		this.frequency = frequency;
	}

	/**
	 * @return the PWM frequency set, or Double.NaN if no frequency have been
	 *         explicitly set.
	 */
	public double getPWMFrequency() {
		return frequency;
	}

	/**
	 * Returns one of the PWM channels on the device. Allowed range is [0, 15].
	 * 
	 * @param channel
	 *            the channel to retrieve.
	 * 
	 * @return the specified PWM channel.
	 */
	public PWMChannel getChannel(int channel) {
		return new PWMChannel(channel);
	}

	/**
	 * Use to control a PWM channel on the PWM device.
	 * 
	 * @see PWMPCA9685Device#getChannel(int)
	 */
	public class PWMChannel {
		private final int channel;

		private PWMChannel(int channel) {
			if (channel < 0 || channel > 15) {
				throw new IllegalArgumentException("There is no channel " + channel + " on the board.");
			}
			this.channel = channel;
		}

		/**
		 * Configures the PWM pulse for the PWMChannel.
		 * 
		 * @param on
		 *            when to go from low to high [0, 4095]. 0 means at the very
		 *            start of the pulse, 4095 at the very end.
		 * @param off
		 *            when to go from high to low [0, 4095]. 0 means at the very
		 *            start of the pulse, 4095 at the very end.
		 * 
		 * @throws IOException
		 *             exception
		 */
		public void setPWM(int on, int off) throws IOException {
			i2cDevice.write(LED0_ON_L + 4 * channel, (byte) (on & 0xFF));
			i2cDevice.write(LED0_ON_H + 4 * channel, (byte) (on >> 8));
			i2cDevice.write(LED0_OFF_L + 4 * channel, (byte) (off & 0xFF));
			i2cDevice.write(LED0_OFF_H + 4 * channel, (byte) (off >> 8));
		}

		/**
		 * @return the PWM device that this channel is associated with.
		 */
		public PWMPCA9685Device getPWMDevice() {
			return PWMPCA9685Device.this;
		}

		/**
		 * @return the channel id used by this channel.
		 */
		public Object getChannelID() {
			return channel;
		}
	}

	private void sleep(int millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// Don't care
		}
	}

	private void initialize() throws IOException {
		setAllPWM(0, 0);
		writeByte(MODE2, (byte) OUTDRV);
		writeByte(MODE1, (byte) ALLCALL);
		sleep(50);
		int mode1 = readByte(MODE1);
		mode1 = mode1 & ~SLEEP;
		writeByte(MODE1, (byte) mode1);
		sleep(50);
	}

	/**
	 * Creates a {@link PWMPCA9685Device}, or returns null if unsuccessful.
	 * Meant to be used in lambdas.
	 * 
	 * @param bus
	 *            the bus
	 * @param address
	 *            the address
	 * @return the device if it all worked out, null if it failed.
	 */
	public static PWMPCA9685Device createDevice(int bus, int address) {
		return createDevice(bus, address, DEFAULT_FREQUENCY);
	}

	public static PWMPCA9685Device createDevice(int bus, int address, int frequency) {
		try {
			PWMPCA9685Device result = new PWMPCA9685Device(bus, address);
			result.setPWMFrequency(frequency);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
