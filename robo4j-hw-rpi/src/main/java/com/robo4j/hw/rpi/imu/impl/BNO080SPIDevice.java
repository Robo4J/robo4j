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

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiMode;
import com.pi4j.io.spi.impl.SpiDeviceImpl;

import java.io.IOException;

/**
 * Abstraction for a BNO080 absolute orientation device.
 * 
 * Channel 0: the SHTP command channel
 * Channel 1: executable Channel 2: sensor hub control channel
 * Channel 3: input sensor reports (non-wake, not gyroRV)
 * Channel 4: wake input sensor reports (for sensors configured as wake up sensors)
 * Channel 5: gyro rotation vector
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BNO080SPIDevice {

	public static final SpiMode DEFAULT_SPI_MODE = SpiMode.MODE_0;
	public static final int DEFAULT_SPI_SPEED = 3000000; // 3MHz maximum SPI speed
	public static final short CHANNEL_COUNT = 6; // BNO080 supports 6 channels

	//Registers
    private static final  int CHANNEL_COMMAND = 0;
    private static final  int CHANNEL_EXECUTABLE = 1;
    private static final  int CHANNEL_CONTROL = 2;
    private static final  int CHANNEL_REPORTS = 3;
    private static final  int CHANNEL_WAKE_REPORTS = 4;
    private static final  int CHANNEL_GYRO = 5;

    //All the ways we can configure or talk to the BNO080, figure 34, page 36 reference manual
    //These are used for low level communication with the sensor, on channel 2
    private static final int SHTP_REPORT_COMMAND_RESPONSE = 0xF1;
    private static final int SHTP_REPORT_COMMAND_REQUEST = 0xF2;
    private static final int SHTP_REPORT_FRS_READ_RESPONSE = 0xF3;
    private static final int SHTP_REPORT_FRS_READ_REQUEST = 0xF4;
    private static final int SHTP_REPORT_PRODUCT_ID_RESPONSE = 0xF8;
    private static final int SHTP_REPORT_PRODUCT_ID_REQUEST = 0xF9;
    private static final int SHTP_REPORT_BASE_TIMESTAMP = 0xFB;
    private static final int SHTP_REPORT_SET_FEATURE_COMMAND = 0xFD;

    //All the different sensors and features we can get reports from
    //These are used when enabling a given sensor
    private static final int SENSOR_REPORTID_ACCELEROMETER = 0x01;
    private static final int SENSOR_REPORTID_GYROSCOPE = 0x02;
    private static final int SENSOR_REPORTID_MAGNETIC_FIELD = 0x03;
    private static final int SENSOR_REPORTID_LINEAR_ACCELERATION = 0x04;
    private static final int SENSOR_REPORTID_ROTATION_VECTOR = 0x05;
    private static final int SENSOR_REPORTID_GRAVITY = 0x06;
    private static final int SENSOR_REPORTID_GAME_ROTATION_VECTOR = 0x08;
    private static final int SENSOR_REPORTID_GEOMAGNETIC_ROTATION_VECTOR = 0x09;
    private static final int SENSOR_REPORTID_TAP_DETECTOR = 0x10;
    private static final int SENSOR_REPORTID_STEP_COUNTER = 0x11;
    private static final int SENSOR_REPORTID_STABILITY_CLASSIFIER = 0x13;
    private static final int SENSOR_REPORTID_PERSONAL_ACTIVITY_CLASSIFIER = 0x1E;

    //Record IDs from figure 29, page 29 reference manual
    //These are used to action the metadata for each sensor type
    private static final int FRS_RECORDID_ACCELEROMETER = 0xE302;
    private static final int FRS_RECORDID_GYROSCOPE_CALIBRATED = 0xE306;
    private static final int FRS_RECORDID_MAGNETIC_FIELD_CALIBRATED = 0xE309;
    private static final int FRS_RECORDID_ROTATION_VECTOR = 0xE30B;

    //Command IDs from section 6.4, page 42
    //These are used to calibrate, initialize, set orientation, tare etc the sensor
    private static final int COMMAND_ERRORS = 1;
    private static final int COMMAND_COUNTER = 2;
    private static final int COMMAND_TARE = 3;
    private static final int COMMAND_INITIALIZE = 4;
    private static final int COMMAND_DCD = 6;
    private static final int COMMAND_ME_CALIBRATE = 7;
    private static final int COMMAND_DCD_PERIOD_SAVE = 9;
    private static final int COMMAND_OSCILLATOR = 10;
    private static final int COMMAND_CLEAR_DCD = 11;

    private static final int CALIBRATE_ACCEL = 0;
    private static final int CALIBRATE_GYRO = 1;
    private static final int CALIBRATE_MAG = 2;
    private static final int CALIBRATE_PLANAR_ACCEL = 3;
    private static final int CALIBRATE_ACCEL_GYRO_MAG = 4;
    private static final int CALIBRATE_STOP = 5;

    private static final int MAX_PACKET_SIZE = 128;     //Packets can be up to 32k but we don't have that much RAM.
    private static final int MAX_METADATA_SIZE = 9;  //This is in words. There can be many but we mostly only care about the first 9 (Qs, range, etc)


    private SpiDevice spiDevice;
    private int imuCSPin = 10;
    private int imuWAKPin = 9;
    private int imuINTPin = 8;
    private int imuRSTPin = 7;

	public BNO080SPIDevice() throws IOException, InterruptedException {

	}

	public void init() throws IOException, InterruptedException {
		this.spiDevice = new SpiDeviceImpl(SpiChannel.CS0, DEFAULT_SPI_SPEED, DEFAULT_SPI_MODE);
	}

	public void action() throws InterruptedException, IOException {

	    Thread.sleep(250);
        System.out.println("READ");
	    for(int i=0; i < CHANNEL_COUNT; i++){
            byte[] message1 = shtpHeaderAndMessage(0,1,  i, 0, 0 );
            byte[] response = spiDevice.write(message1);
            for(byte r: response){
                System.out.print(Byte.toUnsignedInt(r)+",");
            }
            System.out.print("\n");
        }

	}



	public byte[] shtpHeaderAndMessage(int lengthLSB, int lengthMSB, int channel, int sequenceNum, int message){
	    return new byte[]{
                (byte)lengthLSB,
                (byte) lengthMSB,
                (byte) (0b10000000 |( ((channel & 5) << 4))),
                (byte) sequenceNum,
                (byte) message
        };
    }

}
