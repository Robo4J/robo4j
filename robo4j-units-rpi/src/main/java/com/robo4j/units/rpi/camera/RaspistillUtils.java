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

package com.robo4j.units.rpi.camera;

import java.util.Map;

import com.robo4j.hw.rpi.camera.PropertyMapBuilder;

/**
 *
 * raspistill specific utilities
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
final class RaspistillUtils {

	static final String DEFAULT = "default";
	static final String FORMAT_IMAGE = "jpg";
	static final String DEFAULT_ENCODING = "UTF-8";
	static final String KEY_WIDTH = "width";
	static final String KEY_HEIGHT = "height";
	static final String KEY_TIMEOUT = "timeout";
	static final String KEY_QUALITY = "quality";
	static final String KEY_SHARPNESS = "sharpness";
	static final String KEY_BRIGHTNESS = "brightness";
	static final String KEY_CONTRAST = "contrast";
	static final String KEY_SATURATION = "saturation";
	static final String KEY_ROTATION = "rotation";
	static final String KEY_TIMELAPSE = "timelapse";
	static final String KEY_EXPOSURE = "exposure";
	static final String RASPISTILL_COMMAND = "raspistill";

	static String getOption(String key) {
		return raspistillProperties.get(key);
	}

	static boolean isOption(String key) {
		return raspistillProperties.containsKey(key);
	}

	@SuppressWarnings(value = "unchecked")
	//@formatter:off
	private static final Map<String, String> raspistillProperties = PropertyMapBuilder.Builder()
			.put(KEY_WIDTH, "-w").put(KEY_HEIGHT, "-h")
			.put(KEY_TIMEOUT, "-t").put(KEY_QUALITY, "-q")
			.put(KEY_SHARPNESS, "-sh").put(KEY_BRIGHTNESS, "-br")
			.put(KEY_CONTRAST, "-co").put(KEY_SATURATION, "-sa")
			.put(KEY_EXPOSURE, "-ex").put(KEY_TIMELAPSE, "-tl")
			.put(KEY_ROTATION, "-rot")
			.create();
	//@formatter:on
}
