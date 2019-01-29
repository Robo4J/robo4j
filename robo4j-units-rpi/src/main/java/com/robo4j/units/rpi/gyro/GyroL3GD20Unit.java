/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.rpi.gyro;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.WorkTrait;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.gyro.CalibratedGyro;
import com.robo4j.hw.rpi.i2c.gyro.GyroL3GD20Device;
import com.robo4j.hw.rpi.i2c.gyro.GyroL3GD20Device.Sensitivity;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.math.geometry.Tuple3f;
import com.robo4j.units.rpi.I2CRoboUnit;
import com.robo4j.units.rpi.gyro.GyroRequest.GyroAction;

/**
 * Gyro unit based on the {@link GyroL3GD20Device}. Note that there can only be
 * ONE active notification threshold per target. If a new one is registered
 * before it has triggered, the new one will replace the old one.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@WorkTrait
public class GyroL3GD20Unit extends I2CRoboUnit<GyroRequest> {
	/**
	 * This key configures the sensitivity of the gyro. Use the name of the
	 * Sensitivity enum. Default is DPS_245.
	 * 
	 * @see Sensitivity
	 */
	public static final String PROPERTY_KEY_SENSITIVITY = "sensitivity";

	/**
	 * This key configures the high pass filter. Set to true to enable. Default
	 * is true.
	 * 
	 * @see GyroL3GD20Device
	 */
	public static final String PROPERTY_KEY_HIGH_PASS_FILTER = "enableHighPass";

	/**
	 * This key configures how often to read the gyro in ms. Default is 10.
	 */
	public static final String PROPERTY_KEY_PERIOD = "period";

	/**
	 * This attribute will provide the state of the gyro as a {@link Tuple3f}.
	 */
	public static final String ATTRIBUTE_NAME_STATE = "state";

	public static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections
			.unmodifiableCollection(Arrays.asList(DefaultAttributeDescriptor.create(Tuple3f.class, ATTRIBUTE_NAME_STATE)));

	private final Map<RoboReference<GyroEvent>, GyroNotificationEntry> activeThresholds = new HashMap<>();

	private final GyroScanner scanner = new GyroScanner();

	private Sensitivity sensitivity;
	private boolean highPassFilter;
	private int period;
	private CalibratedGyro gyro;
	private volatile ScheduledFuture<?> readings;

	private class GyroScanner implements Runnable {
		private long lastReadingTime = System.currentTimeMillis();
		private Tuple3f lastReading = new Tuple3f(0f, 0f, 0f);

		@Override
		public void run() {
			Tuple3f data = read();
			long newTime = System.currentTimeMillis();

			// Trapezoid
			Tuple3f tmp = new Tuple3f(data);
			long deltaTime = newTime - lastReadingTime;
			data.add(lastReading);
			data.multiplyScalar(deltaTime / 2000.0f);

			lastReading.set(tmp);
			addToDeltas(data);
			lastReadingTime = newTime;
		}

		private void addToDeltas(Tuple3f data) {
			synchronized (GyroL3GD20Unit.this) {
				for (GyroNotificationEntry notificationEntry : activeThresholds.values()) {
					notificationEntry.addDelta(data);
				}
			}
		}

		private void reset() {
			lastReadingTime = System.currentTimeMillis();
			lastReading = read();
		}

		private Tuple3f read() {
			try {
				return gyro.read();
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Could not read gyro, aborting.", e);
				return null;
			}
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            the robo context.
	 * @param id
	 *            the robo unit id.
	 */
	public GyroL3GD20Unit(RoboContext context, String id) {
		super(GyroRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		sensitivity = Sensitivity.valueOf(configuration.getString(PROPERTY_KEY_SENSITIVITY, "DPS_245"));
		period = configuration.getInteger(PROPERTY_KEY_PERIOD, 10);
		highPassFilter = configuration.getBoolean(PROPERTY_KEY_HIGH_PASS_FILTER, true);
		try {
			gyro = new CalibratedGyro(new GyroL3GD20Device(getBus(), getAddress(), sensitivity, highPassFilter));
		} catch (IOException e) {
			throw new ConfigurationException(String.format(
					"Failed to initialize lidar device. Make sure it is hooked up to bus: %d address: %xd", getBus(), getAddress()), e);
		}
	}

	@Override
	public Collection<AttributeDescriptor<?>> getKnownAttributes() {
		return KNOWN_ATTRIBUTES;
	}

	@Override
	public void onMessage(GyroRequest message) {
		RoboReference<GyroEvent> notificationTarget = message.getTarget();
		switch (message.getAction()) {
		case CALIBRATE:
			try {
				gyro.calibrate();
				scanner.reset();
				if (notificationTarget != null) {
					notificationTarget.sendMessage(new GyroEvent(new Tuple3f(0, 0, 0)));
				}
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Failed to calibrate!", e);
			}
			break;
		case STOP:
			stopForNotificationTarget(notificationTarget);
			break;
		case ONCE:
		case CONTINUOUS:
			if (message.getNotificationThreshold() != null) {
				setUpNotification(message);
			}
			break;
		}
	}

	private void stopForNotificationTarget(RoboReference<GyroEvent> notificationTarget) {
		synchronized (this) {
			if (notificationTarget == null) {
				activeThresholds.clear();
			} else {
				activeThresholds.remove(notificationTarget);
			}
			if (activeThresholds.isEmpty()) {
				if (readings != null) {
					readings.cancel(false);
					readings = null;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeType() == Tuple3f.class && descriptor.getAttributeName().equals(ATTRIBUTE_NAME_STATE)) {
			try {
				return (R) gyro.read();
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Failed to read the gyro!", e);
			}
		}
		return super.onGetAttribute(descriptor);
	}

	private void setUpNotification(GyroRequest request) {
		synchronized (this) {
			if (request.getAction() == GyroAction.CONTINUOUS) {
				activeThresholds.put(request.getTarget(), new ContinuousGyroNotificationEntry(request.getTarget(), request.getNotificationThreshold()));
			} else {
				activeThresholds.put(request.getTarget(), new FixedGyroNotificationEntry(request.getTarget(), request.getNotificationThreshold()));
			}
		}
		if (readings == null) {
			synchronized (this) {
				readings = getContext().getScheduler().scheduleAtFixedRate(scanner, 0, period, TimeUnit.MILLISECONDS);
			}
		}
	}
}
