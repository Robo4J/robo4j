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

package com.robo4j.hw.rpi.imu;

import com.robo4j.hw.rpi.imu.bno.DeviceListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for BNO080 Devices, currently used for SPI
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface BNO080Device {

	interface ShtpReport {

		int getId();

		ShtpChannel getChannel();

	}

	/**
	 * available registers
	 */
	enum ShtpChannel {
		//@formatter:off
        NONE            (-1),
        COMMAND         (0),
        EXECUTABLE      (1),
        CONTROL         (2),
        REPORTS         (3),
        WAKE_REPORTS    (4),
        GYRO            (5)
        ;
        //@formatter:on

		private static final Map<Byte, ShtpChannel> map = getMap();
		private byte channel;

		ShtpChannel(int channel) {
			this.channel = (byte) channel;
		}

		public byte getChannel() {
			return channel;
		}

		public static ShtpChannel getByChannel(byte channel) {
			ShtpChannel result = map.get(channel);
			return result == null ? NONE : result;
		}

		private static Map<Byte, ShtpChannel> getMap() {
			Map<Byte, ShtpChannel> map = new HashMap<>();
			for (ShtpChannel ch : values()) {
				map.put(ch.channel, ch);
			}
			return map;
		}
	}

	/**
	 * All the ways we can configure or talk to the BNO080, figure 34, page 36
	 * reference manual These are used for low level communication with the sensor,
	 * on ShtpChannel 2 (CONTROL)
	 */
	enum ShtpDeviceReport implements ShtpReport {
		//@formatter:off
        NONE                                    (-1),
        ADVERTISEMENT                           (0x00),
        COMMAND_RESPONSE                        (0xF1),
        COMMAND_REQUEST                         (0xF2),
        FRS_READ_RESPONSE                       (0xF3),
        FRS_READ_REQUEST                        (0xF4),
        PRODUCT_ID_RESPONSE                     (0xF8),
        PRODUCT_ID_REQUEST                      (0xF9),
        BASE_TIMESTAMP                          (0xFB),
        SET_FEATURE_COMMAND                     (0xFD),
        GET_FEATURE_REQUEST                     (0xFE),
        GET_FEATURE_RESPONSE                    (0xFC),
        FORCE_SENSOR_FLUSH                      (0xF0),
        FLUSH_COMPLETED                         (0xEF)
        ;
        //@formatter:on

		private final static Map<Integer, ShtpDeviceReport> map = getMap();
		private final int id;
		private final ShtpChannel shtpChannel = ShtpChannel.CONTROL;

		ShtpDeviceReport(int id) {
			this.id = id;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public ShtpChannel getChannel() {
			return shtpChannel;
		}

		public static ShtpDeviceReport getById(int id) {
			ShtpDeviceReport report = map.get(id);
			return report == null ? NONE : report;
		}

		private static Map<Integer, ShtpDeviceReport> getMap() {
			Map<Integer, ShtpDeviceReport> map = new HashMap<>();
			for (ShtpDeviceReport r : values()) {
				map.put(r.getId(), r);
			}
			return map;
		}
	}

	/**
	 * Sensor reports received on ShtpChannel 3
	 */
	enum ShtpSensorReport implements ShtpReport {
		//@formatter:off
        NONE                            (-1),
        ACCELEROMETER                   (0x01),
        GYROSCOPE                       (0x02),
        MAGNETIC_FIELD                  (0x03),
        LINEAR_ACCELERATION             (0x04),
        ROTATION_VECTOR                 (0x05),
        GRAVITY                         (0x06),
        GYRO_UNCALIBRATED               (0x07),
        GAME_ROTATION_VECTOR            (0x08),
        GEOMAGNETIC_ROTATION_VECTOR     (0x09),
        TAP_DETECTOR                    (0x10),
        STEP_COUNTER                    (0x11),
        SIGNIFICANT_MOTION              (0x12),
        STABILITY_CLASSIFIER            (0x13),
        RAW_ACCELEROMETER               (0x14),
        RAW_GYROSCOPE                   (0x15),
        RAW_MAGNETOMETER                (0x16),
        STEP_DETECTOR                   (0x18),
        SHAKE_DETECTOR                  (0x19),
        TILT_DETECTOR                   (0x20),
        POCKET_DETECTOR                 (0x21),
        CIRCLE_DETECTOR                 (0x22),
        HEART_RATE_MONITOR              (0x23),
        ARVR_STAB_ROTATION_VECTOR       (0x28),
        ARVR_STAB_GAME_ROTATION_VECTOR  (0x29),
        FLIP_DETECTOR                   (0x1A),
        PICKUP_DETECTOR                 (0x1B),
        STABILITY_DETECTOR              (0x1C),
        PERSONAL_ACTIVITY_CLASSIFIER    (0x1E),
        GYRO_INT_ROTATION_VECTOR        (0x2A),
        PRESSURE                        (0x0A),
        AMBIENT_LIGHT                   (0x0B),
        HUMIDITY                        (0x0C),
        PROXIMITY                       (0x0D),
        TEMPERATURE                     (0x0E),
        BASE_TIMESTAMP                  (0xFB),
        ;
        //@formatter:on

		private final int id;
		private final ShtpChannel shtpChannel = ShtpChannel.REPORTS;

		ShtpSensorReport(int id) {
			this.id = id;
		}

		@Override
		public int getId() {
			return id;
		}

		@Override
		public ShtpChannel getChannel() {
			return shtpChannel;
		}

		public static ShtpSensorReport getById(int code) {
			for (ShtpSensorReport r : values()) {
				if ((code & 0xFF) == r.getId()) {
					return r;
				}
			}
			return NONE;
		}
	}

	/**
	 * Record IDs from figure 29, page 29 reference manual These are used to read
	 * the metadata for each sensor type
	 */
	enum FrsRecord {

		//@formatter:off
        NONE                        (-1),
        ACCELEROMETER               (0xE302),
        GYROSCOPE_CALIBRATED        (0xE306),
        MAGNETIC_FIELD_CALIBRATED   (0xE309),
        ROTATION_VECTOR             (0xE30B)
        ;
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
	 * Command IDs from section 6.4, page 42 These are used to calibrate,
	 * initialize, set orientation, tare etc the sensor
	 */
	enum DeviceCommand {
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
        CLEAR_DCD       (11)
        //@formatter:on
		;

		private static Map<Integer, DeviceCommand> map = getMap();
		private final int id;

		DeviceCommand(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		public static DeviceCommand getById(int id) {
			DeviceCommand command = map.get(id);
			return command == null ? NONE : command;
		}

		private static Map<Integer, DeviceCommand> getMap() {
			Map<Integer, DeviceCommand> map = new HashMap<>();
			for (DeviceCommand c : values()) {
				map.put(c.id, c);
			}
			return map;
		}
	}

	enum DeviceCalibrate {
		//@formatter:off
        NONE            (-1),
        ACCEL           (0),
        GYRO            (1),
        MAG             (2),
        PLANAR_ACCEL    (3),
        ACCEL_GYRO_MAG  (4),
        STOP            (5)
        //@formatter:on
		;
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

	void addListener(DeviceListener listener);

	void removeListener(DeviceListener listener);

	boolean start(ShtpSensorReport report, int reportDelay);

	boolean stop();

	void shutdown();

}
