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
package com.robo4j.hw.rpi.i2c.accelerometer;


import com.robo4j.math.geometry.Tuple3f;

/**
 * Helper class for gathering stats.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Stats {
	private Tuple3f min = new Tuple3f();
	private Tuple3f max = new Tuple3f();
	private Tuple3f sum = new Tuple3f();

	private int count = 0;
	
	public Stats() {
		max.x = Float.MIN_VALUE;
		max.y = Float.MIN_VALUE;
		max.z = Float.MIN_VALUE;
		min.x = Float.MAX_VALUE;
		min.y = Float.MAX_VALUE;
		min.z = Float.MAX_VALUE;
	}
	
	public void addValue(Tuple3f f) {
		updateMin(f);
		updateMax(f);
		sum.add(f);
		count++;
	}
	
	public Tuple3f getMin() {
		return min;
	}
	
	public Tuple3f getMax() {
		return max;
	}
	
	public Tuple3f getSum() {
		return sum;
	}
	
	public Tuple3f getAvg() {
		Tuple3f avg = new Tuple3f();
		avg.x = sum.x / count;
		avg.y = sum.y / count;
		avg.z = sum.z / count;
		return avg;
	}
	
	private boolean updateMin(Tuple3f newVal) {
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

	private boolean updateMax(Tuple3f newVal) {
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
