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

package com.robo4j.units.rpi.camera;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;

/**
 * Unit that can schedule unit to run the unit periodically
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraPeriodUnit extends RoboUnit<Boolean> {

	private static final String FIELD_UNIT = "unit";
	private static final String FIELD_DELAY = "delay";
	private static final String FIELD_PERIOD = "period";
	private static final String FIELD_TIME_UNIT = "timeUnit";

	private Map<String, Object> properties;

	public CameraPeriodUnit(RoboContext context, String id) {
		super(Boolean.class, context, id);
		properties = new HashMap<>();
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		properties.put(FIELD_UNIT, configuration.getString(FIELD_UNIT, null));
		properties.put(FIELD_DELAY, configuration.getInteger(FIELD_DELAY, null));
		properties.put(FIELD_PERIOD, configuration.getInteger(FIELD_PERIOD, null));

		String timeUnit = configuration.getString(FIELD_TIME_UNIT, null);
		if (timeUnit == null) {
			throw ConfigurationException.createMissingConfigNameException("proper timeUnit is missing: " + timeUnit);
		}
		properties.put(FIELD_TIME_UNIT, TimeUnit.valueOf(timeUnit));

		if (properties.entrySet().stream().map(Map.Entry::getValue).filter(Objects::isNull).count() > 0) {
			throw ConfigurationException.createMissingConfigNameException("wrong configuration: " + properties);
		}

	}

	@Override
	public void start() {
		setState(LifecycleState.STARTING);
		runUnit();
		setState(LifecycleState.STARTED);
	}

	@Override
	public void shutdown() {
		setState(LifecycleState.SHUTTING_DOWN);
		setState(LifecycleState.SHUTDOWN);
	}

	// Private Methods

	private void runUnit() {
		RoboReference<Boolean> unitReference = getContext().getReference(properties.get(FIELD_UNIT).toString());
		getContext().getScheduler().schedule(unitReference, true, (int) properties.get(FIELD_DELAY),
				(int) properties.get(FIELD_PERIOD), (TimeUnit) properties.get(FIELD_TIME_UNIT));
	}

}
