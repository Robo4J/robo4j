/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This HttpMethod.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.http;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The HTTP request methods
 * <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">methods</a>
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 06.03.2016
 */
public enum HttpMethod implements Comparator<HttpMethod> {

	// @formatter:off
	// TODO: properly implement all possibilities

	/**
	 * The GET method means retrieve whatever information (in the form of an
	 * entity) is identified by the Request-URI. If the Request-URI refers to a
	 * data-producing process, it is the produced data which shall be returned
	 * as the entity in the response and not the source text of the process,
	 * unless that text happens to be the output of the process.
	 */
	GET("GET"),

	/**
	 * The HEAD method is identical to GET except that the server MUST NOT
	 * return a message-body in the response.
	 */
	HEAD("HEAD"),

	/**
	 * The POST method is used to request that the origin server accept the
	 * entity enclosed in the request as a new subordinate of the resource
	 * identified by the Request-URI in the Request-Line.
	 */
	POST("POST"),

	/**
	 * The PUT method requests that the enclosed entity be stored under the
	 * supplied Request-URI
	 */
	PUT("PUT"),

	/**
	 * The DELETE method requests that the origin server delete the resource
	 * identified by the Request-URI.
	 */
	DELETE("DELETE"),

	/**
	 * The TRACE method is used to invoke a remote, application-layer loop- back
	 * of the request message. The final recipient of the request SHOULD reflect
	 * the message received back to the client as the entity-body of a 200 (OK)
	 * response.
	 */
	TRACE("TRACE"),

	/**
	 * The OPTIONS method represents a request for information about the
	 * communication options available on the request/response chain identified
	 * by the Request-URI. This method allows the client to determine the
	 * options and/or requirements associated with a resource, or the
	 * capabilities of a server, without implying a resource action or
	 * initiating a resource retrieval.
	 */
	OPTIONS("OPTIONS"),

	/**
	 * This specification reserves the method name CONNECT for use with a proxy
	 * that can dynamically switch to being a tunnel
	 */
	CONNECT("CONNECT");
	// @formatter:on

	private volatile static Map<String, HttpMethod> nameToHttpMethod;
	private final String name;

	HttpMethod(String name) {
		this.name = name;
	}

	// Utils Method
	public static HttpMethod getByName(String name) {
		if (Objects.isNull(nameToHttpMethod)) {
			iniMapping();
		}
		return nameToHttpMethod.get(name);
	}

	// Private Methods
	private static void iniMapping() {
		nameToHttpMethod = new HashMap<>();
		for (HttpMethod method : values()) {
			nameToHttpMethod.put(method.getName(), method);
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public int compare(HttpMethod o1, HttpMethod o2) {
		return o1.getName().compareTo(o2.getName());
	}

	@Override
	public String toString() {
		return "HttpMethod{" + "name='" + name + '\'' + '}';
	}
}
