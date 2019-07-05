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

import com.robo4j.hw.rpi.imu.BNO080Device;

import java.util.Arrays;
import java.util.Objects;

/**
 * ShtpOperationResponse expected response caused by {@link ShtpPacketRequest}
 *
 * does contain information about the channel, report and values in order relevant for the response
 * exmple: response is delivered on channel CONTROL(2), COMMAND_RESPONSE(0xF1),
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ShtpOperationResponse {
    private final BNO080Device.ShtpChannel channel;
    private final int report;
    private final int[] values;

    public ShtpOperationResponse(BNO080Device.ShtpReport report){
        this(report.getChannel(), report.getId());
    }

    public ShtpOperationResponse(BNO080Device.ShtpChannel channel, int report, int... array) {
        this.channel = channel;
        this.report = report;
        if(array == null) {
           this.values = new int[0];
        } else {
            this.values = new int[array.length];
            for (int i = 0; i < array.length; i++) {
                this.values[i] = array[i];
            }
        }
    }

    public BNO080Device.ShtpChannel getChannel() {
        return channel;
    }

    public BNO080Device.ShtpReport getReport() {
        switch (channel){
            case CONTROL:
                return BNO080Device.ShtpDeviceReport.getById(report);
            case REPORTS:
                return BNO080Device.ShtpSensorReport.getById(report);
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
        return "ShtpOperationResponse{" +
                "channel=" + channel +
                ", report=" + report +
                ", values=" + Arrays.toString(values) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShtpOperationResponse that = (ShtpOperationResponse) o;
        return report == that.report &&
                channel == that.channel &&
                Arrays.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(channel, report);
        result = 31 * result + Arrays.hashCode(values);
        return result;
    }
}
