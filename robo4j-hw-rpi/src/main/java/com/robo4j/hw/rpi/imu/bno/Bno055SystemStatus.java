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
 * System status result.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Bno055SystemStatus {
	private final StatusFlag[] flags;

	public enum StatusFlag {
		//@formatter:off
		IDLE(0, "Idle"), 
		SYSTEM_ERROR(1, "System error"), 
		INIT_PERIPHERALS(2, "Initializing Peripherals"), 
		INIT_SYSTEM(3, "System Initilization"), 
		SELF_TEST(4, "Executing Self Test"), 
		RUNNING_SENSOR_FUSION(5, "System running with sensor fusion"), 
		RUNNING_NO_SENSOR_FUSION(6, "System running with no sensor fusion");
		//@formatter:on

		int bitPosition;
		String statusMessage;

		StatusFlag(int bitPosition, String errorMessage) {
			this.bitPosition = bitPosition;
			this.statusMessage = errorMessage;
		}

		public int getBitPosition() {
			return bitPosition;
		}

		public String getStatusMessage() {
			return statusMessage;
		}

		public static StatusFlag fromBitPosition(int bitPosition) {
			for (StatusFlag flag : values()) {
				if (flag.getBitPosition() == bitPosition) {
					return flag;
				}
			}
			return null;
		}
	}

	public Bno055SystemStatus(int registerValue) {
		flags = deriveFlags(registerValue);
	}

	public Bno055SystemStatus(StatusFlag[] flags) {
		this.flags = flags;
	}

	private StatusFlag[] deriveFlags(int registerValue) {
		int count = 0;
		StatusFlag[] tmpFlags = new StatusFlag[7];

		for (int i = 0; i < tmpFlags.length; i++) {
			if (isBitSet(i, registerValue)) {
				tmpFlags[count++] = StatusFlag.fromBitPosition(i);
			}
		}
		StatusFlag[] resultFlags = new StatusFlag[count];
		System.arraycopy(tmpFlags, 0, resultFlags, 0, count);
		return resultFlags;
	}

	private boolean isBitSet(int i, int registerValue) {
		return (registerValue & (1 << i)) > 0;
	}

	public StatusFlag[] getStatusFlags() {
		return flags;
	}

	/**
	 * @return true if idle or running.
	 */
	public boolean isReady() {
		// Seems 0 can be read after reset
		if (flags.length == 0) {
			return true;
		}
		return containsOneOf(StatusFlag.IDLE, StatusFlag.RUNNING_SENSOR_FUSION, StatusFlag.RUNNING_NO_SENSOR_FUSION);
	}

	/**
	 * @return true on system error.
	 */
	public boolean hasError() {
		return containsOneOf(StatusFlag.SYSTEM_ERROR);
	}

	/**
	 * Returns true if initializing or running self tests.
	 * 
	 * @return true if initializing or running self tests.
	 */
	public boolean isBusy() {
		return containsOneOf(StatusFlag.INIT_PERIPHERALS, StatusFlag.INIT_SYSTEM, StatusFlag.SELF_TEST);
	}

	private boolean containsOneOf(StatusFlag... flagsToCheck) {
		for (StatusFlag flag : flagsToCheck) {
			if (contains(flag)) {
				return true;
			}
		}
		return false;
	}

	private boolean contains(StatusFlag flagToCheck) {
		for (StatusFlag flag : flags) {
			if (flag == flagToCheck) {
				return true;
			}
		}
		return false;
	}
}