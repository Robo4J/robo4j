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
 * ShtpOperation wrapper full shtp operation consists from request/response
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ShtpOperation {

    private final ShtpPacketRequest request;
    private final ShtpOperationResponse response;
	private ShtpOperation next;
	private boolean processed;

	public ShtpOperation(ShtpPacketRequest request, ShtpOperationResponse response) {
		this.request = request;
		this.response = response;
		this.next = null;
	}

	public boolean hasRequest(){
	    return request != null;
    }

	public ShtpPacketRequest getRequest() {
		return request;
	}

	public ShtpOperationResponse getResponse() {
		return response;
	}

	public ShtpOperation getNext() {
		return next;
	}

	public void setNext(ShtpOperation next) {
		this.next = next;
	}

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public boolean isProcessed() {
		return processed;
	}

    @Override
    public String toString() {
        return "ShtpOperation{" +
                "request=" + request +
                ", response=" + response +
                ", next=" + next +
                ", processed=" + processed +
                '}';
    }
}
