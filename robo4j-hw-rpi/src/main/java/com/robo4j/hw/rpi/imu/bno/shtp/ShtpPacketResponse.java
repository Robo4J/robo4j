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

package com.robo4j.hw.rpi.imu.bno.shtp;

import static com.robo4j.hw.rpi.imu.bno.impl.Bno080SPIDevice.SHTP_HEADER_SIZE;

/**
 * ShtpPacketResponse represent SPI device response see
 * {@link com.robo4j.hw.rpi.imu.bno.impl.Bno080SPIDevice}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ShtpPacketResponse {
	private final int[] header = new int[SHTP_HEADER_SIZE];
	private int[] body;

	public ShtpPacketResponse(int size) {
		this.body = new int[size];
	}

	public void addHeader(int... header) {
		if (header.length != this.header.length) {
			System.out.println("ShtpPacketRequest: wrong header");
		} else {
			this.header[0] = header[0]; // LSB
			this.header[1] = header[1]; // MSB
			this.header[2] = header[2]; // Channel Number
			// Send the sequence number, increments with each packet sent,
			// different counter for each channel
			this.header[3] = header[3];
		}

	}

	public int[] getHeader() {
		int[] copy = new int[header.length];
		System.arraycopy(header, 0, copy, 0, header.length);
		return copy;
	}

	public byte getHeaderChannel() {
		return (byte) header[2];
	}

	public void addBody(int pos, int element) {
		body[pos] = element;
	}

	public int getBodyFirst() {
		return body.length == 0 ? -1 : body[0];
	}

	public int[] getBody() {
		int[] copy = new int[body.length];
		System.arraycopy(body, 0, copy, 0, body.length);
		return copy;
	}

	public int getBodySize() {
		return body.length;
	}

	public boolean dataAvailable() {
		return body.length > 0;
	}

	public String toString() {
		return "ShtpPacketResponse channel: " + getHeaderChannel() + "\n\theader: " + ShtpUtils.toHexString(getHeader()) + "\n\tbody: "
				+ ShtpUtils.toHexString(getBody());
	}
}
