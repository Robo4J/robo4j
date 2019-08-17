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

import com.robo4j.hw.rpi.serial.ydlidar.ResponseHeader.ResponseType;

/**
 * Header for data.
 */
public class DataHeader {
	private static final byte ANSWER_SYNC_BYTE1 = (byte) 0x55;
	private static final byte ANSWER_SYNC_BYTE2 = (byte) 0xAA;
	private final boolean isValid;
	private final PacketType packetType;
	private final int dataLength;

	public enum PacketType {
		POINT_CLOUD(0x0), ZERO(0x1), UNKNOWN(-1);

		private int packetCode;

		PacketType(int packetCode) {
			this.packetCode = packetCode;
		}

		public static PacketType getPacketType(int packetCode) {
			for (PacketType type : values()) {
				if (type.packetCode == packetCode) {
					return type;
				}
			}
			return UNKNOWN;
		}
	}

	public DataHeader(byte[] headerData) {
		if (headerData.length != 10) {
			throw new IllegalArgumentException("The length of the data header must be 10 bytes long!");
		}
		this.isValid = isValid(headerData);
		this.packetType = PacketType.getPacketType(headerData[2]);
		this.dataLength = calculateDataLength(headerData);
	}

	public boolean isValid() {
		return isValid;
	}

	private static boolean isValid(byte[] headerData) {
		if (headerData[0] != ANSWER_SYNC_BYTE1) {
			return false;
		}
		if (headerData[1] != ANSWER_SYNC_BYTE2) {
			return false;
		}
		return true;
	}

	public int getDataLength() {
		return dataLength;
	}

	private static int calculateDataLength(byte[] headerData) {
		// Widened to int by & to get sign right (provided as unsigned byte)
		return (headerData[3] & 0xFF) * 2;
	}

}
