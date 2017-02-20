/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This RoboRequestDynamicFactory.java  is part of robo4j.
 * module: robo4j-core
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.client.request;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robo4j.core.RoboUnit;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.http.HttpMessageWrapper;
import com.robo4j.http.HttpVersion;
import com.robo4j.http.util.HttpMessageUtil;

/**
 * Dynamically configurable request factory
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboRequestFactory implements DefaultRequestFactory<String> {

	private static final int DEFAULT_POSITION_0 = 0;

	private List<RoboUnit<?>> units = null;

	public RoboRequestFactory() {
	}

	@Override
	public String processGet(HttpMessageWrapper<?> wrapper) {
		if (HttpVersion.containsValue(wrapper.message().version())) {
			final URI uri = wrapper.message().uri();
			//@formatter:off
            final List<String> paths = Stream.of(wrapper.message().uri().getPath()
                        .split(HttpMessageUtil.getHttpSeparator(12)))
                    .filter(e -> !e.isEmpty())
                    .collect(Collectors.toList());
            //@formatter:on

			// TODO: support more paths
			SimpleLoggingUtil.debug(getClass(), "path: " + paths);

			String path = paths.get(DEFAULT_POSITION_0);
			Set<RoboRequestEntity> availablePathValues = RoboRequestTypeRegistry.getInstance().getPathValues(path);

			if (units != null) {
				RoboUnit<?> desiredUnit = units.get(DEFAULT_POSITION_0);
				System.out.println(getClass().getSimpleName() + " desiredUnit: " + desiredUnit);
				System.out.println(getClass().getSimpleName() + " getMessageType: " + desiredUnit.getMessageType());
				System.out.println(
						getClass().getSimpleName() + " getMessageType: " + desiredUnit.getMessageType().isEnum());

			} else {
				System.out.println(getClass().getSimpleName() + " NO DESIRED UNITS");
			}

			if (availablePathValues != null && !availablePathValues.isEmpty()) {
				RoboRequestEntity roboRequestEntity = availablePathValues.stream().findFirst().get();
				if (uri != null && uri.getQuery() != null && !uri.getQuery().isEmpty()) {
					final Map<String, String> currentRequestValues = RoboHttpUtils.parseURIQueryToMap(uri.getQuery(),
							ConstantUtil.HTTP_QUERY_SEP);
					//@formatter:off


                    return currentRequestValues.entrySet().stream()
							.filter(e -> roboRequestEntity.getValues().containsValue(e.getValue()))
                            .map(e -> roboRequestEntity.getValues().entrySet()
									.stream().filter(v -> v.getValue().equals(e.getValue())).findFirst().get())
                            .findFirst()
							.map(Map.Entry::getKey)
                            .orElse(RoboHttpUtils._EMPTY_STRING);
                    //@formatter:on
				}
			}

		} else {
			SimpleLoggingUtil.error(getClass(), "processGet is corrupted: " + wrapper);
		}

		return null;
	}

	@Override
	public String processPost(HttpMessageWrapper<?> wrapper) {
		System.out.println("processPost NOT IMPLEMENTED");
		System.out.println("processPost message: " + wrapper.message());
		System.out.println("processPost body: " + wrapper.body());
		return null;
	}

	@Override
	public void setRoboUnits(List<RoboUnit<?>> units) {
		this.units = units;
	}

	@Override
	public List<RoboUnit<?>> getRoboUnits() {
		return units;
	}
}
