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
import java.util.concurrent.atomic.AtomicReference;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.net.NetworkUtil.IpFamily;

/**
 * Use this to get a reference to the default lookup service. It can also be
 * used for creating a new lookup service.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class LookupServiceProvider {
	public static final String DEFAULT_V4_GROUP = "238.12.15.254";
	public static final String DEFAULT_V6_GROUP = "ff02::1234";
	public static final int DEFAULT_PORT = 0x0FFE;
	private static final float DEFAULT_HEARTBEATS_BEFORE_REMOVAL = 3.5f;
	private static final LocalLookupServiceImpl LOCAL_CONTEXTS = new LocalLookupServiceImpl();
	private static final AtomicReference<LookupService> DEFAULT_SERVICE = new AtomicReference<LookupService>(createDefaultService());

	// System property names for multicast configuration
	public static final String PROP_IFADDR = "robo4j.net.ifaddr";   // IPv4 or IPv6 address
	public static final String PROP_IFACE = "robo4j.net.iface";     // interface name
	public static final String PROP_FAMILY = "robo4j.net.family";   // auto|ipv4|ipv6 (default: auto)
	public static final String PROP_GROUP = "robo4j.net.group";     // overrides group for the chosen family

	/**
	 * @return the default lookup service.
	 */
	public static LookupService getDefaultLookupService() {
		return DEFAULT_SERVICE.get();
	}

	public static void registerLocalContext(RoboContext ctx) {
		// Check if this context is running in standalone mode - if so, skip discovery
		if (isStandaloneModeIntended(ctx)) {
			// Don't register for discovery when in standalone mode
			return;
		}
		LOCAL_CONTEXTS.addContext(ctx);
	}

	public static void setDefaultLookupService(LookupService lookupService) {
		DEFAULT_SERVICE.set(lookupService);
	}

	/**
	 * Gets the multicast group address for the given IP family, considering system property overrides.
	 *
	 * @param family the IP family
	 * @return the multicast group address
	 * @throws IOException if the address cannot be resolved
	 */
	public static InetAddress getMulticastGroup(IpFamily family) throws IOException {
		final String groupOverride = System.getProperty(PROP_GROUP);
		final String groupAddress = (groupOverride != null && !groupOverride.isBlank())
			? groupOverride
			: (family == IpFamily.IPV4 ? DEFAULT_V4_GROUP : DEFAULT_V6_GROUP);
		return InetAddress.getByName(groupAddress);
	}

	/**
	 * Checks if the context is running in standalone mode and should skip discovery.
	 */
	private static boolean isStandaloneModeIntended(RoboContext ctx) {
		Configuration rootConfig = ctx.getConfiguration();
		
		if (rootConfig != null) {
			// Check for explicit standalone mode in configuration
			if (rootConfig.getBoolean("standalone", false)) {
				return true;
			}
			
			// Check for explicit networking disable in configuration
			if (!rootConfig.getBoolean("networking.enabled", true)) {
				return true;
			}
			
			// If no server or emitter configuration exists, assume standalone intent
			Configuration serverConfig = rootConfig.getChildConfiguration(RoboBuilder.KEY_CONFIGURATION_SERVER);
			Configuration emitterConfig = rootConfig.getChildConfiguration(RoboBuilder.KEY_CONFIGURATION_EMITTER);
			
			boolean hasServerConfig = serverConfig != null && serverConfig.getValueNames().size() > 0;
			boolean hasEmitterConfig = emitterConfig != null && emitterConfig.getValueNames().size() > 0;
			
			// If neither server nor emitter is configured, assume standalone intent
			if (!hasServerConfig && !hasEmitterConfig) {
				return true;
			}
		} else {
			// No configuration at all (e.g., new RoboSystem()) - clearly standalone intent
			return true;
		}
		
		// Check system properties for explicit declaration
		String mode = System.getProperty("robo4j.mode");
		if ("standalone".equalsIgnoreCase(mode)) {
			return true;
		}
		
		String networkingDisabled = System.getProperty("robo4j.networking.disabled");
		if ("true".equalsIgnoreCase(networkingDisabled)) {
			return true;
		}
		
		return false;
	}

	private static LookupService createDefaultService() {
		//@formatter:off
		return DefaultLookupServiceBuilder.Build()
				.setPort(DEFAULT_PORT)
				.setMissedHeartbeatsBeforeRemoval(DEFAULT_HEARTBEATS_BEFORE_REMOVAL)
				.setLocalContexts(LOCAL_CONTEXTS)
				.build();
		//@formatter:on
	}
}
