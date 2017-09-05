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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.units.lego.example;

import org.junit.Test;

import com.robo4j.core.RoboBuilder;
import com.robo4j.core.RoboContext;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.configuration.ConfigurationFactory;
import com.robo4j.socket.http.units.HttpServerUnit;
import com.robo4j.socket.http.util.RoboHttpUtils;
import com.robo4j.units.lego.LcdTestUnit;
import com.robo4j.units.lego.SimpleTankTestUnit;

/**
 * Simple Tanks examples tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TankExampleTests {

	private static final String ID_LCD = "lcd";
	private static final String ID_PLATFORM = "platform";
	private static final String ID_HTTP = "http";
	private static final String ID_UNIT_CONTROLLER = "controller";
	private static final int PORT = 8025;

	@Test
	public void legoTankExampleTest() throws Exception {
		RoboBuilder builder = new RoboBuilder();

		Configuration config = ConfigurationFactory.createEmptyConfiguration();

		config.setString("target", ID_UNIT_CONTROLLER);
		config.setInteger("port", PORT);
		config.setString("packages", "com.robo4j.units.lego.example.codec");
		/* specific configuration */
		Configuration targetUnits = config.createChildConfiguration(RoboHttpUtils.HTTP_TARGET_UNITS);
		targetUnits.setString(ID_UNIT_CONTROLLER, "GET");
		builder.add(HttpServerUnit.class, config, ID_HTTP);

		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("target", ID_PLATFORM);
		builder.add(TankExampleController.class, config, ID_UNIT_CONTROLLER);

		/* platform is listening to the bus */
		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString("leftMotorPort", "B");
		config.setCharacter("leftMotorType", 'N');
		config.setString("rightMotorPort", "C");
		config.setCharacter("rightMotorType", 'N');
		builder.add(SimpleTankTestUnit.class, config, ID_PLATFORM);

		/* lcd is listening to the bus */
		builder.add(LcdTestUnit.class, ID_LCD);

		RoboContext context = builder.build();

		context.start();

		context.getReference(ID_LCD).sendMessage("Press Key to end...");
		context.shutdown();
	}
}
