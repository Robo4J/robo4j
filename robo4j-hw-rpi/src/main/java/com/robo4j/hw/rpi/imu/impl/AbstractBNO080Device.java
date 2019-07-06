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
import com.robo4j.hw.rpi.imu.bno.DeviceListener;
import com.robo4j.hw.rpi.imu.bno.ShtpPacketRequest;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AbstractBNO080Device base functionality for BNO080 Devices
 *
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

	public static final int SHTP_HEADER_SIZE = 4;
	static byte RECEIVE_WRITE_BYTE = (byte) 0xFF;
	static byte RECEIVE_WRITE_BYTE_CONTINUAL = (byte) 0;
	final List<DeviceListener> listeners = new CopyOnWriteArrayList<>();
	final AtomicBoolean active = new AtomicBoolean(false);
	final AtomicBoolean ready = new AtomicBoolean(false);
	final AtomicInteger commandSequenceNumber = new AtomicInteger(0);

	final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, (r) -> {
		Thread t = new Thread(r, "BNO080 Internal Executor");
		t.setDaemon(true);
		return t;
	});
	private static final short CHANNEL_COUNT = 6; // BNO080 supports 6 channels
	private static final int AWAIT_TERMINATION = 10;
	private final int[] sequenceByChannel = new int[CHANNEL_COUNT];



	@Override
	public abstract boolean start(ShtpSensorReport report, int reportDelay);

	@Override
	public void shutdown() {
		synchronized (executor) {
			active.set(false);
			ready.set(false);
			awaitTermination();
		}
	}

	@Override
	public void addListener(DeviceListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(DeviceListener listener) {
		listeners.remove(listener);
	}

	ShtpPacketRequest prepareShtpPacketRequest(ShtpChannel shtpChannel, int size) {
		ShtpPacketRequest packet = new ShtpPacketRequest(size, sequenceByChannel[shtpChannel.getChannel()]++);
		packet.createHeader(shtpChannel);
		return packet;
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

	private void awaitTermination() {
		try {
			executor.awaitTermination(AWAIT_TERMINATION, TimeUnit.MILLISECONDS);
			executor.shutdown();
		} catch (InterruptedException e) {
			System.err.println(String.format("awaitTermination e: %s", e));
		}
	}

}
