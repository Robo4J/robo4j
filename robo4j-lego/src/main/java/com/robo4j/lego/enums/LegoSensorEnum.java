/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This LegoSensorEnum.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.lego.enums;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 15.02.2016
 */
public enum LegoSensorEnum {

	// @formatter:off
	// type id mode source elements
	/**
	 * Touch pressed 1 : free 0
	 */
	TOUCH(0, "Touch", "lejos.hardware.sensor.EV3TouchSensor", 1),
	/**
	 * Returns an SampleProvider object representing the gyro sensor in angle
	 * mode. <br>
	 * In rate mode the sensor measures the orientation of the sensor in respect
	 * to its start position. A positive angle indicates a orientation to the
	 * left. A negative rate indicates a rotation to the right. Angles are
	 * expressed in degrees.<br>
	 */
	GYRO(1, "Angle", "lejos.hardware.sensor.EV3GyroSensor", 1),

	/**
	 * size of the array is 3
	 */
	COLOR(2, "RGB", "lejos.hardware.sensor.EV3ColorSensor", 3),

	/**
	 * distance is measured in meter
	 */
	SONIC(3, "Distance", "lejos.hardware.sensor.EV3UltrasonicSensor", 1);
	// @formatter:on

	private volatile static Map<Integer, LegoSensorEnum> codeToSensorTypMapping;
	private volatile static Map<String, LegoSensorEnum> codeToSensorSourceMapping;
	private int id;
	private String mode;
	private String source;
	private int elements;

	LegoSensorEnum(final int id, final String mode, final String source, final int elements) {
		this.id = id;
		this.mode = mode;
		this.source = source;
		this.elements = elements;
	}

	public static LegoSensorEnum getById(int id) {
		if (codeToSensorTypMapping == null) {
			initMapping();
		}
		return codeToSensorTypMapping.get(id);
	}

	public static LegoSensorEnum getBySource(int name) {
		if (codeToSensorSourceMapping == null) {
			initSourceMapping();
		}
		return codeToSensorSourceMapping.get(name);
	}

	// Private Methods
	private static void initMapping() {
		codeToSensorTypMapping = new HashMap<>();
		for (LegoSensorEnum cmd : values()) {
			codeToSensorTypMapping.put(cmd.getId(), cmd);
		}
	}

	private static void initSourceMapping() {
		codeToSensorSourceMapping = new HashMap<>();
		for (LegoSensorEnum cmd : values()) {
			codeToSensorSourceMapping.put(cmd.getSource(), cmd);
		}
	}

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

	@Override
	public String toString() {
		return "LegoSensorEnum=(" + "Source='" + source + '\'' + "Mode='" + mode + '\'' + ')';
	}

}
