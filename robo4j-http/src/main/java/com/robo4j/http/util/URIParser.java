/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This URIParser.java is part of robo4j.
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

package com.robo4j.http.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robo4j.http.dto.RequestDTO;

/**
 *
 * Parsing URL
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 17.10.2016
 */
public final class URIParser {
	private static final String HTTP_GET_AND = "&";
	private static final String HTTP_GET_EQ = "=";
	private static final int V_0 = 0;
	private static final int V_1 = 1;
	private static final int V_3 = 3;
	private static final Pattern httpGetUriPattern = Pattern.compile("^/([a-z]+)(\\?)?(.*)");

	// TODO: FIXME need to prepare also header
	@SuppressWarnings(value = "unchecked")
	public static RequestDTO parseGetUri(final List<String> paths, final String uri) {

		final Matcher mather = httpGetUriPattern.matcher(uri);
		if (mather.matches()) {
			String path = mather.group(V_1);
			String variables = mather.group(V_3);
			return new RequestDTO(path, parseVariablesToMap(variables));
		} else {
			return new RequestDTO("", Collections.EMPTY_MAP);
		}

	}

	// Private Methods
	private static Map<String, String> parseVariablesToMap(String values) {
		return Stream.of(values.split(HTTP_GET_AND)).map(e -> e.split(HTTP_GET_EQ))
				.filter(e -> Arrays.asList(e).size() > 1).collect(Collectors.toMap(e -> e[V_0], e -> e[V_1]));
	}
}
