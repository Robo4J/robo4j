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

package com.robo4j.units.rpi.imu;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.imu.bno.Bno080Device;
import com.robo4j.hw.rpi.imu.bno.Bno080Factory;
import com.robo4j.hw.rpi.imu.bno.DataEvent3f;
import com.robo4j.hw.rpi.imu.bno.DataListener;
import com.robo4j.hw.rpi.imu.bno.impl.Bno080SPIDevice;
import com.robo4j.hw.rpi.imu.bno.shtp.SensorReportId;
import com.robo4j.logging.SimpleLoggingUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * IMU unit emitting data produced by {@link Bno080SPIDevice } to the target
 * provided by {@link BnoRequest}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Bno080Unit extends RoboUnit<BnoRequest> {

	public static final String PROPERTY_REPORT_TYPE = "reportType";
	public static final String PROPERTY_REPORT_DELAY = "reportDelay";

	private static final class BnoListenerEvent implements DataListener {
		private final RoboReference<DataEvent3f> target;

		BnoListenerEvent(RoboReference<DataEvent3f> target) {
			this.target = target;
		}

		@Override
		public void onResponse(DataEvent3f event) {
			target.sendMessage(event);
		}
	}

	private Bno080Device device;
	private List<BnoListenerEvent> listeners = new ArrayList<>();

	public Bno080Unit(RoboContext context, String id) {
		super(BnoRequest.class, context, id);
	}

	private int reportDelay;
	private SensorReportId report;

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {

		final String reportType = configuration.getString(PROPERTY_REPORT_TYPE, null);
		report = SensorReportId.valueOf(reportType.toUpperCase());
		if (report.equals(SensorReportId.NONE)) {
			throw new ConfigurationException(PROPERTY_REPORT_TYPE);
		}

		final Integer delay = configuration.getInteger(PROPERTY_REPORT_DELAY, null);
		if (delay == null || delay <= 0) {
			throw new ConfigurationException(PROPERTY_REPORT_DELAY);
		}
		this.reportDelay = delay;

		try {
			// TODO: make device configurable
			device = Bno080Factory.createDefaultSPIDevice();
		} catch (IOException | InterruptedException e) {
			throw new ConfigurationException("Could not initiate device", e);
		}
		device.start(report, reportDelay);
	}

	@Override
	public void onMessage(BnoRequest message) {
		RoboReference<DataEvent3f> target = message.getTarget();
		switch (message.getListenerAction()) {
		case REGISTER:
			register(target);
			break;
		case UNREGISTER:
			unregister(target);
			break;
		default:
			SimpleLoggingUtil.error(getClass(), String.format("Unknown operation: %s", message));
		}

	}

	@Override
	public void shutdown() {
		super.shutdown();
		device.shutdown();
	}

	private synchronized void register(RoboReference<DataEvent3f> target) {
		BnoListenerEvent event = new BnoListenerEvent(target);
		listeners.add(event);
		device.addListener(event);
	}

	private synchronized void unregister(RoboReference<DataEvent3f> target) {
		for (BnoListenerEvent l : new ArrayList<>(listeners)) {
			if (target.equals(l.target)) {
				listeners.remove(l);
				device.removeListener(l);
			}
		}
	}
}
