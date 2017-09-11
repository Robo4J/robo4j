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
package com.robo4j.units.rpi.lidarlite;

import java.util.concurrent.TimeUnit;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.math.geometry.ScanResult2D;

/**
 * Example controller for testing the laser scanner.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LaserScanProcessor extends RoboUnit<ScanResult2D> {
	public LaserScanProcessor(RoboContext context, String id) {
		super(ScanResult2D.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {

	}

	@Override
	public void onMessage(ScanResult2D result) {
		// System.out.println(result.toString());
		RoboReference<String> controller = getContext().getReference("controller");
		getContext().getScheduler().schedule(controller, "scan", 5, 100, TimeUnit.SECONDS, 1);
		controller.sendMessage("scan");
	}
}
