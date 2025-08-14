/*
 * Copyright (c) 2025, Marcus Hirt, Miroslav Wengner
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
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Network utility class for common networking operations.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class NetworkUtil {

	/**
	 * IP family enumeration for multicast configuration.
	 */
	public enum IpFamily {
		IPV4, IPV6
	}

	// Cache for network interface lookups to avoid repeated expensive operations
	private static final ConcurrentMap<IpFamily, NetworkInterface> INTERFACE_CACHE = new ConcurrentHashMap<>();
	private static final ConcurrentMap<IpFamily, Boolean> CAPABILITY_CACHE = new ConcurrentHashMap<>();

	private NetworkUtil() {
		// Utility class
	}

	/**
	 * Checks if there exists at least one multicast-capable interface for the given IP family.
	 *
	 * @param family the IP family to check
	 * @return true if a multicast-capable interface exists for the family
	 * @throws SocketException if an I/O error occurs
	 */
	public static boolean existsMulticastCapable(IpFamily family) throws SocketException {
		// Check cache first
		Boolean cached = CAPABILITY_CACHE.get(family);
		if (cached != null) {
			return cached;
		}

		for (final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
			final NetworkInterface ni = e.nextElement();
			if (!ni.isUp() || ni.isLoopback() || !ni.supportsMulticast()) continue;
			if (hasFamily(ni, family)) {
				CAPABILITY_CACHE.put(family, true);
				return true;
			}
		}
		CAPABILITY_CACHE.put(family, false);
		return false;
	}

	/**
	 * Checks if the given network interface has addresses for the specified IP family.
	 *
	 * @param ni the network interface to check
	 * @param family the IP family to look for
	 * @return true if the interface has addresses for the specified family
	 */
	public static boolean hasFamily(NetworkInterface ni, IpFamily family) {
		return ni.getInterfaceAddresses().stream().anyMatch(ia ->
			(family == IpFamily.IPV4 && ia.getAddress() instanceof Inet4Address) ||
			(family == IpFamily.IPV6 && ia.getAddress() instanceof Inet6Address)
		);
	}

	/**
	 * Picks a suitable multicast interface for the given IP family based on system properties.
	 *
	 * @param family the IP family for the interface
	 * @return a suitable NetworkInterface for multicast
	 * @throws IOException if no suitable interface is found or configuration error
	 */
	public static NetworkInterface pickMulticastInterface(IpFamily family) throws IOException {
		final String ifAddrProp = System.getProperty(LookupServiceProvider.PROP_IFADDR);
		final String ifNameProp = System.getProperty(LookupServiceProvider.PROP_IFACE);

		if (ifAddrProp != null && !ifAddrProp.isBlank()) {
			final InetAddress addr = InetAddress.getByName(ifAddrProp);
			if (family == IpFamily.IPV4 && !(addr instanceof Inet4Address)) {
				throw new SocketException("Expected IPv4 in " + LookupServiceProvider.PROP_IFADDR + ": " + ifAddrProp);
			}
			if (family == IpFamily.IPV6 && !(addr instanceof Inet6Address)) {
				throw new SocketException("Expected IPv6 in " + LookupServiceProvider.PROP_IFADDR + ": " + ifAddrProp);
			}
			final NetworkInterface ni = NetworkInterface.getByInetAddress(addr);
			if (ni == null) throw new SocketException("No interface for address: " + ifAddrProp);
			if (!ni.isUp() || !ni.supportsMulticast() || !hasFamily(ni, family)) {
				throw new SocketException("Interface not up/multicast/" + family + ": " + ni.getName());
			}
			return ni;
		}

		if (ifNameProp != null && !ifNameProp.isBlank()) {
			final NetworkInterface ni = NetworkInterface.getByName(ifNameProp);
			if (ni == null) throw new SocketException("No such interface: " + ifNameProp);
			if (!ni.isUp() || !ni.supportsMulticast() || !hasFamily(ni, family)) {
				throw new SocketException("Interface not up/multicast/" + family + ": " + ifNameProp);
			}
			return ni;
		}

		// Check cache first for auto-detected interface
		NetworkInterface cached = INTERFACE_CACHE.get(family);
		if (cached != null) {
			// Verify the cached interface is still valid
			try {
				if (cached.isUp() && !cached.isLoopback() && cached.supportsMulticast() && hasFamily(cached, family)) {
					return cached;
				} else {
					// Interface state changed, remove from cache
					INTERFACE_CACHE.remove(family);
				}
			} catch (SocketException e) {
				// Interface no longer accessible, remove from cache
				INTERFACE_CACHE.remove(family);
			}
		}

		// Auto-detect interface - sort by name for deterministic selection
		final List<NetworkInterface> candidates = new ArrayList<>();
		for (final Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces(); e.hasMoreElements();) {
			final NetworkInterface ni = e.nextElement();
			if (!ni.isUp() || ni.isLoopback() || !ni.supportsMulticast()) continue;
			if (hasFamily(ni, family)) {
				candidates.add(ni);
			}
		}
		
		if (!candidates.isEmpty()) {
			// Sort by interface name for deterministic selection
			candidates.sort(Comparator.comparing(NetworkInterface::getName));
			final NetworkInterface selected = candidates.get(0);
			INTERFACE_CACHE.put(family, selected);
			return selected;
		}
		throw new SocketException("No multicast-capable interface for family: " + family);
	}

	/**
	 * Determines the IP family to use based on system properties and available interfaces.
	 *
	 * @param groupOverride the group override property value (can be null)
	 * @return the IP family to use
	 * @throws IOException if family determination fails
	 */
	public static IpFamily decideFamily(String groupOverride) throws IOException {
		final String f = System.getProperty(LookupServiceProvider.PROP_FAMILY, "auto").toLowerCase();
		if ("ipv4".equals(f)) return IpFamily.IPV4;
		if ("ipv6".equals(f)) return IpFamily.IPV6;
		// auto:
		if (groupOverride != null && !groupOverride.isBlank()) {
			final InetAddress a = InetAddress.getByName(groupOverride);
			return (a instanceof Inet6Address) ? IpFamily.IPV6 : IpFamily.IPV4;
		}
		// Prefer IPv4 if available; otherwise IPv6; otherwise fail
		if (existsMulticastCapable(IpFamily.IPV4)) return IpFamily.IPV4;
		if (existsMulticastCapable(IpFamily.IPV6)) return IpFamily.IPV6;
		throw new SocketException("No multicast-capable interface found (neither IPv4 nor IPv6).");
	}
}