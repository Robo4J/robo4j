/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.ILegoSensor;
import com.robo4j.hw.lego.enums.DigitalPortEnum;
import com.robo4j.hw.lego.enums.SensorTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple LegoMindstorm Mock Sensor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public record SensorTestWrapper(DigitalPortEnum port, SensorTypeEnum type) implements ILegoSensor {
    private static final Logger LOGGER = LoggerFactory.getLogger(SensorTestWrapper.class);

    @Override
    public String getData() {
        LOGGER.info("SensorTest.getData port:{}, type: {}", port, type);
        return "data";
    }

    @Override
    public void activate(boolean status) {
        LOGGER.info("SensorTest.activate {},  port:{}, type: {}", status, port, type);
    }

    @Override
    public void close() {
        LOGGER.info("SensorTest.close port:{}, type: {}", port, type);
    }

    @Override
    public String toString() {
        return "SensorTestWrapper{" + "port=" + port + ", type=" + type + '}';
    }

}
