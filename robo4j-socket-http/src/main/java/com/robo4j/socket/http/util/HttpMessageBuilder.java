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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.util;

import com.robo4j.socket.http.message.HttpDenominator;

import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpMessageBuilder {

	private HttpHeaderBuilder headerBuilder;
	private HttpDenominator denominator;

	private HttpMessageBuilder() {
		this.headerBuilder = HttpHeaderBuilder.Build();
	}

	public static HttpMessageBuilder Build() {
		return new HttpMessageBuilder();
	}

	public HttpMessageBuilder setDenominator(HttpDenominator denominator) {
		this.denominator = denominator;
		return this;
	}

	public HttpMessageBuilder addHeaderElements(Map<String, String> headerMap) {
		headerBuilder.addAll(headerMap);
		return this;
	}

	public HttpMessageBuilder addHeaderElement(String key, String value) {
		headerBuilder.add(key, value);
		return this;
	}

	/**
	 * build Http Message 1. line - request/response 2. header - available header 3.
	 * message -&gt; if message is available add message
	 *
	 * @return HttpMessage String
	 */
	public String build() {
		return getStringBuilder().toString();
	}

    /**
     *
     * @param attachment attached object to the http message
     * @return final HttpMessage
     */
	public String build(String attachment) {
		final StringBuilder sb = getStringBuilder();
		return sb.append(attachment).toString();
	}

	private StringBuilder getStringBuilder() {
		final StringBuilder sb = new StringBuilder(denominator.generate());
		RoboHttpUtils.decorateByNewLine(sb);
		sb.append(headerBuilder.build());
		RoboHttpUtils.decorateByNewLine(sb);
		return sb;
	}
}
