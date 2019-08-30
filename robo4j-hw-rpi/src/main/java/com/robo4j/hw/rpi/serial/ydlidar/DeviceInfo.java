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
 * Information about the lidar device.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class DeviceInfo {
	/**
	 * The various models of the ydlidar.
	 */
	public enum Model {
		F4(1), T1(2), F2(3), S4(4), G4(5), X4(6), F4Pro(8), G4C(9), UNKNOWN(-1);

		int modelCode;

		Model(int modelCode) {
			this.modelCode = modelCode;
		}

		public static Model getModel(int code) {
			for (Model model : values()) {
				if (model.modelCode == code) {
					return model;
				}
			}
			return UNKNOWN;
		}
	}

	private final Model model;
	private final int firmwareVersion;
	private final int hardwareVersion;
	private final byte[] serialVersion;

	public DeviceInfo(int modelCode, int firmwareVersion, int hardwareVersion, byte[] serialVersion) {
		this.model = Model.getModel(modelCode);
		this.firmwareVersion = firmwareVersion;
		this.hardwareVersion = hardwareVersion;
		this.serialVersion = serialVersion;
	}

	public Model getModel() {
		return model;
	}

	public int getFirmwareVersion() {
		return firmwareVersion;
	}

	public int getHardwareVersion() {
		return hardwareVersion;
	}

	public byte[] getSerialVersion() {
		return serialVersion;
	}

	public static String prettyPrintSerialVersion(byte[] bytes) {
		return String.format("%d%d%d%d-%d%d-%d%d - %d%d%d%d%d%d%d%d", bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5], bytes[6],
				bytes[7], bytes[8], bytes[9], bytes[10], bytes[11], bytes[12], bytes[13], bytes[14], bytes[15]);
	}

	@Override
	public String toString() {
		return "DeviceInfo [model=" + model + ", firmwareVersion=" + firmwareVersion + ", hardwareVersion=" + hardwareVersion
				+ ", serialVersion=" + prettyPrintSerialVersion(serialVersion) + "]";
	}
}
