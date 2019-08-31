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
import com.robo4j.hw.rpi.imu.bno.DeviceSensorReport;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for BNO080 Devices.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface BNO080Device {

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
        CLEAR_DCD       (11);
        //@formatter:on

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

	void addListener(DeviceListener listener);

	void removeListener(DeviceListener listener);

	boolean start(DeviceSensorReport report, int reportDelay);

	boolean stop();

	void shutdown();

}
