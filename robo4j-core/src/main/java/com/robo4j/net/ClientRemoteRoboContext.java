package com.robo4j.net;

import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.scheduler.Scheduler;

import java.net.InetAddress;
import java.util.Collection;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ClientRemoteRoboContext implements RoboContext {

	private RoboContextDescriptorEntry descriptorEntry;

	public ClientRemoteRoboContext(RoboContextDescriptorEntry descriptorEntry) {
		this.descriptorEntry = descriptorEntry;
	}
	
	@Override
	public LifecycleState getState() {
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> RoboReference<T> getReference(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<RoboReference<?>> getUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scheduler getScheduler() {
		throw new UnsupportedOperationException("Accessing the Scheduler remotely is not supported. Use the local scheduler.");
	}	

	@Override
	public String getId() {
		return descriptorEntry.descriptor.getId();
	}

	public InetAddress getAddress() {
		return descriptorEntry.address;
	}

	@Override
	public Configuration getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}
}
