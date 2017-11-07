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

package com.robo4j.socket.http.util;

import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.HttpByteWrapper;
import com.robo4j.socket.http.units.BufferWrapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ChannelBufferUtils {

	public static final int INIT_BUFFER_CAPACITY = 48;
	public static final byte CHAR_NEW_LINE = 0x0A;
	public static final byte CHAR_RETURN = 0x0D;
	public static final byte[] END_WINDOW = { CHAR_NEW_LINE, CHAR_NEW_LINE };

	public static ByteBuffer copy(ByteBuffer source, int start, int end) {
		ByteBuffer result = ByteBuffer.allocate(end);
		for (int i = start; i < end; i++) {
			result.put(source.get(i));
		}
		return result;
	}

	public static HttpByteWrapper getHttpByteWrapperByByteBufferString(BufferWrapper bufferWrapper) {
		final String[] headerAndBody = bufferWrapper.getMessage().split("\n\n");
		final String[] header = headerAndBody[0].split("[\r\n]+");
		return new HttpByteWrapper(header, headerAndBody[1]);
	}

	public static BufferWrapper getBufferWrapperByChannel(ByteChannel channel) throws IOException {

		ByteBuffer buffer = ByteBuffer.allocate(INIT_BUFFER_CAPACITY);
		StringBuilder stringBuilder = new StringBuilder();
		int readBytes = ChannelUtil.readBuffer(channel, buffer);
		addToStringBuilder(stringBuilder, buffer, readBytes);

		int totalReadBytes = readBytes;
		while (!buffer.hasRemaining() && (readBytes % INIT_BUFFER_CAPACITY) == 0) {
			int tmpSize = buffer.array().length;
			buffer = ByteBuffer.allocate(tmpSize + INIT_BUFFER_CAPACITY);
			totalReadBytes += readBytes;
			readBytes = ChannelUtil.readBuffer(channel, buffer);
			addToStringBuilder(stringBuilder, buffer, readBytes);

		}

		System.out.println(ChannelBufferUtils.class.getSimpleName() + " totalReadBytes: " + totalReadBytes
				+ " stringBuffer= " + stringBuilder.toString());

		if (buffer.remaining() == 0) {
			SimpleLoggingUtil.error(ChannelBufferUtils.class, "buffer has a problem position: " + buffer.position()
					+ " readBytes: " + readBytes + " limit: " + buffer.limit());
		}
		buffer.clear();

		return new BufferWrapper(totalReadBytes, stringBuilder.toString());

	}

	public static byte[] validArray(byte[] array, int size) {
		return validArray(array, 0, size);
	}

	public static byte[] validArray(byte[] array, int start, int size) {
		byte[] result = new byte[size];
		for (int i = start; i < size; i++) {
			result[i] = array[i];
		}
		return result;
	}

	public static boolean isBWindow(byte[] stopWindow, byte[] window) {
		return Arrays.equals(stopWindow, window);
	}

	private static void addToStringBuilder(StringBuilder sb, ByteBuffer buffer, int end) {

		byte[] array = new byte[end];
		for (int i = 0; i < end; i++) {
			array[i] = buffer.get(i);
		}
		sb.append(new String(array));

	}
}
