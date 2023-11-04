/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.rpi.gps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.gps.GPSEvent;
import com.robo4j.hw.rpi.gps.GPSListener;
import com.robo4j.hw.rpi.gps.PositionEvent;
import com.robo4j.hw.rpi.gps.VelocityEvent;
import com.robo4j.hw.rpi.i2c.gps.TitanX1GPS;
import com.robo4j.logging.SimpleLoggingUtil;

/**
 * Unit for getting GPS data.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class TitanGPSUnit extends RoboUnit<GPSRequest> {
	private TitanX1GPS titangps;
	private List<GPSEventListener> listeners = new ArrayList<>();

	// The future, if scheduled with the platform scheduler
	private volatile ScheduledFuture<?> scheduledFuture;

	private static class GPSEventListener implements GPSListener {
		private RoboReference<GPSEvent> target;

		GPSEventListener(RoboReference<GPSEvent> target) {
			this.target = target;
		}

		@Override
		public void onPosition(PositionEvent event) {
			target.sendMessage(event);
		}

		@Override
		public void onVelocity(VelocityEvent event) {
			target.sendMessage(event);
		}
	}

	public TitanGPSUnit(RoboContext context, String id) {
		super(GPSRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		// TODO: Configuration for bus, address etc
		try {
			titangps = new TitanX1GPS();
		} catch (IOException e) {
			throw new ConfigurationException("Could not instantiate GPS!", e);
		}
		titangps.start();
	}

	@Override
	public <R> Future<R> getAttribute(AttributeDescriptor<R> attribute) {
		return super.getAttribute(attribute);
	}

	@Override
	public void onMessage(GPSRequest message) {
		super.onMessage(message);
		RoboReference<GPSEvent> targetReference = message.getTarget();
		switch (message.getOperation()) {
		case REGISTER:
			register(targetReference);
			break;
		case UNREGISTER:
			unregister(targetReference);
			break;
		default:
			SimpleLoggingUtil.error(getClass(), "Unknown operation: " + message.getOperation());
			break;
		}
	}

	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		return super.onGetAttribute(descriptor);
	}

	@Override
	public void shutdown() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(false);
		}
		titangps.shutdown();
		super.shutdown();
	}

	// Private Methods
	private synchronized void unregister(RoboReference<GPSEvent> targetReference) {
		List<GPSEventListener> copy = new ArrayList<>(listeners);
		for (GPSEventListener listener : copy) {
			if (targetReference.equals(listener.target)) {
				listeners.remove(listener);
				titangps.removeListener(listener);
				// I guess you could theoretically have several registered to
				// the same target, so let's keep checking...
			}
		}
	}

	private synchronized void register(RoboReference<GPSEvent> targetReference) {
		GPSEventListener listener = new GPSEventListener(targetReference);
		listeners.add(listener);
		titangps.addListener(listener);
	}
}
