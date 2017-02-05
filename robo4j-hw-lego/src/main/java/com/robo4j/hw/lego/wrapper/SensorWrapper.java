/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This LegoSensorWrapper.java  is part of robo4j.
 * module: robo4j-hw-lego
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.lego.wrapper;

import com.robo4j.hw.lego.ILegoSensor;
import com.robo4j.hw.lego.enums.DigitalPortEnum;
import com.robo4j.hw.lego.enums.SensorTypeEnum;
import com.robo4j.hw.lego.provider.SensorProvider;

import lejos.hardware.sensor.BaseSensor;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.robotics.SampleProvider;

/**
 * Wrapper for any LegoMindstorm sensor
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class SensorWrapper<Sensor extends BaseSensor> implements ILegoSensor {

	protected Sensor sensor;
	protected DigitalPortEnum port;
	protected SensorTypeEnum sensorType;

	@SuppressWarnings("unchecked")
	public SensorWrapper(SensorProvider provider, DigitalPortEnum port, SensorTypeEnum sensorType) {
		this.sensor = (Sensor) provider.create(port, sensorType);
		this.port = port;
		this.sensorType = sensorType;
	}

	public SensorWrapper(Sensor sensor, DigitalPortEnum port, SensorTypeEnum sensorType) {
		this.sensor = sensor;
		this.port = port;
		this.sensorType = sensorType;
	}

	@Override
	public SensorTypeEnum getType() {
		return sensorType;
	}

	@Override
	public DigitalPortEnum getPort() {
		return port;
	}

	@Override
	public String getData() {
		StringBuilder sb = new StringBuilder();
		SampleProvider sp;
		if (sensor instanceof EV3UltrasonicSensor) {
			((EV3UltrasonicSensor) sensor).enable();
			sp = ((EV3UltrasonicSensor) sensor).getDistanceMode();
			final int sampleSize = sp.sampleSize();
			float[] samples = new float[sampleSize];
			for (int i = 0; i < sampleSize; i++) {
				sp.fetchSample(samples, i);
				sb.append(samples[i]).append(",");
			}
			((EV3UltrasonicSensor) sensor).disable();
		}
		return sb.toString();
	}

	@Override
	public void close() {
		((EV3UltrasonicSensor) sensor).disable();
		sensor.close();
	}

	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

}
