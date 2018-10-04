/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.socket.http.util;

import com.robo4j.socket.http.HttpHeaderFieldNames;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.message.HttpDecoratedResponse;
import com.robo4j.socket.http.message.HttpResponseDenominator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import static com.robo4j.socket.http.util.ChannelBufferUtils.BUFFER_MARK_END;
import static com.robo4j.socket.http.util.ChannelBufferUtils.INIT_BUFFER_CAPACITY;
import static com.robo4j.socket.http.util.HttpConstant.HTTP_NEW_LINE;
import static com.robo4j.socket.http.util.HttpMessageUtils.HTTP_HEADER_BODY_DELIMITER;
import static com.robo4j.socket.http.util.HttpMessageUtils.POSITION_BODY;
import static com.robo4j.socket.http.util.HttpMessageUtils.POSITION_HEADER;

/**
 * ChannelResponseBuffer
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ChannelResponseBuffer {

	private ByteBuffer responseBuffer;

	public ChannelResponseBuffer() {
		responseBuffer = ByteBuffer.allocateDirect(INIT_BUFFER_CAPACITY);
	}

	public HttpDecoratedResponse getHttpDecoratedResponseByChannel(ByteChannel channel) throws IOException {
		final StringBuilder sbBasic = new StringBuilder();
		int readBytes = channel.read(responseBuffer);
		if (readBytes != BUFFER_MARK_END) {
			responseBuffer.flip();
			ChannelBufferUtils.addToStringBuilder(sbBasic, responseBuffer, readBytes);
			final HttpDecoratedResponse result = extractDecoratedResponseByStringMessage(sbBasic.toString());
			ChannelBufferUtils.readChannelBuffer(result, channel, responseBuffer, readBytes);
			responseBuffer.clear();
			return result;

		} else {

			return new HttpDecoratedResponse(new HashMap<>(),
					new HttpResponseDenominator(StatusCode.BAD_REQUEST, HttpVersion.HTTP_1_1));
		}
	}

	// TODO: 3/5/18 (miro) investigate spring responseBody
	private HttpDecoratedResponse extractDecoratedResponseByStringMessage(String message) {
		final String[] headerAndBody = message.split(HTTP_HEADER_BODY_DELIMITER);
		final String[] header = headerAndBody[POSITION_HEADER].split("[" + HTTP_NEW_LINE + "]+");
		final String firstLine = RoboHttpUtils.correctLine(header[0]);
		final String[] tokens = firstLine.split(HttpConstant.HTTP_EMPTY_SEP);
		final String[] paramArray = Arrays.copyOfRange(header, 1, header.length);

		final String version = tokens[0];
		final StatusCode statusCode = StatusCode.getByCode(Integer.valueOf(tokens[1]));
		final Map<String, String> headerParams = ChannelBufferUtils.getHeaderParametersByArray(paramArray);

		HttpResponseDenominator denominator = new HttpResponseDenominator(statusCode, HttpVersion.getByValue(version));
		HttpDecoratedResponse result = new HttpDecoratedResponse(headerParams, denominator);
		if (headerAndBody.length > 1) {
			if (headerParams.containsKey(HttpHeaderFieldNames.CONTENT_LENGTH)) {
				result.setLength(
						ChannelBufferUtils.calculateMessageSize(headerAndBody[POSITION_HEADER].length(), headerParams));
			} else {
				result.setLength(headerAndBody[POSITION_BODY].length());
			}
			Matcher matcher = ChannelBufferUtils.RESPONSE_SPRING_PATTERN.matcher(headerAndBody[POSITION_BODY]);
			if (matcher.find()) {
				// result.addMessage(headerAndBody[POSITION_BODY]);
				result.addMessage(matcher.group(ChannelBufferUtils.RESPONSE_JSON_GROUP));
			}
		}

		return result;
	}
}
