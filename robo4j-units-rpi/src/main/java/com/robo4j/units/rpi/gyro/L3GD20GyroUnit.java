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
package com.robo4j.units.rpi.gyro;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.AttributeDescriptor;
import com.robo4j.core.ConfigurationException;
import com.robo4j.core.DefaultAttributeDescriptor;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.WorkTrait;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.hw.rpi.i2c.gyro.CalibratedGyro;
import com.robo4j.hw.rpi.i2c.gyro.GyroL3GD20Device;
import com.robo4j.hw.rpi.i2c.gyro.GyroL3GD20Device.Sensitivity;
import com.robo4j.math.geometry.Float3D;
import com.robo4j.units.rpi.lcd.I2CRoboUnit;

/**
 * Gyro unit based on the {@link GyroL3GD20Device}. Note that there can only be
 * ONE active notification threshold per target. If a new one is registered
 * before it has triggered, the new one will replace the old one.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@WorkTrait
public class L3GD20GyroUnit extends I2CRoboUnit<GyroRequest> {
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
	 * This attribute will provide the state of the gyro as a {@link Float3D}.
	 */
	public static final String ATTRIBUTE_NAME_STATE = "state";

	public static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections
			.unmodifiableCollection(Arrays.asList(DefaultAttributeDescriptor.create(Float3D.class, ATTRIBUTE_NAME_STATE)));

	private final Map<RoboReference<GyroEvent>, GyroNotificationEntry> activeThresholds = new HashMap<>();

	private final GyroScanner scanner = new GyroScanner();
	
	private Sensitivity sensitivity;
	private boolean highPassFilter;
	private CalibratedGyro gyro;
	private volatile ScheduledFuture<?> readings;
	
	private class GyroScanner implements Runnable {
		private long lastReadingTime = System.currentTimeMillis();
		private Float3D lastReading;
		
		@Override
		public void run() {
			System.out.println("Probe run");
			Float3D data = read();
			long newTime = System.currentTimeMillis();

			// Trapezoid
			Float3D tmp = new Float3D(data);
			long deltaTime = newTime - lastReadingTime;
			data.add(lastReading);
			data.multiplyScalar(deltaTime / 2000.0f);
			lastReading.set(tmp);
			addToDeltas(data);
			lastReadingTime = newTime;
		}

		private void addToDeltas(Float3D data) {
			synchronized (L3GD20GyroUnit.this) {
				for (GyroNotificationEntry notificationEntry : activeThresholds.values()) {
					notificationEntry.addDelta(data);
				}				
			}
		}
		
		private void reset() {
			lastReadingTime = System.currentTimeMillis();
			lastReading = read();
		}

		private Float3D read() {
			try {
				return gyro.read();
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Could not read gyro, aborting.", e);
				return null;
			}
		}		
	}
	
	public L3GD20GyroUnit(RoboContext context, String id) {
		super(GyroRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		sensitivity = Sensitivity.valueOf(configuration.getString(PROPERTY_KEY_SENSITIVITY, "DPS_245"));
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
		if (message.calibrate() == true) {
			try {
				gyro.calibrate();
				scanner.reset();
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Failed to calibrate!", e);
			}
		}
		if (message.getNotificationThreshold() != null) {
			setUpNotification(message.getTarget(), message);
		}
		super.onMessage(message);
	}

	private void setUpNotification(RoboReference<GyroEvent> target, GyroRequest request) {
		synchronized (this) {
			if (request.isContinuous()) {
				activeThresholds.put(target, new ContinuousGyroNotificationEntry(target, request.getNotificationThreshold()));				
			} else {
				activeThresholds.put(target, new FixedGyroNotificationEntry(target, request.getNotificationThreshold()));								
			}
		}
		if (readings == null) {
			synchronized (this) {
				readings = getContext().getScheduler().scheduleAtFixedRate(scanner, 0, 10, TimeUnit.MILLISECONDS);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeType() == Float3D.class && descriptor.getAttributeName().equals(ATTRIBUTE_NAME_STATE)) {
			try {
				return (R) gyro.read();
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Failed to read the gyro!", e);
			}
		}
		return super.onGetAttribute(descriptor);
	}
}
