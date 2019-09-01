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
package com.robo4j.hw.rpi.serial.ydlidar;

/**
 * The commands available.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum Command {
	//@formatter:off
	FORCE_STOP(0x00),
	LOW_POWER_CONSUMPTION(0x01),
	LOW_POWER_SHUTDOWN(0x02),
	/**
	 * This command is used to enable the constant frequency of the system. 
	 * After being enabled, when the lidar is in scanning mode, it will 
	 * automatically adjust the speed so that the scanning frequency will 
	 * be stabilized at the currently set scanning frequency. 
	 * G4 defaults to constant frequency.
	 */
	CONSTANT_FREQUENCY_ON(0x0E),
	/**
	 * This command is used to shut down the system constant frequency. 
	 * After the radar is turned off, the lidar does not perform automatic 
	 * speed adjustment in the scanning mode. 
	 */
	CONSTANT_FREQUENCY_OFF(0x0F),
	/**
	 * G4 enters a soft reboot and the system restarts. 
	 * This command does not answer.
	 * 
	 * (One manual says 0x80, one says 0x40 - going with 0x80...)
	 */
	RESTART(0x80),
	/**
	 * Enters scan mode and feeds back point cloud data.
	 */
	SCAN(0x60),
	FORCE_SCAN(0x61),
	/**
	 * Stops scanning.
	 */
	STOP(0x65),
	GET_EAI(0x55),
	GET_DEVICE_INFO(0x90),
	GET_DEVICE_HEALTH(0x92),
	/**
	 * This command is used to set the system's ranging frequency and switch the 
	 * ranging frequency between 4 KHz, 8 KHz and 9 KHz. 
	 * The default ranging frequency is 9 KHz. 
	 */
	SET_RANGING_FREQUENCY(0xD0),
	/**
	 * This command is used to obtain the current ranging frequency of the system. 
	 */
	GET_RANGING_FREQUENCY(0xD1);
	//@formatter:on

	int instructionCode;

	Command(int instructionCode) {
		this.instructionCode = instructionCode;
	}

	public int getInstructionCode() {
		return instructionCode;
	}
}