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

import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpMessageDescriptor;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.units.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.robo4j.socket.http.util.HttpMessageUtil.HTTP_HEADER_BODY_DELIMITER;
import static com.robo4j.socket.http.util.HttpMessageUtil.HTTP_HEADER_SEP;
import static com.robo4j.socket.http.util.HttpMessageUtil.NEXT_LINE;
import static com.robo4j.socket.http.util.HttpMessageUtil.POSITION_BODY;
import static com.robo4j.socket.http.util.HttpMessageUtil.POSITION_HEADER;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ChannelBufferUtils {

	public static final int INIT_BUFFER_CAPACITY = 4 * 4096;
	public static final byte CHAR_NEW_LINE = 0x0A;
	public static final byte CHAR_RETURN = 0x0D;
	public static final byte[] END_WINDOW = { CHAR_NEW_LINE, CHAR_NEW_LINE };
	private static final ByteBuffer buffer = ByteBuffer.allocateDirect(INIT_BUFFER_CAPACITY);
	private static final int BUFFER_MARK_END = -1;

	public static ByteBuffer copy(ByteBuffer source, int start, int end) {
		ByteBuffer result = ByteBuffer.allocate(end);
		for (int i = start; i < end; i++) {
			result.put(source.get(i));
		}
		return result;
	}

	public static ByteBuffer getByteBufferByString(String message) {
		ByteBuffer result = ByteBuffer.allocate(message.length());
		result.put(message.getBytes());
		result.flip();
		return result;
	}

	public static HttpMessageDescriptor getHttpMessageDescriptorByChannel(ByteChannel channel) throws IOException {
		final StringBuilder sbBasic = new StringBuilder();
		int readBytes = channel.read(buffer);
		if(readBytes != BUFFER_MARK_END){

			buffer.flip();
			addToStringBuilder(sbBasic, buffer, readBytes);
			final StringBuilder sbAdditional = new StringBuilder();
			final HttpMessageDescriptor result = extractDescriptorByStringMessage(sbBasic.toString());

			int totalReadBytes = readBytes;

			if (result.getLength() != null) {
				while (totalReadBytes < result.getLength()) {
					readBytes = channel.read(buffer);
					buffer.flip();
					addToStringBuilder(sbAdditional, buffer, readBytes);

					totalReadBytes += readBytes;
					buffer.clear();
				}
				if (sbAdditional.length() > 0) {
					result.addMessage(sbAdditional.toString());
				}
			}
			buffer.clear();
			return result;
		} else {
			return new HttpMessageDescriptor(new HashMap<>(), null, null, null);
		}
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


	public static HttpMessageDescriptor extractDescriptorByStringMessage(String message) {
		final String[] headerAndBody = message.split(HTTP_HEADER_BODY_DELIMITER);
		final String[] header = headerAndBody[POSITION_HEADER].split("[" + NEXT_LINE + "]+");
		final String firstLine = RoboHttpUtils.correctLine(header[0]);
		final String[] tokens = firstLine.split(Constants.HTTP_EMPTY_SEP);
		final String[] paramArray = Arrays.copyOfRange(header, 1, header.length);

		final HttpMethod method = HttpMethod.getByName(tokens[HttpMessageUtil.METHOD_KEY_POSITION]);
		final String path = tokens[HttpMessageUtil.URI_VALUE_POSITION];
		final String version = tokens[HttpMessageUtil.VERSION_POSITION];
		final Map<String, String> headerParams = new HashMap<>();

		for (int i = 1; i < paramArray.length; i++) {
			final String[] array = paramArray[i].split(HttpMessageUtil.getHttpSeparator(HTTP_HEADER_SEP));

			String key = array[HttpMessageUtil.METHOD_KEY_POSITION].toLowerCase();
			String value = array[HttpMessageUtil.URI_VALUE_POSITION].trim();
			headerParams.put(key, value);
		}

		HttpMessageDescriptor result = new HttpMessageDescriptor(headerParams, method, version, path);
		if (headerParams.containsKey(HttpHeaderFieldNames.CONTENT_LENGTH)) {
			result.setLength(calculateMessageSize(headerAndBody[POSITION_HEADER].length(), headerParams));
		}
		if (headerAndBody.length > 1) {
			result.addMessage(headerAndBody[POSITION_BODY]);
		}

		return result;
	}

	private static Integer calculateMessageSize(int headerValue, Map<String, String> headerParams) {
		return headerValue + HTTP_HEADER_BODY_DELIMITER.length()
				+ Integer.valueOf(headerParams.get(HttpHeaderFieldNames.CONTENT_LENGTH));
	}

	private static void addToStringBuilder(StringBuilder sb, ByteBuffer buffer, int size) {
		byte[] array = new byte[size];
		for (int i = 0; i < size; i++) {
			array[i] = buffer.get(i);
		}
		final String message = new String(array);
		sb.append(message);
	}
}
