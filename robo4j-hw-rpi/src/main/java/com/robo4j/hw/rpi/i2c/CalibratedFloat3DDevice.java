/*
 * Copyright (C) 2014-2017. Miroslav Wengner, Marcus Hirt
 * This CalibratedFloat3DDevice.java  is part of robo4j.
 * module: robo4j-hw-rpi
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
package com.robo4j.hw.rpi.i2c;

import java.io.IOException;

import com.robo4j.math.geometry.Float3D;

/**
 * Wrapper class for readable devices returning Float3D, allowing for calibration.
 * 
 * @author Marcus Hirt
 */
public class CalibratedFloat3DDevice implements ReadableDevice<Float3D> {
	private final Float3D centerOffsets; 
	private final Float3D rangeMultipliers;
	private final ReadableDevice<Float3D> device;

	public CalibratedFloat3DDevice(ReadableDevice<Float3D> device, Float3D offsets, Float3D multipliers) {
		this.device = device;
		this.centerOffsets = offsets;
		this.rangeMultipliers = multipliers;
	}
	
	public Float3D read() throws IOException {
		Float3D value = device.read();
		value.add(centerOffsets);
		value.multiply(rangeMultipliers);
		return value;
	}
	
	public void setCalibration(Float3D offsets, Float3D multipliers) {
		centerOffsets.set(offsets);
		rangeMultipliers.set(multipliers);
		System.out.println("Gyro offsets: " + centerOffsets);
		System.out.println("Gyro multipliers: " + rangeMultipliers);
	}
	
	protected Float3D getRangeMultipliers() {
		return rangeMultipliers;
	}
	
	protected Float3D getCenterOffsets() {
		return centerOffsets;
	}
}
