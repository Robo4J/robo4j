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
 * The response header.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ResponseHeader {
	public static final int RESPONSE_HEADER_LENGTH = 7;
	private static final byte ANSWER_SYNC_BYTE1 = (byte) 0xA5;
	private static final byte ANSWER_SYNC_BYTE2 = (byte) 0x5A;

	private final ResponseType responseType;
	private final ResponseMode responseMode;
	private final boolean isValid;
	private final int responseLength;

	public static class BadResponseException extends Exception {
		private static final long serialVersionUID = 1L;

		public BadResponseException(String message) {
			super(message);
		}

	}

	public enum ResponseType {
		DEVICE_INFO(0x4), DEVICE_HEALTH(0x6), MEASUREMENT(0x81), UNKNOWN(-1);

		private int responseCode;

		ResponseType(int responseCode) {
			this.responseCode = responseCode;
		}

		public static ResponseType getResponseType(int responseCode) {
			for (ResponseType type : values()) {
				if (type.responseCode == responseCode) {
					return type;
				}
			}
			return UNKNOWN;
		}
	}

	public enum ResponseMode {
		/**
		 * A single result is expected.
		 */
		SINGLE,
		/**
		 * Results will be continuously sent until another command is sent to
		 * change this.
		 */
		CONTINUOUS,
		/**
		 * An error has occurred.
		 */
		UNDEFINED
	}

	/**
	 * Constructor.
	 * 
	 * @param headerData
	 *            the data for the header. Must be at least 7 bytes long and contain header data.
	 */
	public ResponseHeader(byte[] headerData) {
		if (headerData.length < 7) {
			throw new IllegalArgumentException("Not enough elements to contain response header data - must be at least 7 elements");
		}
		this.responseType = ResponseType.getResponseType(headerData[6] & 0xFF);
		this.isValid = isValid(headerData);
		this.responseLength = getResponseLength(headerData);
		this.responseMode = getResponseMode(headerData);
	}

	/**
	 * @return the response type for which the header was generated.
	 */
	public ResponseType getResponseType() {
		return responseType;
	}

	/**
	 * @return true if the response header was a valid response header, i.e.
	 *         starting with the sync bytes.
	 */
	public boolean isValid() {
		return isValid;
	}

	/**
	 * @return the response length.
	 */
	public int getResponseLength() {
		return responseLength;
	}

	/**
	 * @return the {@link ResponseMode}.
	 */
	public ResponseMode getResponseMode() {
		return responseMode;
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

	private static int getResponseLength(byte[] headerData) {
		int responseLength = headerData[5] & 0x3F;
		responseLength = (responseLength << 8) + 0xFF & headerData[4];
		responseLength = (responseLength << 8) + 0xFF & headerData[3];
		responseLength = (responseLength << 8) + 0xFF & headerData[2];
		return responseLength;
	}

	private static ResponseMode getResponseMode(byte[] headerData) {
		int mode = (headerData[5] & 0xC0) >> 6;
		switch (mode) {
		case 0:
			return ResponseMode.SINGLE;
		case 1:
			return ResponseMode.CONTINUOUS;
		default:
			return ResponseMode.UNDEFINED;
		}
	}

	/**
	 * Can be used to check if a byte array is a start of a response.
	 */
	public static boolean isResponseHeaderStart(byte[] bytes) {
		return bytes.length >= 2 && isValid(bytes);
	}
}
