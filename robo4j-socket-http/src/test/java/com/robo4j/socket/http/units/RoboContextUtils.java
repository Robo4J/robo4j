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
package com.robo4j.socket.http.units;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;

import java.io.InputStream;
import java.util.Objects;

/**
 * Load from *.xml descriptor file
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class RoboContextUtils {

	public static RoboContext loadRoboContextByXml(String xmlFilename) throws RoboBuilderException {
		Objects.requireNonNull(xmlFilename, "not allowed");
		RoboBuilder builder = new RoboBuilder();
		InputStream contextIS = RoboContextUtils.class.getClassLoader().getResourceAsStream(xmlFilename);
		builder.add(contextIS);
		return builder.build();
	}

	public static RoboContext loadRoboContextBySystemAndContextXml(String... xmlFilename) throws RoboBuilderException {
		if (xmlFilename == null || xmlFilename.length == 0 || xmlFilename.length > 2) {
			throw new IllegalArgumentException("usage: systemConf.xml contextConf.xml");
		}
		InputStream systemIS = RoboContextUtils.class.getClassLoader().getResourceAsStream(xmlFilename[0]);
		InputStream contextIS = RoboContextUtils.class.getClassLoader().getResourceAsStream(xmlFilename[1]);
		RoboBuilder builder = new RoboBuilder(systemIS);
		builder.add(contextIS);
		return builder.build();
	}
}
