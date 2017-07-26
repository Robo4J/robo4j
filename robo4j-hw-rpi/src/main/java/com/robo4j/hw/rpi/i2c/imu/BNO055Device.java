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
import com.robo4j.math.geometry.Tuple4f;

/**
 * Abstraction for a BN0055 absolute orientation device.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class BNO055Device extends AbstractI2CDevice implements ReadableDevice<Tuple3f> {
	private static final int WAIT_STEP_MILLIS = 5;
	// Spec 3.6.5.5
	private static final float QUAT_SCALE = (float) (1.0 / (1 << 14));
	private static final int DEFAULT_I2C_ADDRESS = 0x28;
	// Registers
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

	// Caching these to minimize the I2C traffic. Assumes that noone else is
	// tinkering with the hardware.
	private Unit currentAccelerationUnit = Unit.M_PER_S_SQUARED;
	private Unit currentAngularRateUnit = Unit.DEGREES_PER_SECOND;
	private Unit currentEuelerAngleUnit = Unit.DEGREES;
	private Unit currentTemperatureUnit = Unit.CELCIUS;
	private Orientation currentOrientation = Orientation.Windows;

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

	public enum Unit {
		/**
		 * Acceleration unit (meter per seconds squared)
		 */
		M_PER_S_SQUARED(100f),
		/**
		 * Acceleration unit (milli g)
		 */
		MILI_G(1f),
		/**
		 * Angular rate unit (angular degrees per second)
		 */
		DEGREES_PER_SECOND(16f),
		/**
		 * Angular rate unit (radians per second)
		 */
		RADIANS_PER_SECOND(900f),
		/**
		 * Angular unit (angular degrees)
		 */
		DEGREES(16f),
		/**
		 * Angular unit (radians)
		 */
		RADIANS(900f),
		/**
		 * Magnetic field strength unit (micro Tesla)
		 */
		MICROTESLA(16f),
		/**
		 * Temperature unit (Celcius)
		 */
		CELCIUS(1f),
		/**
		 * Temperature unit (Fahrenheit)
		 */
		FAHRENHEIT(2f);
		private float factor;

		Unit(float factor) {
			this.factor = factor;
		}

		public float getFactor() {
			return factor;
		}
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
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS, OperatingMode.NDOF);
	}

	/**
	 * Creates a BNO055Device with the provided explicit settings.
	 * 
	 * @param bus
	 *            the i2c bus on which the BNO is.
	 * @param address
	 *            the address to which the BNO is configured.
	 * @param operatingMode
	 *            the {@link OperatingMode} to initialize to.
	 * @throws IOException
	 */
	public BNO055Device(int bus, int address, OperatingMode operatingMode) throws IOException {
		super(bus, address);
		initialize(operatingMode);
	}

	public void setOperatingMode(OperatingMode operatingMode) throws IOException {
		i2cDevice.write(REGISTER_OPR_MODE, operatingMode.getCtrlCode());
		waitForOk(20);
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
	 * @return the system status.
	 * @throws IOException
	 */
	public BNO055SystemStatus getSystemStatus() throws IOException {
		return BNO055SystemStatus.fromErrorCode(i2cDevice.read(REGISTER_SYS_STATUS));
	}

	/**
	 * Returns the temperature.
	 * 
	 * @return the temperature.
	 * @throws IOException
	 */
	public float getTemperature() throws IOException {
		return i2cDevice.read(REGISTER_TEMP) / currentTemperatureUnit.getFactor();
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
		int resultCode = i2cDevice.read(REGISTER_ST_RESULT);
		int errorCode = 0;
		if (resultCode != 0) {
			errorCode = i2cDevice.read(REGISTER_SYS_ERR);
		}
		BNO055SelfTestResult result = new BNO055SelfTestResult(resultCode, errorCode);
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
	 * @param angularRateUnit
	 * @param angleUnit
	 * @param temperatureUnit
	 * @param orientation
	 * @throws IOException
	 */
	public void setUnits(Unit accelerationUnit, Unit angularRateUnit, Unit angleUnit, Unit temperatureUnit, Orientation orientation)
			throws IOException {
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
		if (orientation == Orientation.Android) {
			val |= 0x80;
		}
		i2cDevice.write(REGISTER_UNIT_SELECT, (byte) val);
		currentAccelerationUnit = accelerationUnit;
		currentAngularRateUnit = angularRateUnit;
		currentEuelerAngleUnit = angleUnit;
		currentTemperatureUnit = temperatureUnit;
		currentOrientation = orientation;
	}

	/**
	 * This method returns the absolute orientation: X will contain heading, Y
	 * will contain roll data, Z will contain pitch. The units will be the ones
	 * selected with
	 * {@link #setUnits(AccelerationUnit, AngularRateUnit, EulerAngleUnit, TemperatureUnit, Orientation)}.
	 */
	@Override
	public Tuple3f read() throws IOException {
		return readVector(REGISTER_EUL_DATA_X, currentEuelerAngleUnit);
	}

	/**
	 * The fusion algorithm offset and tilt compensated absolute orientation
	 * data in Euler angles. x = magnetic heading, y = roll, z = pitch. Note
	 * that this is only available in one of the fusion modes.
	 * 
	 * @return a tuple containing x = magnetic heading, y = roll, z = pitch.
	 * @throws IOException
	 */
	public Tuple3f readEulerAngles() throws IOException {
		return read();
	}

	/**
	 * @return the magnetometer values in x,y,z in {@link Unit#MICROTESLA}.
	 * @throws IOException
	 */
	public Tuple3f readMagnetometer() throws IOException {
		return readVector(REGISTER_MAG_DATA_X, Unit.MICROTESLA);
	}

	/**
	 * @return the accelerometer information for the accelerometer in the
	 *         currently set acceleration unit.
	 * @throws IOException
	 */
	public Tuple3f readAccelerometer() throws IOException {
		return readVector(REGISTER_ACC_DATA_X, currentAccelerationUnit);
	}

	/**
	 * @return the gyro information in the currently set angular rate unit.
	 * @throws IOException
	 */
	public Tuple3f readGyro() throws IOException {
		return readVector(REGISTER_GYR_DATA_X, currentAngularRateUnit);
	}

	/**
	 * Reads the quaternion data for the absolute orientation into a Tuple4f.
	 * Note that w will be in the t field.
	 * 
	 * @return the quaternion data for the absolute orientation.
	 * @throws IOException
	 */
	public Tuple4f readQuaternion() throws IOException {
		return readQuaternion(REGISTER_QUA_DATA_W);
	}

	/**
	 * The fusion algorithm output for the linear acceleration data for each
	 * axis in currently set acceleration unit. Note that this is only available
	 * in one of the fusion modes.
	 * 
	 * @return the fusion algorithm output for the linear acceleration data for
	 *         each axis in currently set acceleration unit.
	 * @throws IOException
	 */
	public Tuple3f readLinearAcceleration() throws IOException {
		return readVector(REGISTER_LIA_DATA_X, currentAccelerationUnit);
	}

	/**
	 * The fusion algorithm output for the gravity vector in the currently set
	 * acceleration unit. Note that this is only available in one of the fusion
	 * modes.
	 * 
	 * @return the fusion algorithm output for the gravity vector in the
	 *         currently set acceleration unit.
	 * @throws IOException
	 */
	public Tuple3f readGravityVector() throws IOException {
		return readVector(REGISTER_GRV_DATA_X, currentAccelerationUnit);
	}

	/**
	 * Will attempt to reset the device.
	 * @throws IOException
	 */
	public void reset() throws IOException {
		i2cDevice.write(REGISTER_SYS_TRIGGER, (byte) 0x8);
		waitForOk(50);
	}

	public Orientation getCurrentOrientation() {
		return currentOrientation;
	}

	private void waitForOk(int maxWaitTimeMillis) throws IOException {
		int waitTime = 0;
		while (true) {
			BNO055SystemStatus systemStatus = getSystemStatus();
			if (systemStatus == BNO055SystemStatus.IDLE || systemStatus == BNO055SystemStatus.RUNNING_NO_SENSOR_FUSION
					|| systemStatus == BNO055SystemStatus.RUNNING_SENSOR_FUSION || waitTime >= maxWaitTimeMillis) {
				break;
			}
			sleep(WAIT_STEP_MILLIS);
			waitTime += WAIT_STEP_MILLIS;
		}
	}

	private void initialize(OperatingMode operatingMode) throws IOException {
		setOperatingMode(operatingMode);
	}

	private Tuple3f readVector(int register, Unit unit) throws IOException {
		byte[] data = new byte[6];
		i2cDevice.read(register, data, 0, data.length);
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

	private Tuple4f readQuaternion(int register) throws IOException {
		byte[] data = new byte[8];
		i2cDevice.read(register, data, 0, data.length);
		Tuple4f tuple = new Tuple4f();
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
		return (short) (data[offset + 1] << 8 | data[offset]);
	}
}
