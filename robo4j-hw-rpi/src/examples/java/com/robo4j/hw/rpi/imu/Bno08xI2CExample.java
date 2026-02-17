/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.hw.rpi.imu.bno.Bno080Device;
import com.robo4j.hw.rpi.imu.bno.DataEvent3f;
import com.robo4j.hw.rpi.imu.bno.DataListener;
import com.robo4j.hw.rpi.imu.bno.VectorEvent;
import com.robo4j.hw.rpi.imu.bno.bno08x.Bno08xFactory;
import com.robo4j.hw.rpi.imu.bno.shtp.SensorReportId;
import com.robo4j.math.geometry.QuaternionUtils;
import com.robo4j.math.geometry.Tuple3d;
import com.robo4j.math.geometry.Tuple3f;
import com.robo4j.math.geometry.Tuple4d;

/**
 * Example using the BNO08x driver over I2C (e.g. SparkFun QWIIC BNO086).
 * Prints rotation vector as Euler angles (heading, pitch, roll) to the console.
 * Useful for vehicle orientation control.
 * <p>
 * <b>WARNING:</b> The BNO08x uses I2C clock stretching which is not well supported
 * on Raspberry Pi. This may cause communication failures or bus lockups.
 * Consider using SPI instead (see {@link Bno08xSPIExample}) or connect the INT pin
 * for interrupt-driven communication.
 * </p>
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Bno08xI2CExample {

    public static void main(String[] args) throws Exception {
        boolean clearDcd = args.length > 0 && "--clear-dcd".equals(args[0]);

        DataListener listener = Bno08xI2CExample::onEvent;
        System.out.println("BNO08x I2C Rotation Vector Example (Euler Angles)");
        Bno080Device device = Bno08xFactory.createDefaultI2CDevice();
        device.addListener(listener);
        device.start(SensorReportId.ROTATION_VECTOR, 1000);

        if (clearDcd) {
            System.out.println("Clearing saved calibration data (DCD)...");
            device.clearCalibration();
        }

        System.out.println("Press <Enter> to quit!");
        System.in.read();
        device.shutdown();
    }

    private static void onEvent(DataEvent3f event) {
        if (event instanceof VectorEvent vectorEvent) {
            Tuple3f data = vectorEvent.getData();
            Tuple4d quaternion = new Tuple4d(data.x, data.y, data.z, vectorEvent.getQuatReal());
            Tuple3d euler = QuaternionUtils.toEuler(quaternion);

            System.out.printf("\rHeading: %6.1f°  Pitch: %6.1f°  Roll: %6.1f°  (accuracy: %.2f°)   ",
                    Math.toDegrees(euler.x), Math.toDegrees(euler.y), Math.toDegrees(euler.z),
                    Math.toDegrees(vectorEvent.getRadianAccuracy()));
        } else {
            System.out.println("Event: " + event);
        }
    }
}
