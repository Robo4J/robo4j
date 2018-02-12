package com.robo4j.net;

import java.net.InetAddress;

/**
 * Internal bookkeeping class for discoveries.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class RoboContextDescriptorEntry {
	public RoboContextDescriptor descriptor;
	public long lastAccess;
	public InetAddress address;
}
