/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This PageParser.java  is part of robo4j.
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

package com.robo4j.page;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Miro Wengner (@miragemiko)
 * @since 11.06.2016
 */
public class PageParser {

	private static final String BRACKET_START = "\\$\\{";
	private static final String BRACKET_END = "\\}";
	private static final Pattern pageValuePattern = Pattern
			.compile("(" + BRACKET_START + "[a-z]+[0-9]?+" + BRACKET_END + ")");
	private static final String EMPTY = "";

	public static String parseAndReplace(final String input, final Map<String, String> values) {
		String result = input;
		final Matcher matcherMain = pageValuePattern.matcher(result.trim());
		int filledOut = 0;
		while (matcherMain.find() && filledOut < values.size()) {

			String group = matcherMain.group();
			result = result.replace(group,
					values.get(group.replaceAll(BRACKET_START, EMPTY).replaceAll(BRACKET_END, EMPTY)));
			filledOut++;
		}
		return result;
	}

}
