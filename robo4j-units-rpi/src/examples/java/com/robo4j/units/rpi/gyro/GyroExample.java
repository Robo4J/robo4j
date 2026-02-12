/*
 * Copyright (c) 2014, 2026, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboReference;
import com.robo4j.math.geometry.Tuple3f;
import com.robo4j.units.rpi.gyro.GyroRequest.GyroAction;
import com.robo4j.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.IO.*;

/**
 * Runs the gyro continuously.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GyroExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(GyroExample.class);
    private static final String ID_PROCESSOR = "processor";

    public static void main(String[] args) throws RoboBuilderException {
        var builder = new RoboBuilder();
        var settings = GyroExample.class.getClassLoader().getResourceAsStream("gyroexample.xml");
        if (settings == null) {
            LOGGER.warn("Could not find the settings for the GyroExample!");
            System.exit(2);
        }
        builder.add(settings);
        builder.add(GyroProcessor.class, ID_PROCESSOR);
        var ctx = builder.build();

        LOGGER.info("State before start:");
        LOGGER.info(SystemUtil.printStateReport(ctx));
        ctx.start();

        LOGGER.info("State after start:");
        LOGGER.info(SystemUtil.printStateReport(ctx));

        RoboReference<GyroRequest> gyro = ctx.getReference("gyro");
        RoboReference<GyroEvent> processor = ctx.getReference(ID_PROCESSOR);

        LOGGER.info("Let the gyro unit be absolutely still, then press enter to calibrate and start!");
        readln();
        gyro.sendMessage(new GyroRequest(processor, GyroAction.CONTINUOUS, new Tuple3f(1.0f, 1.0f, 1.0f)));
        LOGGER.info("Will report angular changes indefinitely.\nPress <Enter> to quit!");
        readln();
    }
}
