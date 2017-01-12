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
