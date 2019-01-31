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
package com.robo4j.units.lego.example;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.units.HttpServerUnit;
import com.robo4j.socket.http.util.HttpPathConfigJsonBuilder;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.units.lego.LcdTestUnit;
import com.robo4j.units.lego.SimpleTankTestUnit;

/**
 * Simple Tanks examples tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */

class TankExampleTests {

	private static final String ID_LCD = "lcd";
	private static final String ID_PLATFORM = "platform";
	private static final String ID_HTTP = "http";
	private static final String ID_UNIT_CONTROLLER = "controller";
	private static final int PORT = 8021;

	@Test
	@Disabled("rethink and remove dependency")
	void legoTankExampleTest() throws Exception {
		RoboBuilder builder = new RoboBuilder();

		Configuration config = new ConfigurationBuilder().addString("target", ID_UNIT_CONTROLLER).addInteger("port", PORT)
				.addString("packages", "com.robo4j.units.lego.example.codec").addString(RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG,
						HttpPathConfigJsonBuilder.Builder().addPath(ID_UNIT_CONTROLLER, HttpMethod.GET).build())
				.build();
		builder.add(HttpServerUnit.class, config, ID_HTTP);

		builder.add(TankExampleController.class, new ConfigurationBuilder().addString("target", ID_PLATFORM).build(), ID_UNIT_CONTROLLER);

		/* platform is listening to the bus */
		config = new ConfigurationBuilder().addString("leftMotorPort", "B").addCharacter("leftMotorType", 'N')
				.addString("rightMotorPort", "C").addCharacter("rightMotorType", 'N').build();
		builder.add(SimpleTankTestUnit.class, config, ID_PLATFORM);

		/* lcd is listening to the bus */
		builder.add(LcdTestUnit.class, ID_LCD);

		RoboContext context = builder.build();

		context.start();

		context.getReference(ID_LCD).sendMessage("Press Key to end...");
		context.shutdown();
	}
}
