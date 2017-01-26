/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This RoboRequestFactory.java  is part of robo4j.
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

import com.robo4j.core.client.util.HttpUtils;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.http.HttpMessage;
import com.robo4j.http.HttpVersion;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Request Factory is singleton
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @since 25.01.2017
 */
public class RoboRequestFactory implements RoboRequestFactoryI<String>{

    private static volatile RoboRequestFactory INSTANCE;

    public RoboRequestFactory() {
    }

    public static RoboRequestFactory getInstance() {
        if (INSTANCE == null) {
            synchronized (RoboRequestFactory.class) {
                if (INSTANCE == null) {
                    INSTANCE = new RoboRequestFactory();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public String processGet(HttpMessage httpMessage) {

        if (HttpVersion.containsValue(httpMessage.getVersion())) {
            final URI uri = httpMessage.getUri();
            final List<String> paths = Arrays
                    .stream(httpMessage.getUri().getPath().split(ConstantUtil.getHttpSeparator(12)))
                    .filter(e -> !e.isEmpty()).collect(Collectors.toList());
            SimpleLoggingUtil.debug(getClass(), "path: " + paths);
            if(uri != null && uri.getQuery() != null && !uri.getQuery().isEmpty()){
                final Map<String, String> queryValues = HttpUtils.parseURIQueryToMap(uri.getQuery(),
                        ConstantUtil.HTTP_QUERY_SEP);
                SimpleLoggingUtil.debug(getClass(), "queryValues: " + queryValues);

                switch (queryValues.get("type")){
                    case "lcd":
                        return queryValues.get("command");
                    default:

                }
            }
        } else {
            SimpleLoggingUtil.error(getClass(), "processGet is corrupted: " + httpMessage);
        }

        return null;
    }
}
