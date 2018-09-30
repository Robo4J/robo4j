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

import com.robo4j.RoboContext;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Use this to get a reference to the default lookup service. It can also be
 * used for creating a new lookup service.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class LookupServiceProvider {
	public static final String DEFAULT_MULTICAST_ADDRESS = "238.12.15.254";
	public static final int DEFAULT_PORT = 0x0FFE;
	private static final float DEFAULT_HEARTBEATS_BEFORE_REMOVAL = 3.5f;
	private static final LocalLookupServiceImpl LOCAL_CONTEXTS = new LocalLookupServiceImpl();
	private static final AtomicReference<LookupService> DEFAULT_SERVICE = new AtomicReference<LookupService>(createDefaultService());

	/**
	 * @return the default lookup service.
	 */
	public static LookupService getDefaultLookupService() {
		return DEFAULT_SERVICE.get();
	}

	public static void registerLocalContext(RoboContext ctx) {
		LOCAL_CONTEXTS.addContext(ctx);
	}

	public static void setDefaultLookupService(LookupService lookupService) {
		DEFAULT_SERVICE.set(lookupService);
	}

	private static LookupService createDefaultService() {
		//@formatter:off
		return DefaultLookupServiceBuilder.Build()
				.setAddress(DEFAULT_MULTICAST_ADDRESS)
				.setPort(DEFAULT_PORT)
				.setMissedHeartbeatsBeforeRemoval(DEFAULT_HEARTBEATS_BEFORE_REMOVAL)
				.setLocalContexts(LOCAL_CONTEXTS)
				.build();
		//@formatter:on
	}
}
