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

package com.robo4j.hw.rpi.imu.bno.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import com.robo4j.hw.rpi.imu.bno.Bno080Device;
import com.robo4j.hw.rpi.imu.bno.DataListener;
import com.robo4j.hw.rpi.imu.bno.shtp.ControlReportId;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpChannel;
import com.robo4j.hw.rpi.imu.bno.shtp.ShtpPacketRequest;

/**
 * AbstractBNO080Device base functionality for BNO080 Devices
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class AbstractBno080Device implements Bno080Device {
	public static final int SHTP_HEADER_SIZE = 4;

	static byte RECEIVE_WRITE_BYTE = (byte) 0xFF;
	static byte RECEIVE_WRITE_BYTE_CONTINUAL = (byte) 0;
	final List<DataListener> listeners = new CopyOnWriteArrayList<>();
	final AtomicBoolean active = new AtomicBoolean(false);
	final AtomicBoolean ready = new AtomicBoolean(false);
	final AtomicInteger commandSequenceNumber = new AtomicInteger(0);

	private static final short CHANNEL_COUNT = 6; // BNO080 supports 6 channels
	private static final int AWAIT_TERMINATION = 10;
	private final int[] sequenceNumberByChannel = new int[CHANNEL_COUNT];

	/**
	 * Record IDs (figure 29, page 29 reference manual). These are used to read
	 * the metadata for each sensor type.
	 */
	enum FrsRecord {

		//@formatter:off
        NONE                        (-1),
        ACCELEROMETER               (0xE302),
        GYROSCOPE_CALIBRATED        (0xE306),
        MAGNETIC_FIELD_CALIBRATED   (0xE309),
        ROTATION_VECTOR             (0xE30B);
        //@formatter:on

		private final int id;

		FrsRecord(int recordId) {
			this.id = recordId;
		}

		public int getId() {
			return id;
		}

		public static FrsRecord getById(int id) {
			for (FrsRecord r : values()) {
				if (id == r.getId()) {
					return r;
				}
			}
			return NONE;
		}
	}

	/**
	 * Command IDs (section 6.4, page 42 in the manual). These are used to
	 * calibrate, initialize, set orientation, tare etc the sensor.
	 */
	public enum CommandId {
		//@formatter:off
        NONE            (0),
        ERRORS          (1),
        COUNTER         (2),
        TARE            (3),
        INITIALIZE      (4),
        DCD             (6),
        ME_CALIBRATE    (7),
        DCD_PERIOD_SAVE (9),
        OSCILLATOR      (10),
        CLEAR_DCD       (11);
        //@formatter:on

		private static Map<Integer, CommandId> map = getMap();
		private final int id;

		CommandId(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public static CommandId getById(int id) {
			CommandId command = map.get(id);
			return command == null ? NONE : command;
		}

		private static Map<Integer, CommandId> getMap() {
			Map<Integer, CommandId> map = new HashMap<>();
			for (CommandId c : values()) {
				map.put(c.id, c);
			}
			return map;
		}
	}

	/**
	 * Sensor calibration targets.
	 */
	enum DeviceCalibrate {
		//@formatter:off
        NONE            (-1),
        ACCEL           (0),
        GYRO            (1),
        MAG             (2),
        PLANAR_ACCEL    (3),
        ACCEL_GYRO_MAG  (4),
        STOP            (5);
        //@formatter:on

		private int id;

		DeviceCalibrate(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public static DeviceCalibrate getById(int id) {
			for (DeviceCalibrate r : values()) {
				if (id == r.getId()) {
					return r;
				}
			}
			return NONE;
		}
	}

	static class ShtpPacketBodyBuilder {
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

	final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1, (r) -> {
		Thread t = new Thread(r, "Bno080 Internal Executor");
		t.setDaemon(true);
		return t;
	});

	@Override
	public void shutdown() {
		synchronized (executor) {
			active.set(false);
			ready.set(false);
			awaitTermination();
		}
	}

	@Override
	public void addListener(DataListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeListener(DataListener listener) {
		listeners.remove(listener);
	}

	/**
	 * SHTP packet contains 1 byte to get Error report. Packet is sent to the
	 * COMMAND channel
	 *
	 * @return error request packet
	 */
	public ShtpPacketRequest getErrorRequest() {
		ShtpPacketRequest result = prepareShtpPacketRequest(ShtpChannel.COMMAND, 1);
		result.addBody(0, 0x01 & 0xFF);
		return result;
	}

	ShtpPacketRequest prepareShtpPacketRequest(ShtpChannel shtpChannel, int size) {
		ShtpPacketRequest packet = new ShtpPacketRequest(size, sequenceNumberByChannel[shtpChannel.getChannel()]++);
		packet.createHeader(shtpChannel);
		return packet;
	}

	ShtpPacketRequest getProductIdRequest() {
		// Check communication with device
		// bytes: Request the product ID and reset info, Reserved
		ShtpPacketRequest result = prepareShtpPacketRequest(ShtpChannel.CONTROL, 2);
		result.addBody(0, ControlReportId.PRODUCT_ID_REQUEST.getId());
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
