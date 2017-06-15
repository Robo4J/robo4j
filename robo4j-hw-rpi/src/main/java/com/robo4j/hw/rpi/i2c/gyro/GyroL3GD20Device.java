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
package com.robo4j.hw.rpi.i2c.gyro;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.i2c.ReadableDevice;
import com.robo4j.math.geometry.Tuple3f;

/**
 * Abstraction to read angular change from a L3GD20 Gyro, for example the Gyro 
 * available on the Adafruit 10DOF breakout board.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class GyroL3GD20Device extends AbstractI2CDevice implements
		ReadableDevice<Tuple3f> {
	private static final int DEFAULT_I2C_ADDRESS = 0x6b;

	private final Sensitivity sensitivity;

	// Registers
	private final int REGISTER_WHO_AM_I = 0x0F;
	private final int REGISTER_CTRL_REG1 = 0x20;
	// private final int REGISTER_CTRL_REG2 = 0x21;
	// private final int REGISTER_CTRL_REG3 = 0x22;
	private final int REGISTER_CTRL_REG4 = 0x23;
	private final int REGISTER_CTRL_REG5 = 0x24;
	// private final int REGISTER_REFERENCE = 0x25;
	// private final int REGISTER_OUT_TEMP = 0x26;
	// private final int REGISTER_STATUS_REG = 0x27;
	private final int REGISTER_OUT_X_L = 0x28;
	private final int REGISTER_OUT_X_H = 0x29;
	// private final int REGISTER_OUT_Y_L = 0x2A;
	// private final int REGISTER_OUT_Y_H = 0x2B;
	// private final int REGISTER_OUT_Z_L = 0x2C;
	// private final int REGISTER_OUT_Z_H = 0x2D;
	// private final int REGISTER_FIFO_CTRL_REG = 0x2E;
	// private final int REGISTER_FIFO_SRC_REG = 0x2F;
	// private final int REGISTER_INT1_CFG = 0x30;
	// private final int REGISTER_INT1_SRC = 0x31;
	// private final int REGISTER_TSH_XH = 0x32;
	// private final int REGISTER_TSH_XL = 0x33;
	// private final int REGISTER_TSH_YH = 0x34;
	// private final int REGISTER_TSH_YL = 0x35;
	// private final int REGISTER_TSH_ZH = 0x36;
	// private final int REGISTER_TSH_ZL = 0x37;
	// private final int REGISTER_INT1_DURATION = 0x38;
	private final int REGISTER_LOW_ODR = 0x39;

	// Constants
	private final int L3GD20_ID = 0xD4;
	private final int L3GD20H_ID = 0xD7;

	public GyroL3GD20Device(Sensitivity sensitivity) throws IOException {
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS, sensitivity, true);
	}

	public GyroL3GD20Device(int bus, int address, Sensitivity sensitivity, boolean enableHighPassFilter)
			throws IOException {
		super(bus, address);
		this.sensitivity = sensitivity;
		initialize(enableHighPassFilter);
	}

	private void initialize(boolean enableHighPassFilter) throws IOException {
		int id = i2cDevice.read(REGISTER_WHO_AM_I);
		if ((id != L3GD20_ID) && (id != L3GD20H_ID)) {
			throw new IOException(String.format("Did not find L3GD20 chip on the address %02X. Read ID was %02X.", getAddress(), id));
		}
		// Reset / Power down
		i2cDevice.write(REGISTER_CTRL_REG1, (byte) 0x00);
		// Enable all three channels + normal mode (also sets data rate and bandwidth, see chip docs)
		i2cDevice.write(REGISTER_CTRL_REG1, (byte) 0x0F);
		// Set sensitivity
		i2cDevice.write(REGISTER_CTRL_REG4,
				(byte) sensitivity.fullScaleSelectionMask);
		if (enableHighPassFilter) {
			i2cDevice.write(REGISTER_CTRL_REG5, (byte) 0x10);			
		}
		i2cDevice.write(REGISTER_LOW_ODR, (byte) 0x1);
	}

	public enum OperationMode {
		BYPASS, FIFO, STREAM, BYPASS_TO_STREAM, STREAM_TO_FIFO;
	}

	public enum Sensitivity {
		/**
		 * 245 degrees/s range (default)
		 */
		DPS_245(0x00, 8.75f / 1000.0f),
		/**
		 * 500 degrees/s range
		 */
		DPS_500(0x10, 17.5f / 1000.0f),
		/**
		 * 2000 degrees/s range
		 */
		DPS_2000(0x20, 70.0f / 1000.0f);

		private final int fullScaleSelectionMask;
		private final float sensitivityFactor;

		Sensitivity(int fullScaleSelectionMask, float sensitivityFactor) {
			this.fullScaleSelectionMask = fullScaleSelectionMask;
			this.sensitivityFactor = sensitivityFactor;
		}

		public float getSensitivityFactor() {
			return sensitivityFactor;
		}		
	}

	public Tuple3f read() throws IOException {
		byte[] xyz = new byte[6];
		Tuple3f data = new Tuple3f();
		i2cDevice.write((byte) (REGISTER_OUT_X_L | 0x80));
		i2cDevice.read(REGISTER_OUT_X_H | 0x80, xyz, 0, 6);
		int x = (xyz[1] & 0xFF | (xyz[0] << 8));
		int y = (xyz[3] & 0xFF | (xyz[2] << 8));
		int z = (xyz[5] & 0xFF | (xyz[4] << 8));
		data.x = x * sensitivity.getSensitivityFactor();
		data.y = y * sensitivity.getSensitivityFactor();
		data.z = z * sensitivity.getSensitivityFactor();
		return data;
	}

}
