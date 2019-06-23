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

public class ResponseHeader {
	private static final byte ANSWER_SYNC_BYTE1 = (byte) 0xA5;
	private static final byte ANSWER_SYNC_BYTE2 = 0x5A;

	private final byte[] headerData;

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

	/**
	 * Constructor.
	 * 
	 * @param headerData
	 */
	public ResponseHeader(byte[] headerData) {
		this.headerData = headerData;
	}

	/**
	 * @return true if this header is valid, i.e. the sync bytes are correct.
	 */
	public boolean isValid() {
		if (headerData[0] != ANSWER_SYNC_BYTE1) {
			return false;
		}
		if (headerData[1] != ANSWER_SYNC_BYTE2) {
			return false;
		}
		return true;
	}

	/**
	 * @return the response type for which the header was generated.
	 */
	public ResponseType getResponseType() {
		return ResponseType.getResponseType(headerData[6]);
	}
}
