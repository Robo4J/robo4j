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
package com.robo4j.socket.http;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The HTTP request methods
 * <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html">methods</a>
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum HttpMethod implements Comparator<HttpMethod> {

	// @formatter:off
	/**
	 * The GET method means retrieve whatever information (in the form of an
	 * entity) is identified by the Request-URI. If the Request-URI refers to a
	 * data-producing process, it is the produced data which shall be returned
	 * as the entity in the response and not the source text of the process,
	 * unless that text happens to be the output of the process. Http 1.1 must
	 * implement.
	 */
	GET		("GET"),

	/**
	 * The HEAD method is identical to GET except that the server MUST NOT
	 * return a message-body in the response. Inspect resource headers.
	 */
	HEAD	("HEAD"),

	/**
	 * The POST method is used to request that the origin server accept the
	 * entity enclosed in the request as a new subordinate of the resource
	 * identified by the Request-URI in the Request-Line. Input data for processing
	 */
	POST	("POST"),

	/**
	 * The PUT method requests that the enclosed entity be stored under the
	 * supplied Request-URI. Deposit data on server - inverse to get
	 */
	PUT		("PUT"),

	/**
	 * The PATCH method requests that a set of changes described in the
     * request entity be applied to the resource identified by the Request-URI.
	 * Partially modify a resource
	 */
	PATCH	("PATCH"),

	/**
	 * The DELETE method requests that the origin server delete the resource
	 * identified by the Request-URI.
	 */
	DELETE	("DELETE"),

	/**
	 * The TRACE method is used to invoke a remote, application-layer loop- back
	 * of the request message. The final recipient of the request SHOULD reflect
	 * the message received back to the client as the entity-body of a 200 (OK)
	 * response.
	 */
	TRACE	("TRACE"),

	/**
	 * The OPTIONS method represents a request for information about the
	 * communication options available on the request/response chain identified
	 * by the Request-URI. This method allows the client to determine the
	 * options and/or requirements associated with a resource, or the
	 * capabilities of a server, without implying a resource action or
	 * initiating a resource retrieval.
	 */
	OPTIONS	("OPTIONS"),

	/**
	 * This specification reserves the method name CONNECT for use with a proxy
	 * that can dynamically switch to being a tunnel
	 */
	CONNECT	("CONNECT");
	// @formatter:on

	private static Map<String, HttpMethod> mapByName;
	private final String name;

	HttpMethod(String name) {
		this.name = name;
	}

	// Utils Method
	public static HttpMethod getByName(String name) {
		if (Objects.isNull(mapByName)) {
			mapByName = iniMapping();
		}
		return mapByName.get(name);
	}

	// Private Methods
	private static Map<String, HttpMethod> iniMapping() {
		return Stream.of(values()).collect(Collectors.toMap(HttpMethod::getName, e -> e));
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
