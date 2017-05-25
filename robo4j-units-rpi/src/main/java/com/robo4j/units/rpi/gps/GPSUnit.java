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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.DefaultAttributeDescriptor;
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
import com.robo4j.math.geometry.Float3D;

/**
 * Unit for getting GPS data.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GPSUnit extends RoboUnit<GPSRequest> {
	/**
	 * This key configures the approximate update interval, or how often to
	 * schedule reads from the serial port.
	 */
	public static final String PROPERTY_KEY_READ_INTERVAL = "readInterval";

	/**
	 * This key configures the scheduler to use for scheduling reads. Either
	 * PLATFORM or INTERNAL. Use INTERNAL if the reads take too long and start
	 * disrupting the platform scheduler too much.
	 */
	public static final String PROPERTY_KEY_SCHEDULER = "scheduler";

	/**
	 * Value for the scheduler key for using the platform scheduler.
	 */
	public static final String PROPERTY_VALUE_PLATFORM_SCHEDULER = "platform";

	/**
	 * Value for the scheduler key for using the internal scheduler.
	 */
	public static final String PROPERTY_VALUE_INTERNAL_SCHEDULER = "internal";

	/**
	 * This is the default value for the read interval.
	 */
	public static final int DEFAULT_READ_INTERVAL = 550;

	/**
	 * This attribute will provide the state of the read interval.
	 */
	public static final String ATTRIBUTE_NAME_READ_INTERVAL = "readInterval";

	public static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections
			.unmodifiableCollection(Arrays.asList(DefaultAttributeDescriptor.create(Float3D.class, ATTRIBUTE_NAME_READ_INTERVAL)));

	private GPS gps;
	private int readInterval = DEFAULT_READ_INTERVAL;
	private List<GPSEventListener> listeners = new ArrayList<>();

	// The future, if scheduled with the platform scheduler
	private volatile ScheduledFuture<?> scheduledFuture;

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
		readInterval = configuration.getInteger("readInterval", DEFAULT_READ_INTERVAL);
		String scheduler = configuration.getString("scheduler", PROPERTY_VALUE_PLATFORM_SCHEDULER);
		boolean usePlatformScheduler = PROPERTY_VALUE_PLATFORM_SCHEDULER.equals(scheduler) ? true : false;

		try {
			gps = new GPS(readInterval);
		} catch (IOException e) {
			throw new ConfigurationException("Could not instantiate GPS!", e);
		}
		if (usePlatformScheduler) {
			scheduledFuture = getContext().getScheduler().scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					gps.update();
				}
			}, 10, readInterval, TimeUnit.MILLISECONDS);
		} else {
			gps.startAutoUpdate();
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

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeType() == Integer.class && descriptor.getAttributeName().equals(ATTRIBUTE_NAME_READ_INTERVAL)) {
			return (R) Integer.valueOf(readInterval);
		}
		return super.onGetAttribute(descriptor);
	}

	@Override
	public void shutdown() {
		if (scheduledFuture != null) {
			scheduledFuture.cancel(true);
		}
		gps.shutdown();
		super.shutdown();
	}
}
