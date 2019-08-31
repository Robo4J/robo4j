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
package com.robo4j.hw.rpi.imu.bno;

import com.robo4j.math.geometry.Tuple3f;
import com.robo4j.math.geometry.Tuple4d;

import java.io.IOException;

/**
 * Interface for the BNO055 Devices. This is shared between the I2C and Serial
 * implementations.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface Bno055Device {
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
	 * The operation mode is used to configure how the BNO shall operate. The modes
	 * are described in the BNO055 data sheet.
	 * 
	 * Note that default initialization on power on for the chip will be CONFIG, but
	 * that, if the default constructor is chosen, the BNO055Device will use NDOF by
	 * default.
	 */
	public enum OperatingMode {
		//@formatter:off
		/**
		 * This mode is used to configure BNO, wherein all output data is reset to zero and sensor
		 * fusion is halted. This is the only mode in which all the writable register map entries can be
		 * changed. (Exceptions from this rule are the interrupt registers (INT and INT_MSK) and the
		 * operation mode register (OPR_MODE), which can be modified in any operation mode.)
		 */
		CONFIG	(0x0),
		// Non fusion modes
		/**
		 * If the application requires only raw accelerometer data, this mode can be chosen. In this
		 * mode the other sensors (magnetometer, gyro) are suspended to lower the power
 		 * consumption. In this mode, the BNO055 behaves like a stand-alone acceleration sensor.
		 */
		ACCONLY (0x1),
		/**
		 * In MAGONLY mode, the BNO055 behaves like a stand-alone magnetometer, with
		 * acceleration sensor and gyroscope being suspended.
		 */
		MAGONLY (0x2),
		/**
		 * In GYROONLY mode, the BNO055 behaves like a stand-alone gyroscope, with acceleration
		 * sensor and magnetometer being suspended.
		 */
		GYROONLY (0x3),
		/**
		 * Both accelerometer and magnetometer are switched on, the user can read the data from
		 * these two sensors.
		 */
		ACCMAG (0x4),
		/** 
		 * Both accelerometer and gyroscope are switched on; the user can read the data from these
		 * two sensors.
		 */
		ACCGYRO (0x5),
		/**
		 * Both magnetometer and gyroscope are switched on, the user can read the data from these
		 * two sensors.
		 */
		MAGGYRO (0x6),
		/**
		 * All three sensors accelerometer, magnetometer and gyroscope are switched on.
		 */
		AMG (0x7),
		// Fusion modes
		/**
		 * In the IMU mode the relative orientation of the BNO055 in space is calculated from the
		 * accelerometer and gyroscope data. The calculation is fast (i.e. high output data rate).
		 */
		IMU (0x8),
		/**
		 * The COMPASS mode is intended to measure the magnetic earth field and calculate the
		 * geographic direction.
		 * The earth magnetic field is a vector with the horizontal components x,y and the vertical z
		 * component. It depends on the position on the globe and natural iron occurrence. For
		 * heading calculation (direction of compass pointer) only the horizontal components x and y
		 * are used. Therefore the vector components of the earth magnetic field must be transformed
		 * in the horizontal plane, which requires the knowledge of the direction of the gravity vector.
		 * To summarize, the heading can only be calculated when considering gravity and magnetic
		 * field at the same time.
		 * However, the measurement accuracy depends on the stability of the surrounding magnetic
		 * field. Furthermore, since the earth magnetic field is usually much smaller than the magnetic
		 * fields that occur around and inside electronic devices, the compass mode requires
		 * calibration.
		 */
		COMPASS (0x9),
		/**
		 * The M4G mode is similar to the IMU mode, but instead of using the gyroscope signal to
		 * detect rotation, the changing orientation of the magnetometer in the magnetic field is used.
		 * Since the magnetometer has much lower power consumption than the gyroscope, this mode
		 * is less power consuming in comparison to the IMU mode. There are no drift effects in this
		 * mode which are inherent to the gyroscope.
		 * However, as for compass mode, the measurement accuracy depends on the stability of the
		 * surrounding magnetic field.
		 * For this mode no magnetometer calibration is required and also not available.
		 */
		M4G (0xA),
		/**
		 * This fusion mode is same as NDOF mode, but with the Fast Magnetometer Calibration
		 * turned ‘OFF’.
		 */
		NDOF_FMC_OFF (0xB),
		/**
		 * This is a fusion mode with 9 degrees of freedom where the fused absolute orientation data
		 * is calculated from accelerometer, gyroscope and the magnetometer. The advantages of
		 * combining all three sensors are a fast calculation, resulting in high output data rate, and high
		 * robustness from magnetic field distortions. In this mode the Fast Magnetometer calibration
		 * is turned ON and thereby resulting in quick calibration of the magnetometer and higher
		 * output data accuracy. The current consumption is slightly higher in comparison to the
		 * NDOF_FMC_OFF fusion mode.
		 */
		NDOF (0xC),
		/**
		 * Reading this operating mode is indicative of an error.
		 */
		UNKNOWN(-1);
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
			return OperatingMode.UNKNOWN;
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

	/**
	 * Describes how Euler angle results are defined.
	 */
	public enum OrientationMode {
		/**
		 * Pitch: -180° to +180° (turning clockwise increases values) Roll: -90° to +90°
		 * (increasing with increasing inclination) Heading (Yaw): 0° to 360° (turning
		 * clockwise increases values)
		 */
		Windows,
		/**
		 * Pitch: +180° to -180° (turning clockwise decreases values) Roll: -90° to +90°
		 * (increasing with increasing inclination) Heading (Yaw): 0° to 360° (turning
		 * clockwise increases values)
		 */
		Android
	}

	/**
	 * Sets the device {@link OperatingMode}. Note that {@link OperatingMode#CONFIG}
	 * is required for writing many registers.
	 * 
	 * @param operatingMode
	 *            the OperatingMode to set.
	 * @throws IOException
	 *             exception
	 */
	void setOperatingMode(OperatingMode operatingMode) throws IOException;

	/**
	 * Returns the device {@link OperatingMode} currently in use.
	 * 
	 * @return the current {@link OperatingMode}.
	 * @throws IOException
	 *             exception
	 */
	OperatingMode getOperatingMode() throws IOException;

	/**
	 * Sets the {@link PowerMode} of the device.
	 * 
	 * @param powerMode
	 *            the current {@link PowerMode}
	 * @throws IOException
	 *             exception
	 */
	void setPowerMode(PowerMode powerMode) throws IOException;

	/**
	 * Returns the current {@link PowerMode} of the device. Note that this can
	 * change automatically depending on the device configuration. For example, if
	 * the device has not been disturbed for 5 minutes.
	 * 
	 * @return the current {@link PowerMode} of the device
	 * @throws IOException
	 *             exception
	 */
	PowerMode getPowerMode() throws IOException;

	/**
	 * @return the current calibration status.
	 * @throws IOException
	 *             exception
	 * 
	 * @see Bno055CalibrationStatus
	 */
	Bno055CalibrationStatus getCalibrationStatus() throws IOException;

	/**
	 * @return the system status.
	 * @throws IOException
	 *             exception
	 */
	Bno055SystemStatus getSystemStatus() throws IOException;

	/**
	 * Returns the temperature.
	 * 
	 * @return the temperature.
	 * @throws IOException
	 *             exception
	 */
	float getTemperature() throws IOException;

	/**
	 * Runs a self test, performing the necessary mode changes as needed. Note that
	 * this operating can block the calling thread for a little while.
	 * 
	 * @return the result of the self test.
	 * @throws IOException
	 *             exception
	 */
	Bno055SelfTestResult performSelfTest() throws IOException;

	/**
	 * Sets the units to be used. By default m/s^2, angular degrees per second,
	 * angular degrees, celsius and windows orientation will be used.
	 * 
	 * @param accelerationUnit
	 *            acceleration unit
	 * @param angularRateUnit
	 *            angular rate unit
	 * @param angleUnit
	 *            angle unit
	 * @param temperatureUnit
	 *            temperature unit
	 * @param orientationMode
	 *            orientation mode
	 * @throws IOException
	 *             exception
	 */
	void setUnits(Unit accelerationUnit, Unit angularRateUnit, Unit angleUnit, Unit temperatureUnit,
			OrientationMode orientationMode) throws IOException;

	/**
	 * This method returns the absolute orientation: X will contain heading, Y will
	 * contain roll data, Z will contain pitch. The units will be the ones selected
	 * with {@link #setUnits(Unit, Unit, Unit, Unit, OrientationMode)}
	 * (AccelerationUnit, AngularRateUnit, EulerAngleUnit, TemperatureUnit,
	 * OrientationMode)}.
	 *
	 * @return Tuple 3D
	 * @throws IOException
	 *             exception
	 */
	Tuple3f read() throws IOException;

	/**
	 * The fusion algorithm offset and tilt compensated absolute orientation data in
	 * Euler angles. x = magnetic heading, y = roll, z = pitch. Note that this is
	 * only available in one of the fusion modes.
	 * 
	 * @return a tuple containing x = magnetic heading, y = roll, z = pitch.
	 * @throws IOException
	 *             exception
	 */
	Tuple3f readEulerAngles() throws IOException;

	/**
	 * @return the magnetometer values in x,y,z in {@link Unit#MICROTESLA}.
	 * @throws IOException
	 *             exception
	 */
	Tuple3f readMagnetometer() throws IOException;

	/**
	 * @return the accelerometer information for the accelerometer in the currently
	 *         set acceleration unit.
	 * @throws IOException
	 *             exception
	 */
	Tuple3f readAccelerometer() throws IOException;

	/**
	 * @return the gyro information in the currently set angular rate unit.
	 * @throws IOException
	 *             exception
	 */
	Tuple3f readGyro() throws IOException;

	/**
	 * Reads the quaternion data for the absolute orientation into a Tuple4d. Note
	 * that w will be in the t field.
	 * 
	 * @return the quaternion data for the absolute orientation.
	 * @throws IOException
	 *             exception
	 */
	Tuple4d readQuaternion() throws IOException;

	/**
	 * The fusion algorithm output for the linear acceleration data for each axis in
	 * currently set acceleration unit. Note that this is only available in one of
	 * the fusion modes.
	 * 
	 * @return the fusion algorithm output for the linear acceleration data for each
	 *         axis in currently set acceleration unit.
	 * @throws IOException
	 *             exception
	 */
	Tuple3f readLinearAcceleration() throws IOException;

	/**
	 * The fusion algorithm output for the gravity vector in the currently set
	 * acceleration unit. Note that this is only available in one of the fusion
	 * modes.
	 * 
	 * @return the fusion algorithm output for the gravity vector in the currently
	 *         set acceleration unit.
	 * @throws IOException
	 *             exception
	 */
	Tuple3f readGravityVector() throws IOException;

	/**
	 * Will attempt to reset the device. Note that this can be a lengthy, blocking
	 * operation.
	 * 
	 * @throws IOException
	 *             exception
	 */
	void reset() throws IOException;

	/**
	 * Returns the orientation (used for interpret the Euler angles)
	 * 
	 * @return the configured orientation of the device.
	 */
	OrientationMode getCurrentOrientation();

	/**
	 * Shutdown the device, releasing any resources.
	 */
	void shutdown();

	boolean isUseExternalChrystal() throws IOException;

	void setUseExternalChrystal(boolean b) throws IOException;
}