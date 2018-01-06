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
package com.robo4j.hw.rpi.imu;

/**
 * The result of a BNO055 self test.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class BNO055SelfTestResult {
	private final int registerContent;
	private final BNO055SystemError error;

	public enum TestResult {
		FAILED, PASSED
	}

	public BNO055SelfTestResult(int registerContent, int errorCode) {
		this.registerContent = registerContent;
		this.error = BNO055SystemError.fromErrorCode(errorCode);
	}

	public TestResult getAccelerometerResult() {
		return (registerContent & 1) == 0 ? TestResult.FAILED : TestResult.PASSED;
	}

	public TestResult getMagnetometerResult() {
		return (registerContent & 2) == 0 ? TestResult.FAILED : TestResult.PASSED;
	}

	public TestResult getGyroResult() {
		return (registerContent & 4) == 0 ? TestResult.FAILED : TestResult.PASSED;
	}

	public TestResult getMicroControllerResult() {
		return (registerContent & 8) == 0 ? TestResult.FAILED : TestResult.PASSED;
	}

	public BNO055SystemError getError() {
		return error;
	}
	
	public String toString() {
		if (getError() == BNO055SystemError.NO_ERROR) {
			return String.format("Accelerometer: %s, Magnetometer: %s, Gyro: %s, Microcontroller: %s", getAccelerometerResult(), getMagnetometerResult(), getGyroResult(), getMicroControllerResult());
		} else {
			return String.format("Accelerometer: %s, Magnetometer: %s, Gyro: %s, Microcontroller: %s - Error: %s", getAccelerometerResult(), getMagnetometerResult(), getGyroResult(), getMicroControllerResult(), String.valueOf(getError()));
		}
	}
}
