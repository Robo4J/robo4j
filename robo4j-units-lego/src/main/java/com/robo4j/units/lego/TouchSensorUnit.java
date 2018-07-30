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
import com.robo4j.units.lego.touch.TouchSensorMessage;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TouchSensorUnit sends the touch event to the target
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class TouchSensorUnit extends RoboUnit<String> {

	public static final String PROPERTY_TARGET = "target";
	public static final String PROPERTY_SENSOR_PORT = "sensorPort";
	private AtomicBoolean sensorActive = new AtomicBoolean();
	private ILegoSensor sensor;
	private String target;

	public TouchSensorUnit(RoboContext context, String id) {
		super(String.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString(PROPERTY_TARGET, null);
		if (target == null) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_TARGET);
		}

		String sensorPort = configuration.getString(PROPERTY_SENSOR_PORT, null);
		if(sensorPort == null){
            throw ConfigurationException.createMissingConfigNameException(PROPERTY_SENSOR_PORT);
        }
		SensorProvider provider = new SensorProvider();
		sensor = new SensorWrapper<>(provider, DigitalPortEnum.getByType(sensorPort), SensorTypeEnum.TOUCH);
		sensorActive.set(true);
	}


    @Override
    public void onMessage(String message) {
        runTouchSensor();
    }

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		sensorActive.set(false);
		sensor.close();
		setState(LifecycleState.SHUTDOWN);
	}

    // private methods
    private void runTouchSensor() {
        getContext().getScheduler().scheduleAtFixedRate(() -> {
            if (sensorActive.get()) {
                TouchSensorMessage message = TouchSensorMessage.parseValue(sensor.getData());
                switch (message){
                    case PRESSED:
                        getContext().getReference(target).sendMessage("PRESSED");
                        break;
                    case RELEASED:
                        getContext().getReference(target).sendMessage("Robo4j.io");
                        break;
                    default:
                        break;
                }
            }
        }, 1000, 500, TimeUnit.MILLISECONDS);
    }

}
