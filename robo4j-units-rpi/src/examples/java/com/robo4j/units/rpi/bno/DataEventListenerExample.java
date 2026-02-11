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

package com.robo4j.units.rpi.bno;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.imu.bno.DataEvent3f;
import com.robo4j.units.rpi.imu.BnoRequest;
import com.robo4j.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.IO.*;

/**
 * DataEventEmitterListenerExample is an example displaying received data from
 * GYROSCOPE. Data are provided by
 * {@link com.robo4j.hw.rpi.imu.bno.impl.Bno080SPIDevice}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DataEventListenerExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataEventListenerExample.class);

    public static void main(String[] args) throws Exception {
        var classLoader = Thread.currentThread().getContextClassLoader();

        final InputStream systemIS;
        final InputStream contextIS;

        switch (args.length) {
            case 0:
                systemIS = classLoader.getResourceAsStream("bno080DataSystemEmitterExample.xml");
                contextIS = classLoader.getResourceAsStream("bno080GyroExample.xml");
                LOGGER.info("Default configuration used");
                break;
            case 1:
                systemIS = classLoader.getResourceAsStream("bno080DataSystemEmitterExample.xml");
                Path contextPath = Paths.get(args[0]);
                contextIS = Files.newInputStream(contextPath);
                LOGGER.info("Robo4j config file has been used: {}", args[0]);
                break;
            case 2:
                Path systemPath2 = Paths.get(args[0]);
                Path contextPath2 = Paths.get(args[1]);
                systemIS = Files.newInputStream(systemPath2);
                contextIS = Files.newInputStream(contextPath2);
                LOGGER.info("Custom configuration used system: {}, context: {}", args[0], args[1]);
                break;
            default:
                LOGGER.warn("Could not find the *.xml settings for the CameraClient!");
                LOGGER.warn("java -jar camera.jar system.xml context.xml");
                System.exit(2);
                throw new IllegalStateException("see configuration");
        }

        var builder = new RoboBuilder(systemIS);
        builder.add(contextIS);
        var ctx = builder.build();

        ctx.start();

        LOGGER.info("State after start:");
        LOGGER.info(SystemUtil.printStateReport(ctx));

        var bnoUnit = ctx.getReference("bno");
        RoboReference<DataEvent3f> bnoListenerUnit = ctx.getReference("listener");

        BnoRequest requestToRegister = new BnoRequest(bnoListenerUnit, BnoRequest.ListenerAction.REGISTER);
        bnoUnit.sendMessage(requestToRegister);

        LOGGER.info("Press <Enter> to start!");
        readln();
        ctx.shutdown();

    }
}
