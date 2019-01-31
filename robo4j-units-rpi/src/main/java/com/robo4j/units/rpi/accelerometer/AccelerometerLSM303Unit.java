/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.rpi.accelerometer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.CalibratedFloat3DDevice;
import com.robo4j.hw.rpi.i2c.accelerometer.AccelerometerLSM303Device;
import com.robo4j.hw.rpi.i2c.accelerometer.AccelerometerLSM303Device.DataRate;
import com.robo4j.hw.rpi.i2c.accelerometer.AccelerometerLSM303Device.FullScale;
import com.robo4j.hw.rpi.i2c.accelerometer.AccelerometerLSM303Device.PowerMode;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.hw.rpi.i2c.accelerometer.CalibratedAccelerometer;
import com.robo4j.math.geometry.Tuple3f;
import com.robo4j.units.rpi.I2CRoboUnit;

/**
 * Accelerometer unit.
 * 
 * Use configuration settings to compensate for inverted axes, and for
 * calibration.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class AccelerometerLSM303Unit extends I2CRoboUnit<AccelerometerRequest> {
	/**
	 * This key controls the {@link PowerMode}. Use the enum name of the power
	 * mode you want to use. The default is NORMAL.
	 */
	public static final String PROPERTY_KEY_POWER_MODE = "powerMode";

	/**
	 * This key controls the {@link DataRate} to use. Use the enum name of the
	 * data rate you want to use. The default is HZ_10.
	 */
	public static final String PROPERTY_KEY_RATE = "rate";

	/**
	 * This key controls which axes to enable. See
	 * {@link AccelerometerLSM303Device}.
	 * 
	 * Default is 7. See {@link AccelerometerLSM303Device#AXIS_ENABLE_ALL}.
	 */
	private static final String PROPERTY_KEY_AXIS_ENABLE = "axisEnable";

	/**
	 * This key controls the full scale (and thereby the sensitivity) of the
	 * accelerometer. See {@link FullScale}. Default is G_2.
	 */
	private static final String PROPERTY_KEY_FULL_SCALE = "fullScale";

	/**
	 * This key controls the enabling the high res mode. Default is false.
	 */
	private static final String PROPERTY_KEY_ENABLE_HIGH_RES = "enableHighRes";

	/**
	 * This key controls how often to read the accelerometer, in ms. Default is
	 * 200.
	 */
	private static final String PROPERTY_KEY_PERIOD = "period";

	/**
	 * This attribute will provide the state of the accelerometer as a
	 * {@link Tuple3f}.
	 */
	public static final String ATTRIBUTE_NAME_STATE = "state";

	public static final Collection<AttributeDescriptor<?>> KNOWN_ATTRIBUTES = Collections
			.unmodifiableCollection(Arrays.asList(DefaultAttributeDescriptor.create(Tuple3f.class, ATTRIBUTE_NAME_STATE)));

	private final Scanner scanner = new Scanner();

	private CalibratedFloat3DDevice accelerometer;
	private Integer period;

	private volatile ScheduledFuture<?> scannerTask;
	private List<AccelerometerRequest> requests = new ArrayList<>();

	private class Scanner implements Runnable {
		@Override
		public void run() {
			try {
				Tuple3f value = accelerometer.read();
				for (AccelerometerRequest request : requests) {
					if (request.getPredicate().test(value)) {
						notify(request.getTarget(), value);
					}
				}
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Failed to read accelerometer!", e);
			}
		}

		private void notify(RoboReference<AccelerometerEvent> target, Tuple3f value) {
			target.sendMessage(new AccelerometerEvent(value));
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
	public AccelerometerLSM303Unit(RoboContext context, String id) {
		super(AccelerometerRequest.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		super.onInitialization(configuration);
		PowerMode powerMode = PowerMode.valueOf(configuration.getString(PROPERTY_KEY_POWER_MODE, PowerMode.NORMAL.name()));
		DataRate rate = DataRate.valueOf(configuration.getString(PROPERTY_KEY_RATE, DataRate.HZ_10.name()));
		Integer axisEnable = configuration.getInteger(PROPERTY_KEY_AXIS_ENABLE, AccelerometerLSM303Device.AXIS_ENABLE_ALL);
		FullScale fullScale = FullScale.valueOf(configuration.getString(PROPERTY_KEY_FULL_SCALE, FullScale.G_2.name()));
		Boolean enableHighRes = configuration.getBoolean(PROPERTY_KEY_ENABLE_HIGH_RES, false);
		Tuple3f offsets = readFloat3D(configuration.getChildConfiguration("offsets"));
		Tuple3f multipliers = readFloat3D(configuration.getChildConfiguration("multipliers"));
		period = configuration.getInteger(PROPERTY_KEY_PERIOD, 200);

		try {
			AccelerometerLSM303Device device = new AccelerometerLSM303Device(getBus(), getAddress(), powerMode, rate, axisEnable, fullScale,
					enableHighRes);
			accelerometer = new CalibratedAccelerometer(device, offsets, multipliers);
		} catch (IOException e) {
			throw new ConfigurationException(String.format(
					"Failed to initialize lidar device. Make sure it is hooked up to bus: %d address: %xd", getBus(), getAddress()), e);
		}
	}

	private Tuple3f readFloat3D(Configuration config) {
		return new Tuple3f(config.getFloat("x", 0f), config.getFloat("y", 0f), config.getFloat("z", 0f));
	}

	@Override
	public void onMessage(AccelerometerRequest message) {
		super.onMessage(message);
		registerRequest(message);
	}

	private void registerRequest(AccelerometerRequest message) {
		synchronized (this) {
			requests.add(message);
		}
		if (scannerTask == null) {
			scannerTask = getContext().getScheduler().scheduleAtFixedRate(scanner, 0, period, TimeUnit.MILLISECONDS);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
		if (descriptor.getAttributeType() == Tuple3f.class && descriptor.getAttributeName().equals(ATTRIBUTE_NAME_STATE)) {
			try {
				return (R) accelerometer.read();
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Failed to read the accelerometer!", e);
			}
		}
		return super.onGetAttribute(descriptor);
	}
}
