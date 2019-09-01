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

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.hw.rpi.imu.bno.DataEvent3f;
import com.robo4j.net.LookupService;
import com.robo4j.net.LookupServiceProvider;
import com.robo4j.units.rpi.imu.BnoRequest;
import com.robo4j.util.SystemUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * VectorEventEmitterListenerExample is simple robo4j system displaying Rotation
 * Vector event from {@link com.robo4j.hw.rpi.imu.bno.impl.Bno080SPIDevice}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class VectorEventListenerExample {
	public static void main(String[] args) throws Exception {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		final InputStream systemIS;
		final InputStream contextIS;

		switch (args.length) {
		case 0:
			systemIS = classLoader.getResourceAsStream("bno080VectorSystemEmitterExample.xml");
			contextIS = classLoader.getResourceAsStream("bno080VectorExample.xml");
			System.out.println("Default configuration used");
			break;
		case 1:
			systemIS = classLoader.getResourceAsStream("bno080VectorSystemEmitterExample.xml");
			Path contextPath = Paths.get(args[0]);
			contextIS = Files.newInputStream(contextPath);
			System.out.println("Robo4j config file has been used: " + args[0]);
			break;
		case 2:
			Path systemPath2 = Paths.get(args[0]);
			Path contextPath2 = Paths.get(args[1]);
			systemIS = Files.newInputStream(systemPath2);
			contextIS = Files.newInputStream(contextPath2);
			System.out.println(String.format("Custom configuration used system: %s, context: %s", args[0], args[1]));
			break;
		default:
			System.out.println("Could not find the *.xml settings for the CameraClient!");
			System.out.println("java -jar camera.jar system.xml context.xml");
			System.exit(2);
			throw new IllegalStateException("see configuration");
		}

		if (systemIS == null && contextIS == null) {
			System.out.println("Could not find the settings for the BNO080 Example!");
			System.exit(2);
		}
		RoboBuilder builder = new RoboBuilder(systemIS);
		builder.add(contextIS);
		RoboContext ctx = builder.build();

		ctx.start();

		LookupService service = LookupServiceProvider.getDefaultLookupService();
		try {
			service.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("State after start:");
		System.out.println(SystemUtil.printStateReport(ctx));

		RoboReference<BnoRequest> bnoUnit = ctx.getReference("bno");
		RoboReference<DataEvent3f> bnoListenerUnit = ctx.getReference("listener");

		BnoRequest requestToRegister = new BnoRequest(bnoListenerUnit, BnoRequest.ListenerAction.REGISTER);
		bnoUnit.sendMessage(requestToRegister);

		System.out.println("Press <Enter> to start!");
		System.in.read();
		ctx.shutdown();

	}
}
