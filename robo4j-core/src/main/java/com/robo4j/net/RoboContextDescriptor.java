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
package com.robo4j.net;

import java.util.Map;

/**
 * A descriptor for a RoboContext on the network.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboContextDescriptor {
	public static final String KEY_URI = "uri";

	private final String id;
	private final int heartBeatInterval;
	private final Map<String, String> metaData;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the robo reference id.
	 * @param heartBeatInterval
	 *            how often to send a heart beat
	 * @param metaData
	 *            the metadata describing the RoboContext.
	 */
	public RoboContextDescriptor(String id, int heartBeatInterval, Map<String, String> metaData) {
		this.id = id;
		this.heartBeatInterval = heartBeatInterval;
		this.metaData = metaData;
	}

	public String getId() {
		return id;
	}

	public Map<String, String> getMetadata() {
		return metaData;
	}

	/**
	 * The heart beat interval for this RoboContext, in ms.
	 * 
	 * @return the heart beat interval in ms.
	 */
	public int getHeartBeatInterval() {
		return heartBeatInterval;
	}

	@Override
	public String toString() {
		return "RoboContextDescriptor [id=" + id + ", heartbeat=" + heartBeatInterval + ", metadata=[" + metaData.toString() + "]";
	}
}
