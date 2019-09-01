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

import com.robo4j.hw.rpi.imu.bno.Bno055CalibrationStatus;
import com.robo4j.hw.rpi.imu.bno.Bno055Device;
import com.robo4j.hw.rpi.imu.bno.Bno055Factory;
import com.robo4j.hw.rpi.imu.bno.Bno055SelfTestResult;
import com.robo4j.hw.rpi.imu.bno.Bno055Device.OperatingMode;
import com.robo4j.math.geometry.Tuple3f;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * An example for the BNO device.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Bno055Example {
	private final static ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);

	private final static class BNOPrinter implements Runnable {

		private final Bno055Device device;

		private BNOPrinter(Bno055Device device) {
			this.device = device;
		}

		@Override
		public void run() {
			try {
				Tuple3f orientation = device.read();
				float temperature = device.getTemperature();

				System.out.println(String.format("heading: %f, roll: %f, pitch: %f - temp:%f", orientation.x,
						orientation.y, orientation.z, temperature));
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Runs an example for the BNO running in serial. Use the appropriate factory
	 * method to instead use I2C.
	 *
	 * @param args
	 *            arguments
	 * @throws IOException
	 *             exception
	 * @throws InterruptedException
	 *             exception
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println("Starting the BNO055 Example.");
		Bno055Device bno = Bno055Factory.createDefaultSerialDevice();
		Thread.sleep(20);

		System.out.println("Resetting device...");
		bno.reset();
		Thread.sleep(20);

		System.out.println("Running Self Test...");
		Bno055SelfTestResult testResult = bno.performSelfTest();
		System.out.println("Result of self test: ");
		System.out.println(testResult);
		Thread.sleep(20);

		System.out.println("Operating mode: " + bno.getOperatingMode());
		if (bno.getOperatingMode() != OperatingMode.NDOF) {
			System.out.println("Switching mode to NDOF");
			bno.setOperatingMode(OperatingMode.NDOF);
			System.out.println("Operating mode: " + bno.getOperatingMode());
		}

		System.out.println("Starting calibration sequence...");
		Bno055CalibrationStatus calibrationStatus = null;
		while (!(calibrationStatus = bno.getCalibrationStatus()).isFullyCalibrated()) {
			System.out.println(String.format(
					"Calibration status: system:%s, gyro:%s, accelerometer:%s, magnetometer:%s",
					calibrationStatus.getSystemCalibrationStatus(), calibrationStatus.getGyroCalibrationStatus(),
					calibrationStatus.getAccelerometerCalibrationStatus(),
					calibrationStatus.getAccelerometerCalibrationStatus()));
			Thread.sleep(500);
		}
		System.out.println("System fully calibrated. Now printing data. Press <Enter> to quit!");

		EXECUTOR.scheduleAtFixedRate(new BNOPrinter(bno), 40, 500, TimeUnit.MILLISECONDS);
		System.in.read();
		EXECUTOR.shutdown();
		EXECUTOR.awaitTermination(1000, TimeUnit.MILLISECONDS);
		System.out.println("Bye, bye!");
		bno.shutdown();
	}

}
