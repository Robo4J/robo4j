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

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ShtpOperation {

    private boolean processed;
    private ShtpPacketRequest  request;
    private ShtpPacketResponse response;
    private ShtpOperation next;

    public ShtpOperation(ShtpPacketRequest request, ShtpPacketResponse response) {
        this.request = request;
        this.response = response;
        this.next = null;
    }

    public ShtpPacketRequest getRequest() {
        return request;
    }

    public ShtpPacketResponse getResponse() {
        return response;
    }

    public ShtpOperation getNext() {
        return next;
    }

    public void setNext(ShtpOperation next) {
        this.next = next;
    }
}
