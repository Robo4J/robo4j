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

/**
 * System error results.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum BNO055SystemStatus {
	//@formatter:off
	IDLE(0, "Idle"), 
	SYSTEM_ERROR(1, "System error"), 
	INIT_PERIPHERALS(2, "Initializing Peripherals"), 
	INIT_SYSTEM(3, "System Initilization"), 
	SELF_TEST(4, "Executing Self Test"), 
	RUNNING_SENSOR_FUSION(5, "System running with sensor fusion"), 
	RUNNING_NO_SENSOR_FUSION(6, "System running with no sensor fusion");
	//@formatter:on

	int statusCode;
	String statusMessage;

	BNO055SystemStatus(int errorCode, String errorMessage) {
		this.statusCode = errorCode;
		this.statusMessage = errorMessage;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public static BNO055SystemStatus fromErrorCode(int errorCode) {
		for (BNO055SystemStatus error : values()) {
			if (error.getStatusCode() == errorCode) {
				return error;
			}
		}
		return null;
	}
}