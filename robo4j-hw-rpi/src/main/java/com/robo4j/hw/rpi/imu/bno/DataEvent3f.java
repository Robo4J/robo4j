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

package com.robo4j.hw.rpi.imu.bno;

import java.io.Serializable;
import java.util.Objects;

import com.robo4j.math.geometry.Tuple3f;

/**
 * Event used for data produced by the Bno080.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DataEvent3f implements Serializable {
	private static final long serialVersionUID = 1L;

	private final DataEventType type;
	private final int status;
	private final Tuple3f data;
	private final long timestamp;

	public DataEvent3f(DataEventType type, int status, Tuple3f data, long timestamp) {
		this.type = type;
		this.status = status;
		this.data = data;
		this.timestamp = timestamp;
	}

	public DataEventType getType() {
		return type;
	}

	public int getStatus() {
		return status;
	}

	public Tuple3f getData() {
		return data;
	}

	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		DataEvent3f that = (DataEvent3f) o;
		return status == that.status && timestamp == that.timestamp && type == that.type && Objects.equals(data, that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, status, data, timestamp);
	}

	@Override
	public String toString() {
		return "DataEvent3f{" + "type=" + type + ", status=" + status + ", data=" + data + ", timestamp=" + timestamp + '}';
	}
}
