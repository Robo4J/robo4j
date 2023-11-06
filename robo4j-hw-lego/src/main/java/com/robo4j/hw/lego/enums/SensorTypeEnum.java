/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.lego.enums;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Available Lego Sensors
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum SensorTypeEnum {

	// @formatter:off
	// type id mode source elements
	/**
	 * Touch pressed 1.0 : free 0.0 (string of float)
	 */
	TOUCH		(0, "Touch", "lejos.hardware.sensor.EV3TouchSensor", 1),
	/**
	 * Returns an SampleProvider object representing the gyro sensor in angle
	 * mode. <br>
	 * In rate mode the sensor measures the orientation of the sensor in respect
	 * to its start position. A positive angle indicates a orientation to the
	 * left. A negative rate indicates a rotation to the right. Angles are
	 * expressed in degrees.<br>
	 */
	GYRO		(1, "Angle", "lejos.hardware.sensor.EV3GyroSensor", 1),

	/**
	 * size of the array is 3
	 */
	COLOR		(2, "RGB", "lejos.hardware.sensor.EV3ColorSensor", 3),

	/**
	 * distance is measured in meter
	 */
	SONIC		(3, "Distance", "lejos.hardware.sensor.EV3UltrasonicSensor", 1),

	/**
	 * distance is measure in meter
	 */
	INFRA		(4, "Infra","lejos.hardware.sensor.EV3IRSensor", 0),

	/**
	 *  Unknown sensor
	 */
	UNKNOWN (-1, "", "", -1);
	// @formatter:on

	private static final Map<Integer, SensorTypeEnum> internMapById = initMapping();
	private final int id;
	private final String mode;
	private final String source;
	private final int elements;

	SensorTypeEnum(final int id, final String mode, final String source, final int elements) {
		this.id = id;
		this.mode = mode;
		this.source = source;
		this.elements = elements;
	}

	public static SensorTypeEnum getById(int id) {
		return internMapById.get(id);
	}

	//@formatter:off
	public static SensorTypeEnum getBySource(String source) {
		return internMapById.values().stream()
				.filter(sensorTypeEnum->sensorTypeEnum.getSource().equals(source))
				.findFirst().orElse(UNKNOWN);
	}
	//@formatter:on

	public int getId() {
		return id;
	}

	public String getMode() {
		return mode;
	}

	public String getSource() {
		return source;
	}

	public int getElements() {
		return elements;
	}

	// Private Methods
	private static Map<Integer, SensorTypeEnum> initMapping() {
		return Stream.of(values()).collect(Collectors.toMap(SensorTypeEnum::getId, e -> e));
	}

	@Override
	public String toString() {
		return "SensorEnum{" + "id=" + id + ", mode='" + mode + '\'' + ", source='" + source + '\'' + ", elements="
				+ elements + '}';
	}
}
