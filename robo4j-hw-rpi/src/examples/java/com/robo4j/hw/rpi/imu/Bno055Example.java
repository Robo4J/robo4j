/*
 * Copyright (c) 2014, 2026, Marcus Hirt, Miroslav Wengner
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
import com.robo4j.hw.rpi.imu.bno.Bno055Device.OperatingMode;
import com.robo4j.hw.rpi.imu.bno.Bno055Factory;
import com.robo4j.hw.rpi.imu.bno.Bno055SelfTestResult;
import com.robo4j.math.geometry.Tuple3f;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.IO.*;

/**
 * An example for the BNO device.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Bno055Example {
    private static final int TIMEOUT_SEC = 1;
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

                println("heading: %s, roll: %s, pitch: %s - temp:%s".formatted(orientation.x,
                        orientation.y, orientation.z, temperature));
            } catch (Throwable e) {
                System.err.println("error");
                e.printStackTrace();
            }
        }

    }

    /**
     * Runs an example for the BNO running in serial. Use the appropriate factory
     * method to instead use I2C.
     *
     * @param args arguments
     * @throws IOException          exception
     * @throws InterruptedException exception
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // TODO: review sleeps
        println("Starting the BNO055 Example.");
        Bno055Device bno = Bno055Factory.createDefaultSerialDevice();
        Thread.sleep(20);

        println("Resetting device...");
        bno.reset();
        Thread.sleep(20);

        println("Running Self Test...");
        Bno055SelfTestResult testResult = bno.performSelfTest();
        println("Result of self test: ");
        println("result:" + testResult);
        Thread.sleep(20);

        println("Operating mode: " + bno.getOperatingMode());
        if (bno.getOperatingMode() != OperatingMode.NDOF) {
            println("Switching mode to NDOF");
            bno.setOperatingMode(OperatingMode.NDOF);
            println("Operating mode: " + bno.getOperatingMode());
        }

        println("Starting calibration sequence...");
        Bno055CalibrationStatus calibrationStatus = null;
        while (!(calibrationStatus = bno.getCalibrationStatus()).isFullyCalibrated()) {
            println("Calibration status: system:%s, gyro:%s, accelerometer:%s, magnetometer:%s".formatted(calibrationStatus.getSystemCalibrationStatus(), calibrationStatus.getGyroCalibrationStatus(), calibrationStatus.getAccelerometerCalibrationStatus(), calibrationStatus.getAccelerometerCalibrationStatus()));
            Thread.sleep(500);
        }
        println("System fully calibrated. Now printing data. Press <Enter> to quit!");

        EXECUTOR.scheduleAtFixedRate(new BNOPrinter(bno), 40, 500, TimeUnit.MILLISECONDS);
        readln();
        EXECUTOR.shutdown();
        var terminated = EXECUTOR.awaitTermination(TIMEOUT_SEC, TimeUnit.SECONDS);
        if (!terminated) {
            System.err.println("not terminated properly");
            EXECUTOR.shutdownNow();
        }
        println("Bye, bye!");
        bno.shutdown();
    }

}
