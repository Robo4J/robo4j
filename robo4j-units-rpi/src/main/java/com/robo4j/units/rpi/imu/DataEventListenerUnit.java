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

package com.robo4j.units.rpi.imu;

import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.hw.rpi.imu.bno.DataEvent3f;
import com.robo4j.logging.SimpleLoggingUtil;

/**
 * DataEventListener unit listens to {@link DataEvent3f} event types produced by
 * the {@link Bno080Unit}.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DataEventListenerUnit extends RoboUnit<DataEvent3f> {

	public DataEventListenerUnit(RoboContext context, String id) {
		super(DataEvent3f.class, context, id);
	}

	@Override
	public void onMessage(DataEvent3f message) {
		SimpleLoggingUtil.info(getClass(), "received:" + message);
	}
}
