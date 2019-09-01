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
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class DataHeader {
	public static final int DATA_HEADER_LENGTH = 10;
	public static final byte ANSWER_SYNC_BYTE1 = (byte) 0xAA;
	public static final byte ANSWER_SYNC_BYTE2 = (byte) 0x55;
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
		if (headerData.length < DATA_HEADER_LENGTH) {
			throw new IllegalArgumentException("Too short array to contain a data header - must be at least 10 elements");
		}
		this.isValid = isValid(headerData);
		this.packetType = PacketType.getPacketType(getByte(headerData, 2));
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
	 * @return the pre-calculated part of the checksum for the header (not
	 *         including checksum field)
	 */
	public int getHeaderChecksum() {
		return headerChecksum;
	}

	/**
	 * Returns the angle at the point.
	 * 
	 * @param samplePointIndex
	 *            the sample point index for which to calculate the angle.
	 * @param distance
	 *            the distance, in mm.
	 * @return the angle, in degrees.
	 */
	public float getAngleAt(int samplePointIndex, float distance, float diff) {
		float correction = calcCorrection(distance);
		if (samplePointIndex == 0) {
			return getUncorrectedStartAngle() + correction;
		}
		if (samplePointIndex == lsn - 1) {
			return getUncorrectedEndAngle() + correction;
		}
		return (diff / (lsn - 1)) * (samplePointIndex) + getUncorrectedStartAngle() + correction;
	}

	public static float getAngularDiff(float correctedStart, float correctedEnd) {
		float diff;
		if (correctedStart > 270 && correctedEnd < 90) {
			diff = 360 + correctedEnd - correctedStart;
		} else {
			diff = correctedEnd - correctedStart;
		}
		return diff;
	}

	private float calcCorrection(float distance) {
		if (distance <= 0.02) {
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

	/**
	 * LSN, as defined in the protocol.
	 */
	int getLSN() {
		return lsn;
	}

	float getUncorrectedStartAngle() {
		return (fsa >> 1) / 64.0f;
	}

	float getUncorrectedEndAngle() {
		return (lsa >> 1) / 64.0f;
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

	/**
	 * Can be used to check if a byte array is a start of a response.
	 */
	public static boolean isDataHeaderStart(byte[] bytes) {
		return bytes.length >= 2 && isValid(bytes);
	}

	@Override
	public String toString() {
		return "DataHeader [type: " + getPacketType().name() + ", samples: " + getSampleCount() + ", uncorrected start: "
				+ getUncorrectedStartAngle() + ", uncorrected end: " + getUncorrectedEndAngle() + "]";
	}
}
