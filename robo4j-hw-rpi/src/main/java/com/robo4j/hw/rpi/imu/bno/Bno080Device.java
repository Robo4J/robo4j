/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.hw.rpi.imu.bno.bno08x.TareBasis;
import com.robo4j.hw.rpi.imu.bno.shtp.SensorReportId;

/**
 * Interface for BNO080 Devices.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface Bno080Device {

	/**
	 * Add a listener for new data.
	 *
	 * @param listener
	 *            the listener to accept new data.
	 */
	void addListener(DataListener listener);

	/**
	 * Remove a listener for new data.
	 *
	 * @param listener
	 *            the listener to remove.
	 */
	void removeListener(DataListener listener);

	/**
	 * Starts listening for specific data.
	 *
	 * @param report
	 *            the kind of data to start listening for.
	 * @param reportPeriod
	 *            report period in ms.
	 * @return boolean status
	 */
	boolean start(SensorReportId report, int reportPeriod);

	/**
	 * Stop listening for all kinds of reports.
	 *
	 * @return true if successfully stopped.
	 */
	boolean stop();

	/**
	 * Shuts down the device. There is no coming back. ;)
	 */
	void shutdown();

	/**
	 * Do calibration cycle. Will block until calibration results are good
	 * enough, or the timeout is reached. unit milliseconds
	 */
	void calibrate(long timeout);

	// --- Tare operations ---

	/**
	 * Perform a tare operation on all axes using the rotation vector as basis.
	 * This zeroes out the current orientation as the reference.
	 *
	 * @return true if the command was sent successfully
	 */
	default boolean tareNow() {
		return tareNow(false, TareBasis.ROTATION_VECTOR);
	}

	/**
	 * Perform a tare operation.
	 *
	 * @param zAxisOnly if true, only tare the Z axis (heading); if false, tare all axes
	 * @param basis     which rotation vector to use as the basis for tare
	 * @return true if the command was sent successfully
	 */
	boolean tareNow(boolean zAxisOnly, TareBasis basis);

	/**
	 * Persist the current tare settings to flash memory.
	 * The tare will be automatically applied on next power-up.
	 *
	 * @return true if the command was sent successfully
	 */
	boolean saveTare();

	/**
	 * Clear any previously applied tare.
	 *
	 * @return true if the command was sent successfully
	 */
	boolean clearTare();

	// --- Dynamic Calibration Data (DCD) operations ---

	/**
	 * Save the current Dynamic Calibration Data (DCD) to flash.
	 * This preserves calibration across power cycles for faster startup.
	 *
	 * @return true if the command was sent successfully
	 */
	boolean saveCalibration();

	/**
	 * Configure which sensors have dynamic calibration enabled.
	 *
	 * @param accel  enable accelerometer calibration
	 * @param gyro   enable gyroscope calibration
	 * @param mag    enable magnetometer calibration
	 * @return true if the command was sent successfully
	 */
	boolean setCalibrationConfig(boolean accel, boolean gyro, boolean mag);

	// --- Power management ---

	/**
	 * Put the sensor hub into sleep mode to conserve power.
	 * Use {@link #wake()} to bring it back.
	 *
	 * @return true if the command was sent successfully
	 */
	boolean sleep();

	/**
	 * Wake the sensor hub from sleep mode.
	 *
	 * @return true if the command was sent successfully
	 */
	boolean wake();

	// --- Status ---

	/**
	 * Check if the sensor has been reset since the last call to this method.
	 * This can be used to detect unexpected resets and re-initialize sensors.
	 *
	 * @return true if a reset has occurred
	 */
	boolean wasReset();

	/**
	 * Get the reason for the last reset.
	 * Values: 1=POR, 2=Internal reset, 3=Watchdog, 4=External reset, 5=Other
	 *
	 * @return reset reason code
	 */
	int getResetReason();
}
