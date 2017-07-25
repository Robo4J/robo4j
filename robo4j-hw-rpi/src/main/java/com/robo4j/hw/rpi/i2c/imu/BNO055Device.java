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
package com.robo4j.hw.rpi.i2c.imu;

import java.io.IOException;

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.i2c.ReadableDevice;
import com.robo4j.math.geometry.Tuple3f;

/**
 * Abstraction for a BN0055 absolute orientation device.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class BNO055Device extends AbstractI2CDevice implements ReadableDevice<Tuple3f> {
	private static final int DEFAULT_I2C_ADDRESS = 0x28;
	// Registers
	private static final int REGISTER_SYS_TRIGGER = 0x3F;
	private static final int REGISTER_PWR_MODE = 0x3E;
	private static final int REGISTER_OPR_MODE = 0x3D;
	private static final int REGISTER_UNIT_SELECT = 0x3B;
	private static final int REGISTER_ST_RESULT = 0x36;
	private static final int REGISTER_CALIB_STAT = 0x35;
	private static final int REGISTER_TEMP = 0x34;

	private static final int REGISTER_EUL_DATA_X = 0x1A;

	/**
	 * The power mode can be used to
	 */
	public enum PowerMode {
		//@formatter:off
		/**
		 * All sensors for the selected {@link OperatingMode} are always switched on.
		 */
		NORMAL	(0x0),
		/**
		 * If there is no activity for a configurable duration (default 5s), the BNO enters the LOW_POWER mode. 
		 * In this mode only the accelerometer is active. Once motion is detected, the system is woken up and 
		 * NORMAL mode is entered.
		 */
		LOW_POWER		(0x1),
		/**
		 * In SUSPEND mode the system is paused and all sensors and the micro-controller is put into sleep mode.
		 * No values in the register map will be updated in this mode. To exit SUSPEND, setPowerMode() to something other than SUSPEND.
		 */
		SUSPEND (0x2);		
		//@formatter:on
		private byte ctrlCode;

		PowerMode(int ctrlCode) {
			this.ctrlCode = (byte) ctrlCode;
		}

		public byte getCtrlCode() {
			return ctrlCode;
		}

		public static PowerMode fromCtrlCode(int ctrlCode) {
			for (PowerMode mode : values()) {
				if (ctrlCode == mode.getCtrlCode()) {
					return mode;
				}
			}
			return null;
		}
	}

	/**
	 * The operation mode is used to configure how the BNO shall operate. The
	 * modes are described in the BNO055 data sheet.
	 * 
	 * Note that default initialization on power on for the chip will be CONFIG,
	 * but that, if the default constructor is chosen, the BNO055Device will use
	 * NDOF by default.
	 */
	public enum OperatingMode {
		//@formatter:off
		CONFIG	(0x0),
		// Non fusion modes
		ACCONLY (0x1),
		MAGONLY (0x2),
		GYROONLY (0x3),
		ACCMAG (0x4),
		ACCGYRO (0x5),
		MAGGYRO (0x6),
		AMG (0x7),
		// Fusion modes
		IMU (0x8),
		COMPASS (0x9),
		M4G (0xA),
		NDOF_FMC_OFF (0xB),
		NDOF (0xC);
		//@formatter:on
		private byte ctrlCode;

		OperatingMode(int ctrlCode) {
			this.ctrlCode = (byte) ctrlCode;
		}

		public byte getCtrlCode() {
			return ctrlCode;
		}

		public static OperatingMode fromCtrlCode(int ctrlCode) {
			for (OperatingMode mode : values()) {
				if (mode.getCtrlCode() == ctrlCode) {
					return mode;
				}
			}
			return null;
		}
	}

	public enum UnitsAcceleration {
		M_PER_S_SQUARED, MILI_G
	}

	public enum UnitsAngularRate {
		DEGREES_PER_SECOND, RADIANS_PER_SECOND
	}

	public enum UnitsEulerAngles {
		DEGREES, RADIANS
	}

	public enum UnitsTemperature {
		Celcius, Fahrenheit
	}

	public enum Orientation {
		Windows, Android
	}

	/**
	 * Creates a BNO055Device with the default settings.
	 * 
	 * @throws IOException
	 * 
	 * @see PowerMode
	 * @see OperatingMode
	 */
	public BNO055Device() throws IOException {
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS, PowerMode.NORMAL, OperatingMode.NDOF);
	}

	/**
	 * Creates a BNO055Device with the provided explicit settings.
	 * 
	 * @param bus
	 *            the i2c bus on which the BNO is.
	 * @param address
	 *            the address to which the BNO is configured.
	 * @param powerMode
	 *            the {@link PowerMode} to initialize to.
	 * @param operatingMode
	 *            the {@link OperatingMode} to initialize to.
	 * @throws IOException
	 */
	public BNO055Device(int bus, int address, PowerMode powerMode, OperatingMode operatingMode) throws IOException {
		super(bus, address);
		initialize(powerMode, operatingMode);
	}

	public void setOperatingMode(OperatingMode operatingMode) throws IOException {
		i2cDevice.write(REGISTER_OPR_MODE, operatingMode.getCtrlCode());
	}

	public OperatingMode getOperatingMode() throws IOException {
		return OperatingMode.fromCtrlCode(i2cDevice.read(REGISTER_OPR_MODE));
	}

	public void setPowerMode(PowerMode powerMode) throws IOException {
		i2cDevice.write(REGISTER_PWR_MODE, powerMode.getCtrlCode());
	}

	public PowerMode getPowerMode() throws IOException {
		return PowerMode.fromCtrlCode(i2cDevice.read(REGISTER_PWR_MODE));
	}

	/**
	 * @return the current calibration status.
	 * 
	 * @see BNO055CalibrationStatus
	 */
	public BNO055CalibrationStatus getCalibrationStatus() throws IOException {
		return new BNO055CalibrationStatus(i2cDevice.read(REGISTER_CALIB_STAT));
	}

	/**
	 * Returns the temperature. Note that is the temperature unit is set to
	 * Fahrenheit, then multiply the result by 2.
	 * 
	 * @return the temperature.
	 * @throws IOException
	 */
	public byte getTemperature() throws IOException {
		return (byte) i2cDevice.read(REGISTER_TEMP);
	}

	private void initialize(PowerMode powerMode, OperatingMode operatingMode) throws IOException {
		setPowerMode(powerMode);
		setOperatingMode(operatingMode);
	}

	/**
	 * Runs a self test, performing the necessary mode changes as needed. Note
	 * that this operating can block the calling thread for a little while.
	 * 
	 * @return the result of the self test.
	 * @throws IOException
	 */
	public BNO055SelfTestResult performSelfTest() throws IOException {
		OperatingMode previousOperatingMode = getOperatingMode();
		if (previousOperatingMode != OperatingMode.CONFIG) {
			setOperatingMode(OperatingMode.CONFIG);
			sleep(20);
		}
		i2cDevice.write(REGISTER_SYS_TRIGGER, (byte) 0x8);
		sleep(20);
		BNO055SelfTestResult result = new BNO055SelfTestResult(i2cDevice.read(REGISTER_ST_RESULT));
		if (previousOperatingMode != OperatingMode.CONFIG) {
			setOperatingMode(previousOperatingMode);
			sleep(20);
		}
		return result;
	}

	/**
	 * Sets the units to be used. By default m/s^2, angular degrees per second,
	 * angular degrees, celsius and windows orientation will be used.
	 * 
	 * @param accelerationUnit
	 * @param rate
	 * @param angles
	 * @param temp
	 * @param orientation
	 * @throws IOException
	 */
	public void setUnits(UnitsAcceleration accelerationUnit, UnitsAngularRate rate, UnitsEulerAngles angles, UnitsTemperature temp,
			Orientation orientation) throws IOException {
		int val = 0;
		if (accelerationUnit == UnitsAcceleration.MILI_G) {
			val |= 0x01;
		}
		if (rate == UnitsAngularRate.RADIANS_PER_SECOND) {
			val |= 0x02;
		}
		if (angles == UnitsEulerAngles.RADIANS) {
			val |= 0x04;
		}
		if (temp == UnitsTemperature.Fahrenheit) {
			val |= 0x10;
		}
		if (orientation == Orientation.Android) {
			val |= 0x80;
		}
		i2cDevice.write(REGISTER_UNIT_SELECT, (byte) val);
	}

	/**
	 * This method returns the absolute orientation: X will contain heading, Y
	 * will contain roll data, Z will contain pitch. The units will be the ones
	 * selected with
	 * {@link #setUnits(UnitsAcceleration, UnitsAngularRate, UnitsEulerAngles, UnitsTemperature, Orientation)}.
	 */
	@Override
	public Tuple3f read() throws IOException {
		byte[] eulerVals = new byte[6];
		i2cDevice.read(REGISTER_EUL_DATA_X, eulerVals, 0, 6);
		Tuple3f tuple = new Tuple3f();
		tuple.x = read16bitSigned(eulerVals, 0);
		tuple.y = read16bitSigned(eulerVals, 2);
		tuple.z = read16bitSigned(eulerVals, 4);
		return tuple;
	}

	private short read16bitSigned(byte[] data, int offset) {
		int n = ((data[offset + 1] & 0xFF) << 8 | (data[offset] & 0xFF));
		return (short) (n < 32768 ? n : n - 65536);
	}
}
