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
package com.robo4j.hw.rpi.imu;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.hw.rpi.imu.BNO055SystemStatus;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BNOSystemStatusTests {

	@Test
	public void testFlags() {
		BNO055SystemStatus status = new BNO055SystemStatus(1);
		Assert.assertTrue(status.getStatusFlags().length == 1);
		Assert.assertArrayEquals(createFlags(BNO055SystemStatus.StatusFlag.IDLE), status.getStatusFlags());
		status = new BNO055SystemStatus(65);
		Assert.assertTrue(status.getStatusFlags().length == 2);
		Assert.assertArrayEquals(createFlags(BNO055SystemStatus.StatusFlag.IDLE, BNO055SystemStatus.StatusFlag.RUNNING_NO_SENSOR_FUSION), status.getStatusFlags());

	}

	private BNO055SystemStatus.StatusFlag[] createFlags(BNO055SystemStatus.StatusFlag... flags) {
		return flags;
	}
}
