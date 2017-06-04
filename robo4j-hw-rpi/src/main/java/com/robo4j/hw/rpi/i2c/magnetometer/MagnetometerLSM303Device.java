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
package com.robo4j.hw.rpi.i2c.magnetometer;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.i2c.ReadableDevice;
import com.robo4j.math.geometry.Float3D;
import com.robo4j.math.geometry.Int3D;

/**
 * Represents a LSM303 magnetometer, for example the one on the Adafruit IMU
 * breakout board.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
// FIXME(Marcus/Dec 5, 2016): Verify that this one works.
public class MagnetometerLSM303Device extends AbstractI2CDevice implements ReadableDevice<Float3D> {
	private static final int DEFAULT_I2C_ADDRESS = 0x1e;
	private static final int CRA_REG_M = 0x00;
	private static final int CRB_REG_M = 0x01;
	private static final int MR_REG_M = 0x02;
	private static final int OUT_X_H_M = 0x03;

	private static final int RESULT_BUFFER_SIZE = 6;
	private static final int ENABLE_TEMP = 0x80;
	private Gain gain = Gain.GAIN_1_3;

	public MagnetometerLSM303Device() throws IOException {
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS, Mode.CONTINUOUS_CONVERSION, Rate.RATE_1_5, false);
	}

	public MagnetometerLSM303Device(int bus, int address, Mode mode, Rate rate, boolean enableTemp) throws IOException {
		super(bus, address);
		initialize(mode, rate, enableTemp);
	}

	public synchronized Float3D read() throws IOException {
		Float3D rawData = new Float3D();
		byte[] data = new byte[RESULT_BUFFER_SIZE];
		int n = i2cDevice.read(OUT_X_H_M, data, 0, RESULT_BUFFER_SIZE);
		if (n != RESULT_BUFFER_SIZE) {
			getLogger().warning("Failed to read all data from accelerometer. Should have read 6, could only read " + n);
		}
		rawData.x = read16bitSigned(data, 0) / gain.getXY();
		rawData.y = read16bitSigned(data, 2) / gain.getXY();
		rawData.z = read16bitSigned(data, 4) / gain.getZ();
		return rawData;
	}

	public synchronized Int3D readRaw() throws IOException {
		Int3D rawData = new Int3D();
		byte[] data = new byte[RESULT_BUFFER_SIZE];
		int n = i2cDevice.read(OUT_X_H_M, data, 0, RESULT_BUFFER_SIZE);
		if (n != RESULT_BUFFER_SIZE) {
			getLogger().warning("Failed to read all data from accelerometer. Should have read 6, could only read " + n);
		}
		// Yep, this is indeed the correct order ;)
		rawData.x = read16bitSigned(data, 0);
		rawData.z = read16bitSigned(data, 2);
		rawData.y = read16bitSigned(data, 4);
		return rawData;
	}

	/**
	 * Helper function to convert the result to a compass heading. Note that
	 * this only works if the device is lying flat. For a better result, use
	 * accelerometer readings, and properly calculate the heading.
	 * 
	 * @param magResult
	 *            a result to use to calculate the compass heading.
	 * 
	 * @return the (XY) compass heading.
	 */
	public static float getCompassHeading(Int3D magResult) {
		float heading = (float) ((Math.atan2(magResult.y, magResult.x) * 180.0) / Math.PI);

		if (heading < 0) {
			heading = 360 + heading;
		}
		return heading;
	}

	private short read16bitSigned(byte[] data, int i) {
		short val = (short) (data[i + 1] << 8 | (data[i] & 0xFF));
		return val;
	}

	private void initialize(Mode mode, Rate rate, boolean enableTemp) throws IOException {
		writeByte(MR_REG_M, (byte) mode.getCtrlCode());
		int cra = enableTemp ? ENABLE_TEMP : 0x0;
		writeByte(CRA_REG_M, (byte) ((rate.getCtrlCode() | cra) & 0xFF));

		setGain(gain);
		if (gain.getCtrlCode() != (byte) readByte(CRB_REG_M)) {
			throw new IOException("Could not communicate with the magnetometer");
		}
	}

	public void setGain(Gain gain) throws IOException {
		writeByte(CRB_REG_M, gain.getCtrlCode());
		this.gain = gain;
	}

	public enum Gain {
		//@formatter:off
		GAIN_1_3	(1.3f, 0x20, 1100, 980),
		GAIN_1_9	(1.9f, 0x40, 855, 760),
		GAIN_2_5	(2.5f, 0x60, 670, 600),
		GAIN_4_0	(4.0f,0x80, 450, 400),
		GAIN_4_7	(4.7f, 0xA0, 400, 350),
		GAIN_5_6	(5.6f, 0xC0, 330, 295),
		GAIN_8_1	(8.1f, 0xE0, 230, 205);
		//@formatter:on

		private float gain;
		private byte ctrlCode;
		private float xy;
		private float z;

		Gain(float gain, int ctrlCode, float xy, float z) {
			this.gain = gain;
			this.ctrlCode = (byte) ctrlCode;
			this.xy = xy;
			this.z = z;
		}

		public byte getCtrlCode() {
			return ctrlCode;
		}

		public float getGain() {
			return gain;
		}

		public float getXY() {
			return xy;
		}

		public float getZ() {
			return z;
		}
	}

	public enum Rate {
		//@formatter:off
		RATE_0_75(0.75f, 0x00), 
		RATE_1_5(1.5f, 0x01), 
		RATE_3_0(3.0f, 0x62), 
		RATE_7_5(7.5f, 0x03), 
		RATE_15(15f, 0x04), 
		RATE_30(30f, 0x05), 
		RATE_75(75f, 0x06), 
		RATE_220(220f, 0x07);
		//@formatter:on

		private float rate;
		private int ctrlCode;

		Rate(float rate, int ctrlCode) {
			this.rate = rate;
			this.ctrlCode = ctrlCode;

		}

		public int getCtrlCode() {
			return ctrlCode;
		}

		public float getRate() {
			return rate;
		}
	}

	public enum Mode {
		//@formatter:off
		CONTINUOUS_CONVERSION	(0x0),
		SINGLE_CONVERSION		(0x1),
		SLEEP					(0x2);
		//@formatter:on
		private int ctrlCode;

		Mode(int ctrlCode) {
			this.ctrlCode = ctrlCode;
		}

		public int getCtrlCode() {
			return ctrlCode;
		}
	}
}
