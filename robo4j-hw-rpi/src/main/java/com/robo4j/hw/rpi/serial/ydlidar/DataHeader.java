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
 * Header for data.
 */
public class DataHeader {
	private static final byte ANSWER_SYNC_BYTE1 = (byte) 0xAA;
	private static final byte ANSWER_SYNC_BYTE2 = (byte) 0x55;
	private final boolean isValid;
	private final PacketType packetType;
	private final int lsn;
	private final int fsa;
	private final int lsa;
	private final int checksum;
	// For the fields in the header only, not including
	private final int headerChecksum;

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
		this.lsn = getByte(headerData, 3);
		this.fsa = getFromShort(headerData, 4);
		this.lsa = getFromShort(headerData, 6);
		this.checksum = getFromShort(headerData, 8);
		this.headerChecksum = calculateHeaderChecksum(headerData);
	}

	public boolean isValid() {
		return isValid;
	}

	public PacketType getPacketType() {
		return packetType;
	}

	/**
	 * @return the length of the data part in bytes.
	 */
	public int getDataLength() {
		return lsn * 2;
	}

	/**
	 * @return the sample count
	 */
	public int getSampleCount() {
		return lsn;
	}

	/**
	 * @return the expected checksum for the header plus data (not including
	 *         checksum field)
	 */
	public int getExpectedChecksum() {
		return checksum;
	}

	/**
	 * @return the precalculated part of the checksum for the header (not
	 *         including checksum field)
	 */
	public int getHeaderChecksum() {
		return headerChecksum;
	}

	public float getAngleAt(int samplePointIndex, float distance) {
		float correction = calcCorrection(distance);
		float angleFSA = (fsa >> 1) / 64.0f + correction;
		if (samplePointIndex == 0) {
			return angleFSA;
		}
		float angleLSA = (lsa >> 1) / 64.0f + correction;
		if (samplePointIndex == lsn - 1) {
			return angleLSA;
		}

		return (angleLSA - angleFSA) * (samplePointIndex) + angleFSA + correction;
	}

	private float calcCorrection(float distance) {
		if (distance <= 0) {
			return 0;
		}
		return (float) Math.toDegrees(Math.atan(21.8 * (155.3 - distance) / (155.3 * distance)));
	}

	/**
	 * FSA, as defined in the protocol.
	 */
	int getFSA() {
		return fsa;
	}

	/**
	 * LSA, as defined in the protocol.
	 */
	int getLSA() {
		return lsa;
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

	private static int getByte(byte[] headerData, int index) {
		return (headerData[index] & 0xFF);
	}

	static int getFromShort(byte[] headerData, int startIndex) {
		// Funny byte ordering courtesy of the protocol
		return (headerData[startIndex] & 0xFF) | ((headerData[startIndex + 1] & 0xFF) << 8);
	}

	private int calculateHeaderChecksum(byte[] headerData) {
		int checksum = 0;
		for (int i = 0; i < 4; i++) {
			checksum ^= (headerData[i] & 0xFF) << 8 | (headerData[i + 1] & 0xFF);
		}
		return checksum;
	}
}
