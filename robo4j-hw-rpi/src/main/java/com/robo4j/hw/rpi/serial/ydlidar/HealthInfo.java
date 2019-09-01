/*
 * Copyright (c) 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.serial.ydlidar;

/**
 * Health info for the lidar.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HealthInfo {
	private final HealthStatus status;
	private final short errorCode;

	public enum HealthStatus {
		//@formatter:off
		OK((byte) 0x0), 
		WARNING((byte) 0x1), 
		ERROR((byte) 0x2),
		UNKNOWN((byte) -1);
		//@formatter:on

		byte statusCode;

		HealthStatus(byte status) {
			this.statusCode = status;
		}

		public byte getStatusCode() {
			return statusCode;
		}

		public static HealthStatus fromStatusCode(byte code) {
			for (HealthStatus status : values()) {
				if (status.getStatusCode() == code) {
					return status;
				}
			}
			return UNKNOWN;
		}
	}

	public HealthInfo(HealthStatus status, short errorCode) {
		this.status = status;
		this.errorCode = errorCode;
	}

	public HealthStatus getStatus() {
		return status;
	}

	public short getErrorCode() {
		return errorCode;
	}

	public String toString() {
		return String.format("HealthInfo status=%s, errorCode=%d", status.name(), errorCode);
	}
}
