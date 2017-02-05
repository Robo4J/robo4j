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
package com.robo4j.core.util;

import java.util.Arrays;
import java.util.List;

/**
 * Commonly used constants
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ConstantUtil {

	public static final String EMPTY_STRING = "";
	public static final String EXIT = "exit";
	public static final String ACTIVE = "active";
	public static final String STATUS = "status";
	public static final String SETUP = "setup";
	public static final String COMMAND_BUS = "command-bus";
	public static final String PROVIDER_BUS = "provider-bus";
	public static final String FACTORY_BUS = "factory-bus";
	public static final int DEFAULT_VALUE_0 = 0;
	public static final int DEFAULT_VALUE_1 = 1;
	public static final int PLATFORM_FACTORY = 10; /* Producer Consumer */
	public static final int PLATFORM_ENGINES = 30;
	public static final int DEFAULT_PRIORITY = 1;
	public static final List<String> availablePaths = Arrays.asList(EXIT, STATUS, SETUP);
	public static final int DEFAULT_ENGINE_SPEED = 300;
	public final static String HTTP_QUERY_SEP = "&";
	public final static String HTTP_EMPTY_SEP = "\\s+";
	public static final String STORAGE_SERVICE = "storageService";
	/**
	 * The HTTP separator characters. Defined in RFC 2616, section 2.2
	 */
	private final static String HTTP_SEPARATORS = " ,;=()<>@:\\\"/[]?{}\t";

	public static String getHttpSeparator(int position) {
		return Character.toString(HTTP_SEPARATORS.charAt(position));
	}

}
