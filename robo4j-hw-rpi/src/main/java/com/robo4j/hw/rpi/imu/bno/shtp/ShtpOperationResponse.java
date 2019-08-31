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

package com.robo4j.hw.rpi.imu.bno.shtp;

import com.robo4j.hw.rpi.imu.bno.DeviceChannel;
import com.robo4j.hw.rpi.imu.bno.DeviceDeviceReport;
import com.robo4j.hw.rpi.imu.bno.DeviceReport;
import com.robo4j.hw.rpi.imu.bno.DeviceSensorReport;

import java.util.Arrays;
import java.util.Objects;

/**
 * ShtpOperationResponse expected response caused by {@link ShtpPacketRequest}
 *
 * Contains information about the channel, report and values in order relevant
 * for the response example: response is delivered on channel CONTROL(2),
 * COMMAND_RESPONSE(0xF1),
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ShtpOperationResponse {
	private final DeviceChannel channel;
	private final int report;
	private final int[] values;

	public ShtpOperationResponse(DeviceReport report) {
		this(report.getChannel(), report.getId());
	}

	public ShtpOperationResponse(DeviceChannel channel, int report, int... array) {
		this.channel = channel;
		this.report = report;
		if (array == null) {
			this.values = new int[0];
		} else {
			this.values = new int[array.length];
			for (int i = 0; i < array.length; i++) {
				this.values[i] = array[i];
			}
		}
	}

	public DeviceChannel getChannel() {
		return channel;
	}

	public DeviceReport getReport() {
		switch (channel) {
		case CONTROL:
			return DeviceDeviceReport.getById(report);
		case REPORTS:
			return DeviceSensorReport.getById(report);
		default:
			return null;
		}
	}

	public boolean containValues(int... array) {
		if (array == null) {
			return values.length == 0;
		} else {
			return Arrays.equals(values, array);
		}
	}

	@Override
	public String toString() {
		return "ShtpOperationResponse{" + "channel=" + channel + ", report=" + report + ", values=" + Arrays.toString(values) + '}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		ShtpOperationResponse that = (ShtpOperationResponse) o;
		return report == that.report && channel == that.channel && Arrays.equals(values, that.values);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(channel, report);
		result = 31 * result + Arrays.hashCode(values);
		return result;
	}
}
