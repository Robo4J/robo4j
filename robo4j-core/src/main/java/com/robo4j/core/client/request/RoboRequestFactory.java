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
import java.util.Map;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.RoboReference;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.httpunit.HttpUriRegister;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.http.HttpMessageWrapper;
import com.robo4j.http.HttpVersion;

/**
 * Dynamically configurable request factory
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboRequestFactory implements DefaultRequestFactory<Object> {

	public RoboRequestFactory() {
	}

	@Override
	public Object processGet(final RoboReference<?> desiredUnit, final String path, final HttpMessageWrapper<?> wrapper) {
		if (HttpVersion.containsValue(wrapper.message().version())) {
			final URI uri = wrapper.message().uri();
			/* currently is supported only */
			final HttpUriRegister register = HttpUriRegister.getInstance();
			if (register.isUnitAvailable(path)) {
				/* currently is supported only one http unit */
				final Map<String, String> tmpQueryParsed = RoboHttpUtils.parseURIQueryToMap(uri.getQuery(),
						ConstantUtil.HTTP_QUERY_SEP);
				// @formatter:off

				final AttributeDescriptor<?> descriptor = desiredUnit.getKnownAttributes().stream()
						.filter(a -> tmpQueryParsed.containsKey(a.getAttributeName()))
						.findFirst().orElse(null);
				//TODO: make validation
				return desiredUnit.getAttribute(descriptor);
				// @formatter:on
			}

		} else {
			SimpleLoggingUtil.error(getClass(), "processGet is corrupted: " + wrapper);
		}
		return null;
	}

	@Override
	public String processPost(final RoboReference<?> desiredUnit, final String path, final HttpMessageWrapper<?> wrapper) {
		System.out.println("processPost NOT IMPLEMENTED");
		System.out.println("processPost unit: " + desiredUnit);
		System.out.println("processPost path: " + path);
		System.out.println("processPost message: " + wrapper.message());
		System.out.println("processPost body: " + wrapper.body());
		return null;
	}

}
