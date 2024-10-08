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
package com.robo4j.units.rpi.gyro;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example recipient for gyro events.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GyroProcessor extends RoboUnit<GyroEvent> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GyroProcessor.class);

    public GyroProcessor(RoboContext context, String id) {
        super(GyroEvent.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {

    }

    @Override
    public void onMessage(GyroEvent result) {
        LOGGER.info("GyroEvent: {}", result.toString());
    }
}
