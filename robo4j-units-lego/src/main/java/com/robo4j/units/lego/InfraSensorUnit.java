/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.units.lego;

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.lego.ILegoSensor;
import com.robo4j.hw.lego.enums.DigitalPortEnum;
import com.robo4j.hw.lego.enums.SensorTypeEnum;
import com.robo4j.hw.lego.provider.SensorProvider;
import com.robo4j.hw.lego.wrapper.SensorWrapper;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.units.lego.infra.InfraSensorEnum;
import com.robo4j.units.lego.infra.InfraSensorMessage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * InfraSensorUnit {@link InfraSensorEnum}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class InfraSensorUnit extends RoboUnit<String> {

    public static final String PROPERTY_SENSOR_PORT = "sensorPort";
    public static final String PROPERTY_TARGET = "target";
    public static final String PROPERTY_SCAN_INIT_DELAY = "scanInitDelay";
    public static final String PROPERTY_SCAN_PERIOD = "scanPeriod";
    public static final int VALUE_SCAN_INIT_DELAY = 1000;
    public static final int VALUE_SCAN_PERIOD = 800;
    private volatile AtomicBoolean active = new AtomicBoolean();
    private ILegoSensor sensor;
    private String target;
    private int scanInitialDelay;
    private int scanPeriod;

    public InfraSensorUnit(RoboContext context, String id) {
        super(String.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        setState(LifecycleState.UNINITIALIZED);
        scanInitialDelay = configuration.getInteger(PROPERTY_SCAN_INIT_DELAY, VALUE_SCAN_INIT_DELAY);
        scanPeriod = configuration.getInteger(PROPERTY_SCAN_PERIOD, VALUE_SCAN_PERIOD);
        String port = configuration.getString(PROPERTY_SENSOR_PORT, null);
        DigitalPortEnum sensorPort = DigitalPortEnum.getByType(port);
        target = configuration.getString(PROPERTY_TARGET, null);
        if (sensorPort == null) {
            throw new ConfigurationException("infraRed sensor port required: {S1,S2,S3,S4}");
        }
        if (target == null) {
            throw new ConfigurationException("infraRed sensor target required");
        }
        SensorProvider provider = new SensorProvider();
        sensor = new SensorWrapper<>(provider, sensorPort, SensorTypeEnum.INFRA);
        setState(LifecycleState.INITIALIZED);
    }

    @Override
    public void onMessage(String message) {
        processMessage(message);

    }

    private void processMessage(String message){
        final InfraSensorEnum type = InfraSensorEnum.parseValue(message);
        switch (type) {
            case START:
                active.set(true);
                scheduleMeasurement();
                break;
            case STOP:
                stopMeasurement();
                break;
            default:
                SimpleLoggingUtil.error(getClass(), String.format("not supported value: %s", message));
        }
    }

    private void scheduleMeasurement() {
        if (active.get()) {
            getContext().getScheduler().scheduleAtFixedRate(this::startMeasurement, scanInitialDelay, scanPeriod,
                    TimeUnit.MILLISECONDS);
        }
    }

    private void startMeasurement() {
        sensor.activate(true);
        String data = sensor.getData();
        sendTargetMessage(data);
        sensor.activate(false);
    }

    private void stopMeasurement() {
        active.set(false);
        sensor.activate(false);
    }

    private void sendTargetMessage(String distance) {
        InfraSensorMessage message = new InfraSensorMessage(distance);
        getContext().getReference(target).sendMessage(message);
    }
}
