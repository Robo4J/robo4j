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
	 * @return
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
	 * enough, or the time out is reached.
	 */
	void calibrate(long timeout);
}
