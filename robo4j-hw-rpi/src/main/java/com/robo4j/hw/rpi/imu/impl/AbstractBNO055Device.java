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
package com.robo4j.hw.rpi.imu.impl;

import com.robo4j.hw.rpi.imu.BNO055CalibrationStatus;
import com.robo4j.hw.rpi.imu.BNO055Device;
import com.robo4j.hw.rpi.imu.BNO055SelfTestResult;
import com.robo4j.hw.rpi.imu.BNO055SystemStatus;
import com.robo4j.math.geometry.Tuple3f;
import com.robo4j.math.geometry.Tuple4d;

import java.io.IOException;

/**
 * Abstraction for a BN0055 absolute orientation device.
 * 
 * NOTE(Marcus/Jul 27, 2017): Note that the Rasperry Pi does not properly
 * support clock stretching yet (Raspberry Pi 3), so this particular class does
 * not work until that has been fixed in the i2c implementation on the Raspberry
 * Pi. Until then, wire the BNO055 to use serial tty communication as described
 * in that data sheet, and use the com.robo4j.hw.rpi.serial.imu classes instead.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public abstract class AbstractBNO055Device implements BNO055Device {
	private static final int WAIT_STEP_MILLIS = 5;
	// Spec 3.6.5.5
	private static final float QUAT_SCALE = (float) (1.0 / (1 << 14));
	// Registers
	private static final int REGISTER_CHIP_ID = 0x00;
	private static final int REGISTER_PAGE_ID = 0x07;
	private static final int REGISTER_SYS_TRIGGER = 0x3F;
	private static final int REGISTER_PWR_MODE = 0x3E;
	private static final int REGISTER_OPR_MODE = 0x3D;
	private static final int REGISTER_UNIT_SELECT = 0x3B;
	private static final int REGISTER_ST_RESULT = 0x36;
	private static final int REGISTER_CALIB_STAT = 0x35;
	private static final int REGISTER_TEMP = 0x34;

	// Gravity vector
	private static final int REGISTER_GRV_DATA_X = 0x2E;
	// Linear acceleration
	private static final int REGISTER_LIA_DATA_X = 0x28;
	// Quaternion w, x, y, z
	private static final int REGISTER_QUA_DATA_W = 0x20;
	private static final int REGISTER_EUL_DATA_X = 0x1A;
	private static final int REGISTER_GYR_DATA_X = 0x14;
	private static final int REGISTER_MAG_DATA_X = 0x0E;
	private static final int REGISTER_ACC_DATA_X = 0x08;

	private static final int REGISTER_SYS_ERR = 0x3A;
	private static final int REGISTER_SYS_STATUS = 0x39;

	// The constant value of the chip id register for the BNO055
	private static final byte CHIP_ID_VALUE = (byte) 0xA0;

	// Caching these to minimize the I2C traffic. Assumes that noone else is
	// tinkering with the hardware.
	private Unit currentAccelerationUnit = Unit.M_PER_S_SQUARED;
	private Unit currentAngularRateUnit = Unit.DEGREES_PER_SECOND;
	private Unit currentEuelerAngleUnit = Unit.DEGREES;
	private Unit currentTemperatureUnit = Unit.CELCIUS;
	private OrientationMode currentOrientation = OrientationMode.Windows;

	/**
	 * Creates a BNO055Device with the default settings.
	 * 
	 * @throws IOException
	 *             exception
	 * 
	 * @see PowerMode
	 * @see OperatingMode
	 */
	public AbstractBNO055Device() throws IOException {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.robo4j.hw.rpi.i2c.imu.BNO055Device#setOperatingMode(com.robo4j.hw.rpi
	 * .i2c.imu.BNO055I2CDevice.OperatingMode)
	 */
	@Override
	public void setOperatingMode(OperatingMode operatingMode) throws IOException {
		write(REGISTER_OPR_MODE, operatingMode.getCtrlCode());
		waitForOk(20);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#getOperatingMode()
	 */
	@Override
	public OperatingMode getOperatingMode() throws IOException {
		return OperatingMode.fromCtrlCode(read(REGISTER_OPR_MODE) & 0x0F);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.robo4j.hw.rpi.i2c.imu.BNO055Device#setPowerMode(com.robo4j.hw.rpi.i2c
	 * .imu.BNO055I2CDevice.PowerMode)
	 */
	@Override
	public void setPowerMode(PowerMode powerMode) throws IOException {
		write(REGISTER_PWR_MODE, powerMode.getCtrlCode());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#getPowerMode()
	 */
	@Override
	public PowerMode getPowerMode() throws IOException {
		return PowerMode.fromCtrlCode(read(REGISTER_PWR_MODE));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#getCalibrationStatus()
	 */
	@Override
	public BNO055CalibrationStatus getCalibrationStatus() throws IOException {
		return new BNO055CalibrationStatus(read(REGISTER_CALIB_STAT));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#getSystemStatus()
	 */
	@Override
	public BNO055SystemStatus getSystemStatus() throws IOException {
		return new BNO055SystemStatus(read(REGISTER_SYS_STATUS));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#getTemperature()
	 */
	@Override
	public float getTemperature() throws IOException {
		return read(REGISTER_TEMP) / currentTemperatureUnit.getFactor();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#performSelfTest()
	 */
	@Override
	public BNO055SelfTestResult performSelfTest() throws IOException {
		OperatingMode previousOperatingMode = getOperatingMode();
		if (previousOperatingMode != OperatingMode.CONFIG) {
			setOperatingMode(OperatingMode.CONFIG);
			sleep(20);
		}
		write(REGISTER_SYS_TRIGGER, (byte) 0x8);
		sleep(20);
		int resultCode = read(REGISTER_ST_RESULT) & 0x0F;
		int errorCode = 0;
		if (resultCode != 0x0F) {
			errorCode = read(REGISTER_SYS_ERR);
		}
		BNO055SelfTestResult result = new BNO055SelfTestResult(resultCode, errorCode);
		if (previousOperatingMode != OperatingMode.CONFIG) {
			setOperatingMode(previousOperatingMode);
			sleep(20);
		}
		return result;
	}

	/**
	 * Reads a byte (signed or unsigned) from the provided register.
	 * 
	 * @param register
	 *            the address of the register to read from.
	 * @return the value read.
	 * @throws IOException
	 *             exception
	 */
	protected abstract int read(int register) throws IOException;

	/**
	 * Writes the provided byte into the register address.
	 * 
	 * @param register
	 *            the register to write to.
	 * @param b
	 *            the byte to write.
	 * @throws IOException
	 *             exception
	 */
	protected abstract void write(int register, byte b) throws IOException;

	protected void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.robo4j.hw.rpi.i2c.imu.BNO055Device#setUnits(com.robo4j.hw.rpi.i2c.imu
	 * .BNO055I2CDevice.Unit, com.robo4j.hw.rpi.i2c.imu.BNO055I2CDevice.Unit,
	 * com.robo4j.hw.rpi.i2c.imu.BNO055I2CDevice.Unit,
	 * com.robo4j.hw.rpi.i2c.imu.BNO055I2CDevice.Unit,
	 * com.robo4j.hw.rpi.i2c.imu.BNO055I2CDevice.Orientation)
	 */
	@Override
	public void setUnits(Unit accelerationUnit, Unit angularRateUnit, Unit angleUnit, Unit temperatureUnit,
			OrientationMode orientationMode) throws IOException {
		int val = 0;
		if (accelerationUnit == Unit.MILI_G) {
			val |= 0x01;
		} else if (accelerationUnit != Unit.MICROTESLA) {
			throw new IllegalArgumentException(accelerationUnit + " is not an acceleration unit!");
		}
		if (angularRateUnit == Unit.RADIANS_PER_SECOND) {
			val |= 0x02;
		} else if (angularRateUnit != Unit.DEGREES_PER_SECOND) {
			throw new IllegalArgumentException(angularRateUnit + " is not an angular rate unit!");
		}
		if (angleUnit == Unit.RADIANS) {
			val |= 0x04;
		} else if (angleUnit != Unit.DEGREES) {
			throw new IllegalArgumentException(angleUnit + " is not an angle unit!");
		}
		if (temperatureUnit == Unit.FAHRENHEIT) {
			val |= 0x10;
		} else if (temperatureUnit != Unit.CELCIUS) {
			throw new IllegalArgumentException(temperatureUnit + " is not a temperature unit!");
		}
		if (orientationMode == OrientationMode.Android) {
			val |= 0x80;
		}
		write(REGISTER_UNIT_SELECT, (byte) val);
		currentAccelerationUnit = accelerationUnit;
		currentAngularRateUnit = angularRateUnit;
		currentEuelerAngleUnit = angleUnit;
		currentTemperatureUnit = temperatureUnit;
		currentOrientation = orientationMode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#read()
	 */
	@Override
	public Tuple3f read() throws IOException {
		return readVector(REGISTER_EUL_DATA_X, currentEuelerAngleUnit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#readEulerAngles()
	 */
	@Override
	public Tuple3f readEulerAngles() throws IOException {
		return read();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#readMagnetometer()
	 */
	@Override
	public Tuple3f readMagnetometer() throws IOException {
		return readVector(REGISTER_MAG_DATA_X, Unit.MICROTESLA);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#readAccelerometer()
	 */
	@Override
	public Tuple3f readAccelerometer() throws IOException {
		return readVector(REGISTER_ACC_DATA_X, currentAccelerationUnit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#readGyro()
	 */
	@Override
	public Tuple3f readGyro() throws IOException {
		return readVector(REGISTER_GYR_DATA_X, currentAngularRateUnit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#readQuaternion()
	 */
	@Override
	public Tuple4d readQuaternion() throws IOException {
		return readQuaternion(REGISTER_QUA_DATA_W);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#readLinearAcceleration()
	 */
	@Override
	public Tuple3f readLinearAcceleration() throws IOException {
		return readVector(REGISTER_LIA_DATA_X, currentAccelerationUnit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#readGravityVector()
	 */
	@Override
	public Tuple3f readGravityVector() throws IOException {
		return readVector(REGISTER_GRV_DATA_X, currentAccelerationUnit);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#reset()
	 */
	@Override
	public void reset() throws IOException {
		write(REGISTER_SYS_TRIGGER, (byte) 0x20);
		sleep(650);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.robo4j.hw.rpi.i2c.imu.BNO055Device#getCurrentOrientation()
	 */
	@Override
	public OrientationMode getCurrentOrientation() {
		return currentOrientation;
	}

	/**
	 * Will wait until the system is ready, an error occurs or the max wait was
	 * exceeded.
	 * 
	 * @param maxWaitTimeMillis
	 *            max time to wait for the system to be ready.
	 * @return returns true if the system is ready, false if the system is reporting
	 *         an error or if the timeout was reached.
	 * @throws IOException
	 */
	private boolean waitForOk(int maxWaitTimeMillis) throws IOException {
		int waitTime = 0;
		while (true) {
			BNO055SystemStatus systemStatus = getSystemStatus();
			if (systemStatus.isReady() || systemStatus.hasError() || waitTime >= maxWaitTimeMillis) {
				if (systemStatus.isReady()) {
					return true;
				} else {
					return false;
				}
			}
			sleep(WAIT_STEP_MILLIS);
			waitTime += WAIT_STEP_MILLIS;
		}
	}

	protected void initialize(OperatingMode operatingMode) throws IOException {
		// First check that we are really communicating with the BNO.
		if (read(REGISTER_CHIP_ID) != CHIP_ID_VALUE) {
			throw new IOException("Not a BNO connected to the defined endpoint!");
		}

		try {
			write(REGISTER_PAGE_ID, (byte) 0);
		} catch (IOException ioe) {
			// Seems sometimes the first one fails, so just ignore.
		}
		write(REGISTER_PAGE_ID, (byte) 0);
		// This may be a bit unnecessary, but let's make sure we are in config
		// first.
		OperatingMode currentOperatingMode = getOperatingMode();
		if (currentOperatingMode != OperatingMode.CONFIG) {
			setOperatingMode(OperatingMode.CONFIG);
			waitForOk(20);
		}
		setUseExternalChrystal(true);
		if (currentOperatingMode != operatingMode) {
			setOperatingMode(operatingMode);
		}
	}

	@Override
	public void setUseExternalChrystal(boolean useExternalChrystal) throws IOException {
		write(REGISTER_SYS_TRIGGER, useExternalChrystal ? (byte) 0x80 : 0x00);
	}

	@Override
	public boolean isUseExternalChrystal() throws IOException {
		return (read(REGISTER_SYS_TRIGGER) & 0x80) > 0;
	}

	private Tuple3f readVector(int register, Unit unit) throws IOException {
		byte[] data = read(register, 6);
		Tuple3f tuple = new Tuple3f();
		tuple.x = read16bitSigned(data, 0);
		tuple.y = read16bitSigned(data, 2);
		tuple.z = read16bitSigned(data, 4);
		if (unit.getFactor() != 1f) {
			tuple.x /= unit.getFactor();
			tuple.y /= unit.getFactor();
			tuple.z /= unit.getFactor();
		}
		return tuple;
	}

	/**
	 * Reads n values from the provided register.
	 * 
	 * @param register
	 *            the register to read
	 * @param length
	 *            the total length of values to read.
	 * @return bytes
	 * @throws IOException
	 *             exception
	 */
	protected abstract byte[] read(int register, int length) throws IOException;

	protected Tuple4d readQuaternion(int register) throws IOException {
		byte[] data = read(register, 8);
		Tuple4d tuple = new Tuple4d();
		tuple.t = read16bitSigned(data, 0);
		tuple.x = read16bitSigned(data, 2);
		tuple.y = read16bitSigned(data, 4);
		tuple.z = read16bitSigned(data, 6);
		tuple.t *= QUAT_SCALE;
		tuple.x *= QUAT_SCALE;
		tuple.y *= QUAT_SCALE;
		tuple.z *= QUAT_SCALE;
		return tuple;
	}

	private short read16bitSigned(byte[] data, int offset) {
		return (short) (data[offset + 1] << 8 | (data[offset] & 0xFF));
	}
}
