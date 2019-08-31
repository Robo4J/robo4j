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

package com.robo4j.units.rpi.bno;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.imu.Bno080Device;
import com.robo4j.hw.rpi.imu.bno.DataEvent3f;
import com.robo4j.hw.rpi.imu.bno.DeviceListener;
import com.robo4j.hw.rpi.imu.bno.shtp.SensorReportIds;
import com.robo4j.hw.rpi.imu.impl.Bno080SPIDevice;
import com.robo4j.logging.SimpleLoggingUtil;

/**
 * BNO080EmitterUnit emits desired information produced by
 * {@link com.robo4j.hw.rpi.imu.impl.Bno080SPIDevice } to the target provided by
 * {@link BnoRequest}
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Bno080EmitterUnit extends RoboUnit<BnoRequest> {

	public static final String PROPERTY_REPORT_TYPE = "reportType";
	public static final String PROPERTY_REPORT_DELAY = "reportDelay";

	private static final class BNOListenerEvent implements DeviceListener {
		private final RoboReference<DataEvent3f> target;

		BNOListenerEvent(RoboReference<DataEvent3f> target) {
			this.target = target;
		}

		@Override
		public void onResponse(DataEvent3f event) {
			target.sendMessage(event);
		}
	}

	private Bno080Device device;
	private List<BNOListenerEvent> listeners = new ArrayList<>();

	public Bno080EmitterUnit(RoboContext context, String id) {
		super(BnoRequest.class, context, id);
	}

	private int reportDelay;
	private SensorReportIds report;

	@Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {

        final String reportType = configuration.getString(PROPERTY_REPORT_TYPE, null);
        report = SensorReportIds.valueOf(reportType.toUpperCase());
        if(report.equals(SensorReportIds.NONE)){
            throw new ConfigurationException(PROPERTY_REPORT_TYPE);
        }

        final Integer delay = configuration.getInteger(PROPERTY_REPORT_DELAY, null);
        if(delay == null || delay <= 0){
            throw new ConfigurationException(PROPERTY_REPORT_DELAY);
        }
        this.reportDelay = delay;

        try {
            //TODO: make device configurable
            device = new Bno080SPIDevice();
        } catch (IOException e) {
            throw new ConfigurationException("not possible initiate", e);
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

    private synchronized void register(RoboReference<DataEvent3f> target){
		BNOListenerEvent event = new BNOListenerEvent(target);
		listeners.add(event);
		device.addListener(event);
	}

    private synchronized void unregister(RoboReference<DataEvent3f> target){
	    for(BNOListenerEvent l: new ArrayList<>(listeners)){
	        if(target.equals(l.target)){
	            listeners.remove(l);
	            device.removeListener(l);
            }
        }
    }
}
