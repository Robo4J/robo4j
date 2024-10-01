/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.message;

import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.HttpConstant;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DatagramDecoratedRequest implements DatagramMessage<byte[]> {

	private final DatagramDenominator denominator;
	private String host;
	private Integer port;
	private byte[] message;

	public DatagramDecoratedRequest(DatagramDenominator denominator) {
		this.denominator = denominator;
	}

	/**
	 * convert to byte array based on following UDP protocol
     * //@formatter:off
     * -----------
     * |Type Path|
     * |         |
     * |Body     |
     * |----------
     * //@formatter:on
     * note: empty space represent space, line are separated by new line
     *
	 * @return byte of datagram message protocol
	 */
	@Override
	public byte[] toMessage() {
		ByteBuffer headerAndMessage = ByteBuffer.allocate(HttpConstant.HTTP_NEW_LINE.length() + message.length);
		headerAndMessage.put(HttpConstant.HTTP_NEW_LINE.getBytes());
		headerAndMessage.put(message);
		return ChannelBufferUtils.joinByteArrays(denominator.generate(), headerAndMessage.array());
	}

	/**
	 * add new byte array to the message
	 * 
	 * @param message
	 *            byte array
	 */
	@Override
	public void addMessage(byte[] message) {
		this.message = this.message == null ? message : ChannelBufferUtils.joinByteArrays(this.message, message);
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "DatagramDecoratedRequest{" + "host='" + host + '\'' + ", port=" + port + ", message="
				+ Arrays.asList(message) + '}';
	}
}
