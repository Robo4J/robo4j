/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 * 
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.units.rpi.gps;

import java.io.IOException;

import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboBuilderException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.util.SystemUtil;
import com.robo4j.hw.rpi.serial.gps.GPSEvent;
import com.robo4j.units.rpi.gps.GPSRequest.Operation;

/**
 * Runs the gyro continuously.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GPSExample {
	private static final String ID_PROCESSOR = "processor";
	private static final String ID_GPS = "gps";

	public static void main(String[] args) throws RoboBuilderException, IOException {
		RoboBuilder builder = new RoboBuilder();
		builder.add(GPSUnit.class, ID_GPS);
		builder.add(GPSProcessor.class, ID_PROCESSOR);
		RoboContext ctx = builder.build();

		System.out.println("State before start:");
		System.out.println(SystemUtil.printStateReport(ctx));
		ctx.start();

		System.out.println("State after start:");
		System.out.println(SystemUtil.printStateReport(ctx));

		RoboReference<GPSRequest> gps = ctx.getReference(ID_GPS);
		RoboReference<GPSEvent> processor = ctx.getReference(ID_PROCESSOR);

		System.out.println("Press enter to start requesting events, then press enter again to stop requesting events!");
		System.in.read();

		System.out.println("Requesting GPS events! Press enter to stop!");
		gps.sendMessage(new GPSRequest(processor, Operation.REGISTER));
		System.in.read();

		System.out.println("Ending requesting GPS events...");
		gps.sendMessage(new GPSRequest(processor, Operation.UNREGISTER));
		// Note that we can still get a few more events after this, and that is
		// quite fine. ;)
		System.out.println("All done! Press enter to quit!");
		System.in.read();

		System.out.println("Exiting! Bye!");
	}
}
