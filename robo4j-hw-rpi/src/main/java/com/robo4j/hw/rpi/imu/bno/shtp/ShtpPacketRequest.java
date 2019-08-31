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
 * ShtpPacketRequest represent SHTP request format used by SPI devices
 * {@link ShtpOperation}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ShtpPacketRequest {
	private final int[] header = new int[SHTP_HEADER_SIZE];
	private final int sequenceNumber;
	private int[] body;

	public ShtpPacketRequest(int size, int sequenceNumber) {
		this.body = new int[size];
		this.sequenceNumber = sequenceNumber;
	}

	/**
	 * create a shtp header for the command.
	 * sequence number increments header[3] with each packet sent to this channel
	 *
	 * @param shtpChannel device channel
	 */
	public void createHeader(ShtpChannel shtpChannel) {
		int[] header = createSpiHeader(body.length + SHTP_HEADER_SIZE, shtpChannel, sequenceNumber);
		this.header[0] = header[0]; // LSB
		this.header[1] = header[1]; // MSB
		this.header[2] = header[2]; // Channel Number
		this.header[3] = header[3]; // Sequence Number
	}

	public void addBody(int pos, int element) {
		body[pos] = element;
	}

	public void addBody(int[] elements) {
		for (int i = 0; i < elements.length; i++) {
			body[i] = elements[i] & 0xFF;
		}
	}

	public ShtpChannel getRegister() {
		return ShtpChannel.getByChannel((byte) header[2]);
	}

	public int[] getHeader() {
		return header;
	}

	public byte getHeaderByte(int pos) {
		return (byte) (header[pos] & 0xFF);
	}

	public int getHeaderSize() {
		return header.length;
	}

	public int[] getBody() {
		return body;
	}

	public byte getBodyByte(int pos) {
		return (byte) (body[pos] & 0xFF);
	}

	public int getBodySize() {
		return body.length;
	}

	private int[] createSpiHeader(int packetLength, ShtpChannel shtpChannel, int sequenceNumber) {
		int[] header = new int[SHTP_HEADER_SIZE];
		header[0] = (byte) (packetLength & 0xFF); // LSB
		header[1] = (byte) ((packetLength >> 8) & 0xFF); // MSB
		header[2] = (byte) (shtpChannel.getChannel() & 0xFF); // Channel Number
		header[3] = (byte) (sequenceNumber & 0xFF); // Sequence number
		return header;
	}
}
