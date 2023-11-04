/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.ConfigurationException;
import com.robo4j.socket.http.units.CodecRegistry;

import static com.robo4j.util.Utf8Constant.UTF8_COMMA;

/**
 * Codec Registry for codecs used for json socket communication
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CodeRegistryUtils {

	public static CodecRegistry getCodecRegistry(String packages) throws ConfigurationException {
		if (RoboHttpUtils.validatePackages(packages.trim())) {
			return new CodecRegistry(Thread.currentThread().getContextClassLoader(), packages.split(UTF8_COMMA));
		} else {
			throw new ConfigurationException("not valid code package");
		}
	}

}
