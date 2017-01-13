/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This Stats.java  is part of robo4j.
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
package com.robo4j.hw.rpi.i2c.accelerometer;

import com.robo4j.hw.rpi.geometry.Float3D;

/**
 * Helper class for gathering stats.
 * 
 * @author Marcus Hirt
 */
public class Stats {
	private Float3D min = new Float3D();
	private Float3D max = new Float3D();
	private Float3D sum = new Float3D();

	private int count = 0;
	
	public Stats() {
		max.x = Float.MIN_VALUE;
		max.y = Float.MIN_VALUE;
		max.z = Float.MIN_VALUE;
		min.x = Float.MAX_VALUE;
		min.y = Float.MAX_VALUE;
		min.z = Float.MAX_VALUE;
	}
	
	public void addValue(Float3D f) {
		updateMin(f);
		updateMax(f);
		sum.add(f);
		count++;
	}
	
	public Float3D getMin() {
		return min;
	}
	
	public Float3D getMax() {
		return max;
	}
	
	public Float3D getSum() {
		return sum;
	}
	
	public Float3D getAvg() {
		Float3D avg = new Float3D();
		avg.x = sum.x / count;
		avg.y = sum.y / count;
		avg.z = sum.z / count;
		return avg;
	}
	
	private boolean updateMin(Float3D newVal) {
		boolean isUpdated = false;
		
		if (newVal.x < min.x) {
			min.x = newVal.x;
			isUpdated = true;
		}
		if (newVal.y < min.y) {
			min.y = newVal.y;
			isUpdated = true;
		}
		if (newVal.z < min.z) {
			min.z = newVal.z;
			isUpdated = true;
		}
		return isUpdated;
	}

	private boolean updateMax(Float3D newVal) {
		boolean isUpdated = false;
		
		if (newVal.x > max.x) {
			max.x = newVal.x;
			isUpdated = true;
		}
		if (newVal.y > max.y) {
			max.y = newVal.y;
			isUpdated = true;
		}
		if (newVal.z > max.z) {
			max.z = newVal.z;
			isUpdated = true;
		}
		return isUpdated;
	}
	
	public String toString() {
		return String.format("Min: %s, Max: %s, Avg: %s", getMin().toString(), getMax().toString(), getAvg().toString());
	}
}
