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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robo4j.core.AttributeDescriptor;
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
public class RoboRequestFactory implements DefaultRequestFactory<Object> {

	private static final int DEFAULT_POSITION_0 = 0;

	private List<RoboUnit<?>> units = null;

	public RoboRequestFactory() {
	}

	@Override
	public Object processGet(HttpMessageWrapper<?> wrapper) {
		if (HttpVersion.containsValue(wrapper.message().version())) {
			final URI uri = wrapper.message().uri();
			//@formatter:off
            final List<String> paths = Stream.of(wrapper.message().uri().getPath()
                        .split(HttpMessageUtil.getHttpSeparator(12)))
                    .filter(e -> !e.isEmpty())
                    .collect(Collectors.toList());
            //@formatter:on

			/* currently is supported only */
			// TODO: support more paths
			SimpleLoggingUtil.debug(getClass(), "path: " + paths);
			if (units != null) {
				/* currently is supported only one http unit */
				final RoboUnit<?> desiredUnit = units.get(DEFAULT_POSITION_0);
				final Map<String, String> tmpQueryParsed = RoboHttpUtils.parseURIQueryToMap(uri.getQuery(),
						ConstantUtil.HTTP_QUERY_SEP);

				// @formatter:
				final AttributeDescriptor descriptor = desiredUnit.getKnownAttributes().stream()
						.filter(a -> tmpQueryParsed.containsKey(a.getAttributeName()))
						.findFirst().orElse(null);
				System.out.println(getClass().getSimpleName() + " descriptor: " + descriptor);
				final Object result = desiredUnit.getMessageAttribute(descriptor,
						tmpQueryParsed.get(descriptor.getAttributeName()));
				System.out.println(getClass().getSimpleName() + " result " + result);
				return result;
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
