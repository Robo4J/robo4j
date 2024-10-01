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
package com.robo4j.hw.rpi.gps;

import java.util.ArrayList;
import java.util.List;

import com.robo4j.hw.rpi.gps.GPS;
import com.robo4j.hw.rpi.gps.GPSListener;

public class MockGPS implements GPS {
	List<GPSListener> listeners = new ArrayList<>();

	@Override
	public void addListener(GPSListener gpsListener) {
		listeners.add(gpsListener);
	}

	@Override
	public void removeListener(GPSListener gpsListener) {
		listeners.remove(gpsListener);
	}

	@Override
	public void start() {
	}

	@Override
	public void shutdown() {
	}

	@Override
	public String toString() {
		return "Mock GPS";
	}
}
