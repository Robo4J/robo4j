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

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.math.geometry.ScanResult2D;
import com.robo4j.units.rpi.lidarlite.ScanRequest.ScanAction;

/**
 * Example controller for testing the laser scanner.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LaserScannerTestController extends RoboUnit<String> {
	public static String CONFIG_KEY_START_ANGLE = "startAngle";
	public static String CONFIG_KEY_RANGE = "range";
	public static String CONFIG_KEY_STEP = "step";

	private float startAngle = -45.0f;
	private float range = 90.0f;
	private float step = 1.0f;

	public LaserScannerTestController(RoboContext context, String id) {
		super(String.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		startAngle = configuration.getFloat(CONFIG_KEY_START_ANGLE, startAngle);
		range = configuration.getFloat(CONFIG_KEY_RANGE, range);
		step = configuration.getFloat(CONFIG_KEY_STEP, step);
	}

	@Override
	public void onMessage(String message) {
		switch (message) {
		case "scan":
			scan();
			break;
		}
	}

	private void scan() {
		RoboReference<ScanRequest> scanner = getContext().getReference("scanner");
		RoboReference<ScanResult2D> processor = getContext().getReference("processor");
		scanner.sendMessage(new ScanRequest(processor, ScanAction.ONCE, startAngle, range, step));
	}

}
