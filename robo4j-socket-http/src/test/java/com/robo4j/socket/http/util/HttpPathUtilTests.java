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

package com.robo4j.socket.http.util;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.PathMethodDTO;
import com.robo4j.socket.http.units.test.PropertyMapBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpPathUtilTests {

	@SuppressWarnings("unchecked")
	@Test
	public void parsTargetUnitsTest() {

	    //@formatter:off
		Map<String, Object> targetMethodMap = PropertyMapBuilder.Builder()
                .put("imageController", "POST")
				.put("cameraController", "GET").build();
	    //@formatter:on
		String targetUnitsString = JsonUtil.getJsonByMap(targetMethodMap);
		Set<PathMethodDTO> result = HttpPathUtil.getPathMethodTargetByString(targetUnitsString);

		Assert.assertTrue(result.contains(new PathMethodDTO("imageController", HttpMethod.POST)));
		Assert.assertTrue(result.contains(new PathMethodDTO("cameraController", HttpMethod.GET)));

	}
}
