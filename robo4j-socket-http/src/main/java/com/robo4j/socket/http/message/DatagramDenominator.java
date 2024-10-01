/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.message;

import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.util.Utf8Constant;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DatagramDenominator implements MessageDenominator<byte[]> {
	private final StringBuilder sb = new StringBuilder();
	private final int type;
	private final String path;

	/**
	 * Constructor
	 * 
	 * @param type
	 *            denominator type
	 * @param path
	 *            requested path
	 */
	public DatagramDenominator(int type, String path) {
		this.type = type;
		this.path = path;
	}

	@Override
	public byte[] generate() {
        sb.append(type)
            .append(Utf8Constant.UTF8_SPACE)
		    .append(path);
		RoboHttpUtils.decorateByNewLine(sb);
		return sb.toString().getBytes();
	}
}
