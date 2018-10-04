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
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.message.AbstractHttpDecoratedMessage;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpRequestDenominator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.robo4j.socket.http.util.HttpConstant.HTTP_NEW_LINE;
import static com.robo4j.socket.http.util.HttpMessageUtils.HTTP_HEADER_BODY_DELIMITER;
import static com.robo4j.socket.http.util.HttpMessageUtils.HTTP_HEADER_SEP;
import static com.robo4j.socket.http.util.HttpMessageUtils.POSITION_BODY;
import static com.robo4j.socket.http.util.HttpMessageUtils.POSITION_HEADER;

/**
 * ChannelBufferUtils useful utilities to work with channel
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class ChannelBufferUtils {

	public static final Pattern RESPONSE_SPRING_PATTERN = Pattern.compile("^(\\d.\r\n)?(.*)(\r\n)?");
	public static final int CHANNEL_TIMEOUT = 60000;
	public static final int INIT_BUFFER_CAPACITY = 4 * 4096;
	public static final byte CHAR_NEW_LINE = 0x0A;
	public static final byte CHAR_RETURN = 0x0D;
	public static final byte[] END_WINDOW = { CHAR_NEW_LINE, CHAR_NEW_LINE };
	public static final int BUFFER_MARK_END = -1;
	public static final int RESPONSE_JSON_GROUP = 2;

	private static final ByteBuffer requestBuffer = ByteBuffer.allocateDirect(INIT_BUFFER_CAPACITY);

	/**
	 *
	 * @param source
	 *            buffer
	 * @param start
	 *            start position
	 * @param end
	 *            end position
	 * @return buffer
	 */
	public static ByteBuffer copy(ByteBuffer source, int start, int end) {
		ByteBuffer result = ByteBuffer.allocate(end);
		for (int i = start; i < end; i++) {
			result.put(source.get(i));
		}
		return result;
	}

	/**
	 *
	 * @param message
	 *            message
	 * @return byte buffer
	 */
	public static ByteBuffer getByteBufferByString(String message) {
		ByteBuffer result = ByteBuffer.allocate(message.length());
		result.put(message.getBytes());
		result.flip();
		return result;
	}

	/**
	 *
	 * @param array1
	 *            array 1
	 * @param array2
	 *            array 2
	 * @return byte array
	 */
	public static byte[] joinByteArrays(final byte[] array1, byte[] array2) {
		byte[] result = Arrays.copyOf(array1, array1.length + array2.length);
		System.arraycopy(array2, 0, result, array1.length, array2.length);
		return result;
	}

	/**
	 *
	 * @param array
	 *            array
	 * @param size
	 *            size
	 * @return byte array
	 */
	public static byte[] validArray(byte[] array, int size) {
		return validArray(array, 0, size);
	}

	/**
	 *
	 * @param stopWindow
	 *            byte array
	 * @param window
	 *            byte array
	 * @return is byte array window
	 */
	public static boolean isBWindow(byte[] stopWindow, byte[] window) {
		return Arrays.equals(stopWindow, window);
	}

	/**
	 *
	 * @param message
	 *            message
	 * @return http decorate request
	 */
	public static HttpDecoratedRequest extractDecoratedRequestByStringMessage(String message) {
		final String[] headerAndBody = message.split(HTTP_HEADER_BODY_DELIMITER);
		final String[] header = headerAndBody[POSITION_HEADER].split("[" + HTTP_NEW_LINE + "]+");
		final String firstLine = RoboHttpUtils.correctLine(header[0]);
		final String[] tokens = firstLine.split(HttpConstant.HTTP_EMPTY_SEP);
		final String[] paramArray = Arrays.copyOfRange(header, 1, header.length);

		final HttpMethod method = HttpMethod.getByName(tokens[HttpMessageUtils.METHOD_KEY_POSITION]);
		final String path = tokens[HttpMessageUtils.URI_VALUE_POSITION];
		final String version = tokens[HttpMessageUtils.VERSION_POSITION];
		final Map<String, String> headerParams = getHeaderParametersByArray(paramArray);

		final HttpRequestDenominator denominator;
		if (path.contains(HttpPathUtils.DELIMITER_PATH_ATTRIBUTES)) {
			denominator = new HttpRequestDenominator(method, path.split(HttpPathUtils.REGEX_ATTRIBUTE)[0],
					HttpVersion.getByValue(version), HttpPathUtils.extractAttributesByPath(path));
		} else {
			denominator = new HttpRequestDenominator(method, path, HttpVersion.getByValue(version));
		}
		HttpDecoratedRequest result = new HttpDecoratedRequest(headerParams, denominator);

		if (headerParams.containsKey(HttpHeaderFieldNames.CONTENT_LENGTH)) {
			result.setLength(calculateMessageSize(headerAndBody[POSITION_HEADER].length(), headerParams));
			result.addMessage(headerAndBody[POSITION_BODY]);
		}

		return result;
	}

	/**
	 * convert byte buffer to string and clean
	 * 
	 * @param buffer
	 *            incoming buffer
	 * @return string
	 */
	public static String byteBufferToString(ByteBuffer buffer) {
		StringBuilder sb = new StringBuilder();
		while (buffer.hasRemaining()) {
			sb.append((char) buffer.get());
		}
		buffer.clear();
		return sb.toString();
	}

	/**
	 *
	 * @param result
	 *            result message
	 * @param channel
	 *            byte channel
	 * @param buffer
	 *            buffer
	 * @param readBytes
	 *            read bytes
	 * @throws IOException
	 *             exception
	 */
	static void readChannelBuffer(AbstractHttpDecoratedMessage result, ByteChannel channel, ByteBuffer buffer,
			int readBytes) throws IOException {
		final StringBuilder sbAdditional = new StringBuilder();
		int totalReadBytes = readBytes;
		if (result.getLength() != 0) {
			while (totalReadBytes < result.getLength()) {
				readBytes = channel.read(buffer);
				buffer.flip();
				ChannelBufferUtils.addToStringBuilder(sbAdditional, buffer, readBytes);

				totalReadBytes += readBytes;
				buffer.clear();
			}
			if (sbAdditional.length() > 0) {
				result.addMessage(sbAdditional.toString());
			}
		}
	}

	/**
	 *
	 * @param paramArray
	 *            params array
	 * @return map
	 */
	static Map<String, String> getHeaderParametersByArray(String[] paramArray) {
		final Map<String, String> result = new HashMap<>();
		for (int i = 0; i < paramArray.length; i++) {
			final String[] array = paramArray[i].split(HttpMessageUtils.getHttpSeparator(HTTP_HEADER_SEP));

			String key = array[HttpMessageUtils.METHOD_KEY_POSITION].toLowerCase();
			String value = array[HttpMessageUtils.URI_VALUE_POSITION].trim();
			result.put(key, value);
		}
		return result;
	}

	static Integer calculateMessageSize(int headerValue, Map<String, String> headerParams) {
		return headerValue + HTTP_HEADER_BODY_DELIMITER.length()
				+ Integer.valueOf(headerParams.get(HttpHeaderFieldNames.CONTENT_LENGTH));
	}

	/**
	 *
	 * @param sb
	 *            string builder
	 * @param buffer
	 *            buffer
	 * @param size
	 *            size
	 */
	static void addToStringBuilder(StringBuilder sb, ByteBuffer buffer, int size) {
		byte[] array = new byte[size];
		for (int i = 0; i < size; i++) {
			array[i] = buffer.get(i);
		}
		final String message = new String(array);
		sb.append(message);
	}

	/**
	 *
	 * @param array
	 *            array
	 * @param start
	 *            start position
	 * @param size
	 *            size
	 * @return byte array
	 */
	private static byte[] validArray(byte[] array, int start, int size) {
		byte[] result = new byte[size];
		System.arraycopy(result, start, array, 0, size);
		return result;
	}

}
