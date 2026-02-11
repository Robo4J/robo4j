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
package com.robo4j.units.rpi.gps;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.gps.GPSEvent;
import com.robo4j.units.rpi.gps.GPSRequest.Operation;
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
public class GPSExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(GPSExample.class);
    private static final String ID_PROCESSOR = "processor";
    private static final String ID_GPS = "gps";

    public static void main(String[] args) throws RoboBuilderException {
        var builder = new RoboBuilder();
        builder.add(MtkGPSUnit.class, ID_GPS);
        builder.add(GPSProcessor.class, ID_PROCESSOR);
        var ctx = builder.build();

        LOGGER.info("State before start:");
        LOGGER.info(SystemUtil.printStateReport(ctx));
        ctx.start();

        LOGGER.info("State after start:");
        LOGGER.info(SystemUtil.printStateReport(ctx));

        RoboReference<GPSRequest> gps = ctx.getReference(ID_GPS);
        RoboReference<GPSEvent> processor = ctx.getReference(ID_PROCESSOR);

        LOGGER.info("Press <Enter> to start requesting events, then press enter again to stop requesting events!");
        readln();

        LOGGER.info("Requesting GPS events! Press <Enter> to stop!");
        gps.sendMessage(new GPSRequest(processor, Operation.REGISTER));
        readln();

        LOGGER.info("Ending requesting GPS events...");
        gps.sendMessage(new GPSRequest(processor, Operation.UNREGISTER));
        // Note that we can still get a few more events after this, and that is
        // quite fine. ;)
        LOGGER.info("All done! Press <Enter> to quit!");
        readln();

        LOGGER.info("Exiting! Bye!");
        ctx.shutdown();

        // Seems Pi4J keeps an executor with non-daemon threads around after
        // we've used the serial port, even after closing it. :/
        System.exit(0);
    }
}
