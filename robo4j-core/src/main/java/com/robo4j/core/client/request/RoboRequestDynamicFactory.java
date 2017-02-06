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

import com.robo4j.core.client.util.HttpUtils;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.unit.HttpDynamicUnit;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.http.HttpMessage;
import com.robo4j.http.HttpVersion;

/**
 * Dynamically configurable request factory
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 05.02.2017
 */
public class RoboRequestDynamicFactory implements DefaultRequestFactory<String> {

	private static final int DEFAULT_POSITION_0 = 0;

	public RoboRequestDynamicFactory() {
	}

	@Override
	public String processGet(HttpMessage httpMessage) {
		if (HttpVersion.containsValue(httpMessage.getVersion())) {
			final URI uri = httpMessage.getUri();
			//@formatter:off
            final List<String> paths = Stream.of(httpMessage.getUri().getPath()
                        .split(ConstantUtil.getHttpSeparator(12)))
                    .filter(e -> !e.isEmpty())
                    .collect(Collectors.toList());
            //@formatter:on

			// TODO: support more paths
			SimpleLoggingUtil.debug(getClass(), "path: " + paths);
			String path = paths.get(DEFAULT_POSITION_0);
			Set<RoboRequestElement> availablePathValues = RoboRequestTypeRegistry.getInstance().getPathValues(path);

			if (!availablePathValues.isEmpty()) {
				if (uri != null && uri.getQuery() != null && !uri.getQuery().isEmpty()) {
					final Map<String, String> currentRequestValues = HttpUtils.parseURIQueryToMap(uri.getQuery(),
							ConstantUtil.HTTP_QUERY_SEP);
					//@formatter:off
                    return currentRequestValues.entrySet().stream()
                            .filter(e -> availablePathValues.stream()
                                    .filter(ac -> ac.getKey().equals(e.getKey()))
                                    .filter(ac -> ac.getValues().contains(e.getValue()))
                                    .count() > 0)
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(HttpDynamicUnit._DEFAULT_COMMAND);
                    //@formatter:on
				}
			}

		} else {
			SimpleLoggingUtil.error(getClass(), "processGet is corrupted: " + httpMessage);
		}

		return null;
	}
}
