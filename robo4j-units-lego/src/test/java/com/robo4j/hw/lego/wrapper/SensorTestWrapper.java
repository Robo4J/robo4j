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
package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.ILegoSensor;
import com.robo4j.hw.lego.enums.DigitalPortEnum;
import com.robo4j.hw.lego.enums.SensorTypeEnum;

/**
 * Simple LegoMindstorm Mock Sensor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SensorTestWrapper implements ILegoSensor {

	private final DigitalPortEnum port;
	private final SensorTypeEnum type;

	public SensorTestWrapper(DigitalPortEnum port, SensorTypeEnum type) {
		this.port = port;
		this.type = type;
	}

	@Override
	public DigitalPortEnum getPort() {
		return port;
	}

	@Override
	public SensorTypeEnum getType() {
		return type;
	}

	@Override
	public String getData() {
		System.out.println(String.format("SensorTest.getData port:%s, type: %s", port, type));
		return "data";
	}

	@Override
	public void activate(boolean status) {
		System.out.println(String.format("SensorTest.activate %s,  port:%s, type: %s", status, port, type));
	}

	@Override
	public void close() {
		System.out.println(String.format("SensorTest.close port:%s, type: %s", port, type));
	}

	@Override
	public String toString() {
		return "SensorTestWrapper{" + "port=" + port + ", type=" + type + '}';
	}

}
