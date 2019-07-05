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

package com.robo4j.hw.rpi.imu.impl;

import com.robo4j.hw.rpi.imu.BNO080Device;
import com.robo4j.hw.rpi.imu.BNO80DeviceListener;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketRequest;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketResponse;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class AbstractBNO080Device implements BNO080Device {

	class ShtpPacketBodyBuilder {
		private int[] body;
		private final AtomicInteger counter = new AtomicInteger();

		ShtpPacketBodyBuilder(int size) {
			body = new int[size];
		}

		ShtpPacketBodyBuilder addElement(int value) {
			this.body[counter.getAndIncrement()] = value;
			return this;
		}

		int[] build() {
			return body;
		}
	}

	public static final short CHANNEL_COUNT = 6; // BNO080 supports 6 channels
	public static final int SHTP_HEADER_SIZE = 4;

	private static final int AWAIT_TERMINATION = 10;

	final int[] sequenceByChannel = new int[CHANNEL_COUNT];
	final List<BNO80DeviceListener> listeners = new CopyOnWriteArrayList<>();
	final AtomicBoolean active = new AtomicBoolean(false);
	final AtomicInteger commandSequenceNumber = new AtomicInteger(0);

	final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, (r) -> {
		Thread t = new Thread(r, "BNO080 Internal Executor");
		t.setDaemon(true);
		return t;
	});

	@Override
	public abstract boolean start(ShtpSensorReport report, int reportDelay);

	@Override
	public void shutdown() {
		synchronized (executor) {
			active.set(false);
			awaitTermination();
		}
	}

	public void addListener(BNO80DeviceListener listener) {
		listeners.add(listener);
	}

	ShtpPacketRequest prepareShtpPacketRequest(ShtpChannel shtpChannel, int size) {
		ShtpPacketRequest packet = new ShtpPacketRequest(size, sequenceByChannel[shtpChannel.getChannel()]++);
		packet.createHeader(shtpChannel);
		return packet;
	}

	boolean processShtpReportResponse(ShtpDeviceReport report) {
		switch (report) {
		case COMMAND_RESPONSE:
		case FRS_READ_RESPONSE:
		case PRODUCT_ID_RESPONSE:
		case BASE_TIMESTAMP:
		case GET_FEATURE_RESPONSE:
		case FLUSH_COMPLETED:
			System.out.println("processShtpReportResponse response: " + report);
			return true;
		default:
			System.out.println("processShtpReportResponse: received not valid response: " + report);
			return false;
		}
	}

	ShtpPacketRequest getProductIdRequest() {
		// Check communication with device
		// bytes: Request the product ID and reset info, Reserved
		ShtpPacketRequest result = prepareShtpPacketRequest(ShtpChannel.CONTROL, 2);
		result.addBody(0, ShtpDeviceReport.PRODUCT_ID_REQUEST.getId());
		result.addBody(1, 0);
		return result;
	}

	ShtpPacketRequest getSoftResetPacket() {
		ShtpChannel shtpChannel = ShtpChannel.EXECUTABLE;
		ShtpPacketRequest packet = prepareShtpPacketRequest(shtpChannel, 1);
		packet.addBody(0, 1);
		return packet;
	}

	boolean containsResponseCode(ShtpPacketResponse response, ShtpDeviceReport expectedReport) {
		if (response.dataAvailable()) {
			ShtpDeviceReport report = ShtpDeviceReport.getById(response.getBodyFirst());
			return expectedReport.equals(report);
		} else {
			System.out.println("containsResponseCode: No Data");
		}
		return false;
	}

	private void awaitTermination() {
		try {
			executor.awaitTermination(AWAIT_TERMINATION, TimeUnit.MILLISECONDS);
			executor.shutdown();
		} catch (InterruptedException e) {
			System.err.println(String.format("awaitTermination e: %s", e));
		}
	}

	/**
	 *
	 * @param array
	 *            byte array
	 * @return unsigned 8-bit int
	 */
	int toInt8U(byte[] array) {
		return array[0] & 0xFF;
	}

	int toInt8U(byte b) {
		return b & 0xFF;
	}

}
