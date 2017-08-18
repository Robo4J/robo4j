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

package com.robo4j.core.units.httpunit.codec;

import java.util.HashMap;
import java.util.Map;

import com.robo4j.core.units.httpunit.Constants;
import com.robo4j.core.units.httpunit.HttpDecoder;
import com.robo4j.core.units.httpunit.HttpEncoder;
import com.robo4j.core.units.httpunit.HttpProducer;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@HttpProducer
public class CameraMessageCodec implements HttpDecoder<CameraMessage>, HttpEncoder<CameraMessage> {
	private static final String KEY_TYPE = "type";
	private static final String KEY_VALUE = "value";
	private static final String KEY_IMAGE = "image";

	@Override
	public String encode(CameraMessage stuff) {
		//@formatter:off
        final StringBuilder sb = new StringBuilder("{\"")
				.append(KEY_TYPE).append("\":\"")
                .append(stuff.getType())
                .append("\",\"")
				.append(KEY_VALUE)
				.append("\":\"")
                .append(stuff.getValue())
                .append("\",\"")
				.append(KEY_IMAGE)
				.append("\":\"")
                .append(stuff.getImage())
                .append("\"}");
        //@formatter:off
        return sb.toString();
    }

    @Override
    public CameraMessage decode(String json) {
        final Map<String, String> map = new HashMap<>();
        //@formatter:off
		final String[] parts = json.replaceAll("^\\{\\s*\"|\"\\s*\\}$", Constants.EMPTY_STRING)
				.split("\"?(\"?\\s*:\\s*\"?|\\s*,\\s*)\"?");
		//@formatter:on
		for (int i = 0; i < parts.length - 1; i += 2) {
			map.put(parts[i].trim(), parts[i + 1].trim());
		}

		final String type = map.get(KEY_TYPE);
		final String value =  map.get(KEY_VALUE);
		final String image = map.get(KEY_IMAGE);
		return new CameraMessage(type, value, image);

	}

	@Override
	public Class<CameraMessage> getEncodedClass() {
		return CameraMessage.class;
	}

	@Override
	public Class<CameraMessage> getDecodedClass() {
		return CameraMessage.class;
	}

}
