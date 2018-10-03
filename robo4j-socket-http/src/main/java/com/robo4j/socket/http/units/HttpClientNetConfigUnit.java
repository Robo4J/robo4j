/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.util.Map;

/**
 * CameraNetUnit allows to reconfigure HttpClient
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

@SuppressWarnings({"unchecked", "rawtypes"})
public class HttpClientNetConfigUnit extends RoboUnit<Map> {

    public static final String PROPERTY_TARGET = "target";
    private String target;

	public HttpClientNetConfigUnit(RoboContext context, String id) {
		super(Map.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString(PROPERTY_TARGET, null);
		if (target == null) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_TARGET);
		}
	}

	@Override
	public void onMessage(Map message) {
		Map<String, String> map = (Map<String, String>) message;
		SimpleLoggingUtil.info(getClass(), "RECEIVED: " + map);

		String host = map.get(RoboHttpUtils.PROPERTY_HOST);
		Integer port = Integer.valueOf(map.get(RoboHttpUtils.PROPERTY_SOCKET_PORT));

		RoboReference<HttpDecoratedRequest> httpUnitRef = getContext().getReference(target);
		HttpDecoratedRequest confMessage = new HttpDecoratedRequest();
		confMessage.setHost(host);
		confMessage.setPort(port);
		httpUnitRef.sendMessage(confMessage);

	}
}
