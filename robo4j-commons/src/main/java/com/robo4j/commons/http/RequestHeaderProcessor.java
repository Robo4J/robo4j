/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This RequestHeaderProcessor.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.commons.http;

import java.util.Map;

/**
 *
 * helper class for processing HTTP requests
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 23.05.2016
 */
public final class RequestHeaderProcessor {

	private static final String CONTENT_LENGHT = "Content-Length";

	public static int getContentLength(final Map<String, String> header) {
		return Integer.valueOf(header.get(CONTENT_LENGHT).trim());
	}

}
