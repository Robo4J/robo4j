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

/**
 * The calibration status.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class Bno055CalibrationStatus {
	private final int status;

	public enum CalibrationStatus {
		NOT_CALIBRATED(0), MINIMALLY_CALIBRATED(1), SOMEWHAT_CALIBRATED(2), FULLY_CALIBRATED(3);

		private int statusValue;

		private CalibrationStatus(int statusValue) {
			this.statusValue = statusValue;
		}

		public int getStatusValue() {
			return statusValue;
		}

		public static CalibrationStatus fromStatusValue(int statusValue) {
			for (CalibrationStatus status : values()) {
				if (status.getStatusValue() == statusValue) {
					return status;
				}
			}
			return null;
		}
	}

	/**
	 * Creates a status object.
	 * 
	 * @param statusRegisterValue
	 *            the contents of the status register.
	 */
	public Bno055CalibrationStatus(int statusRegisterValue) {
		this.status = statusRegisterValue;
	}

	/**
	 * @return the raw composite status value.
	 */
	public final int getRawStatus() {
		return status;
	}

	/**
	 * @return the calibration status for the magnetometer.
	 */
	public CalibrationStatus getMagnetometerCalibrationStatus() {
		return CalibrationStatus.fromStatusValue(status & 3);
	}

	/**
	 * @return the calibration status for the accelerometer.
	 */
	public CalibrationStatus getAccelerometerCalibrationStatus() {
		return CalibrationStatus.fromStatusValue((status >> 2) & 3);
	}

	/**
	 * @return the calibration status for the gyro.
	 */
	public CalibrationStatus getGyroCalibrationStatus() {
		return CalibrationStatus.fromStatusValue((status >> 4) & 3);
	}

	/**
	 * @return the calibration status for the system. Depends on the status of
	 *         all sensors.
	 */
	public CalibrationStatus getSystemCalibrationStatus() {
		return CalibrationStatus.fromStatusValue((status >> 6) & 3);
	}
	
	/**
	 * @return true only if all sensors are fully calibrated.
	 */
	public boolean isFullyCalibrated() {
		return getMagnetometerCalibrationStatus() == CalibrationStatus.FULLY_CALIBRATED && getAccelerometerCalibrationStatus() == CalibrationStatus.FULLY_CALIBRATED && getGyroCalibrationStatus() == CalibrationStatus.FULLY_CALIBRATED && getSystemCalibrationStatus() == CalibrationStatus.FULLY_CALIBRATED;
	}
}
