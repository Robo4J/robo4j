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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.hw.rpi.serial.gps.GPS;
import com.robo4j.hw.rpi.serial.gps.GPSEvent;
import com.robo4j.hw.rpi.serial.gps.GPSListener;
import com.robo4j.hw.rpi.serial.gps.PositionEvent;
import com.robo4j.hw.rpi.serial.gps.VelocityEvent;

/**
 * Unit for getting GPS data.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GPSUnit extends RoboUnit<GPSRequest> {
	private GPS gps;
	private List<GPSEventListener> listeners = new ArrayList<>();

	private static class GPSEventListener implements GPSListener {
		private RoboReference<GPSEvent> target;

		GPSEventListener(RoboReference<GPSEvent> target) {
			this.target = target;
		}

		@Override
		public void onEvent(PositionEvent event) {
			target.sendMessage(event);
		}

		@Override
		public void onEvent(VelocityEvent event) {
			target.sendMessage(event);
		}
	}

	public GPSUnit(RoboContext context, String id) {
		super(GPSRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		try {
			gps = new GPS();
		} catch (IOException e) {
			throw new ConfigurationException("Could not instantiate GPS!", e);
		}
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

	private synchronized void unregister(RoboReference<GPSEvent> targetReference) {
		List<GPSEventListener> copy = new ArrayList<>(listeners);
		for (GPSEventListener listener : copy) {
			if (targetReference.equals(listener.target)) {
				listeners.remove(listener);
				gps.removeListener(listener);
				// I guess you could theoretically have several registered to
				// the same target, so let's keep checking...
			}
		}
	}

	private synchronized void register(RoboReference<GPSEvent> targetReference) {
		GPSEventListener listener = new GPSEventListener(targetReference);
		listeners.add(listener);
		gps.addListener(listener);
	}

	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		return super.onGetAttribute(descriptor);
	}
}
