/*
 * Copyright (c) 2014, 2025, Marcus Hirt, Miroslav Wengner
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

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.robo4j.net.NetworkUtil.IpFamily;

/**
 * Builder helps to build {@link LookupService}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DefaultLookupServiceBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultLookupServiceBuilder.class);
	private Integer port;
	private Float missedHeartbeatsBeforeRemoval;
	private LocalLookupServiceImpl localContexts;

	private DefaultLookupServiceBuilder() {

	}

	public static DefaultLookupServiceBuilder Build() {
		return new DefaultLookupServiceBuilder();
	}


	public DefaultLookupServiceBuilder setPort(int port) {
		this.port = port;
		return this;
	}

	public DefaultLookupServiceBuilder setMissedHeartbeatsBeforeRemoval(float missedHeartbeatsBeforeRemoval) {
		this.missedHeartbeatsBeforeRemoval = missedHeartbeatsBeforeRemoval;
		return this;
	}

	public DefaultLookupServiceBuilder setLocalContexts(LocalLookupServiceImpl localContexts) {
		this.localContexts = localContexts;
		return this;
	}

	public LookupService build() {
		try {
			// Do all the smart network detection here
			final String groupOverride = System.getProperty(LookupServiceProvider.PROP_GROUP);
			final IpFamily family = NetworkUtil.decideFamily(groupOverride);
			final InetAddress multicastGroup = LookupServiceProvider.getMulticastGroup(family);
			final NetworkInterface networkInterface = NetworkUtil.pickMulticastInterface(family);

			return new LookupServiceImpl(multicastGroup, networkInterface, port, missedHeartbeatsBeforeRemoval, localContexts);
		} catch (IOException e) {
			LOGGER.warn("Failed to set up LookupService! No multicast route? Will use null provider: {}", e.getMessage());
			return new NullLookupService();
		}
	}
}
