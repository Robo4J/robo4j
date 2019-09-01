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
 * System error results.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum Bno055SystemError {
	//@formatter:off
	NO_ERROR(0, "No error"), 
	PERIPHERAL_INIT(1, "Peripheral initialization error"), 
	SYSTEM_INIT(2, "System initializiation error"), 
	SELF_TEST_FAILED(3, "Self test result failed"), 
	REGISTER_MAP_OOR(4, "Register map value out of range"), 
	REGISTER_MAP_ADDRESS_OOR(5, "Register map address out of range"), 
	REGISTER_MAP_WRITE(6, "Register map write error"), 
	BNO_LOW_POWER_MODE_NA(7, "BNO low power mode not available for selected operation mode"), 
	ACCELEROMETER_POWER_MODE_NA(8, "Accelerometer power mode not available"), 
	FUSION_CONFIG_ERROR(9, "Fusion mode configuration error"), 
	SENSOR_CONFIG_ERROR(10, "Sensor configuration error");
	//@formatter:on

	int errorCode;
	String errorMessage;

	Bno055SystemError(int errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public static Bno055SystemError fromErrorCode(int errorCode) {
		for (Bno055SystemError error : values()) {
			if (error.getErrorCode() == errorCode) {
				return error;
			}
		}
		return null;
	}
	
	public String toString() {
		return errorMessage;
	}
}