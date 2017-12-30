package com.robo4j.net;

import java.net.InetAddress;

/**
 * Internal bookkeeping class for discoveries.
 */
class RoboContextDescriptorEntry {
	public RoboContextDescriptor descriptor;
	public long lastAccess;
	public InetAddress address;
}
