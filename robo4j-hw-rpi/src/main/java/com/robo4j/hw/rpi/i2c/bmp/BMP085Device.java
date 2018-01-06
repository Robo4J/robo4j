/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.i2c.bmp;

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Abstraction to read a Bosch digital barometric pressure sensor
 * (BMP085/BMP180).
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class BMP085Device extends AbstractI2CDevice {
	private static final int DEFAULT_I2C_ADDRESS = 0x77;
	private static final int PRESSURE_SEA_LEVEL = 101325;
	private static final double POW_FACT = 1.0 / 5.225;
	// Calibration data
	private static final int CALIBRATION_START = 0xAA;
	private static final int CALIBRATION_END = 0xBF;

	private static final short BMP085_CONTROL = 0xF4;
	private static final short BMP085_TEMPDATA = 0xF6;
	private static final short BMP085_PRESSUREDATA = 0xF6;
	private static final byte BMP085_READTEMPCMD = 0x2E;
	private static final byte BMP085_READPRESSURECMD = 0x34;

	private final OperatingMode mode;

	// Calibration variables
	private short AC1;
	private short AC2;
	private short AC3;
	private int AC4;
	private int AC5;
	private int AC6;
	private short B1;
	private short B2;
	private short MC;
	private short MD;

	/**
	 * Available operating modes for the BMP085.
	 */
	public enum OperatingMode {
		/**
		 * Max conversion time (pressure): 4.5ms Current draw: 3µA
		 */
		ULTRA_LOW_POWER(45, 3),
		/**
		 * Max conversion time (pressure): 7.5ms Current draw: 5µA
		 */
		STANDARD(75, 5),
		/**
		 * Max conversion time (pressure): 13.5ms Current draw: 7µA
		 */
		HIGH_RES(135, 7),
		/**
		 * Max conversion time (pressure): 25.5ms Current draw: 12µA
		 */
		ULTRA_HIGH_RES(255, 12);

		int waitTime;
		int currentDraw;

		OperatingMode(int maxConversionTime, int currentDraw) {
			this.waitTime = (maxConversionTime + 5) / 10;
			this.currentDraw = currentDraw;
		}

		/**
		 * @return the over sampling setting.
		 */
		public int getOverSamplingSetting() {
			return this.ordinal();
		}

		/**
		 * @return time to wait for a result, in ms.
		 */
		public int getWaitTime() {
			return waitTime;
		}

		/**
		 * @return the average typical current at 1 sample per second, in µA.
		 */
		public int getCurrentDraw() {
			return currentDraw;
		}
	}

	/**
	 * Constructs a BMPDevice using the default settings. (I2CBUS.BUS_1, 0x77)
	 * 
	 * @see #BMP085Device(int, int, OperatingMode)
	 *
	 * @param mode
	 *            operating mode
	 * 
	 * @throws IOException
	 *             if there was communication problem
	 */
	public BMP085Device(OperatingMode mode) throws IOException {
		// 0x77 is the default address used by the AdaFruit BMP board.
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS, mode);
	}

	/**
	 * Creates a software interface to an Adafruit BMP board (BMP085).
	 * 
	 * @param bus
	 *            the I2C bus to use.
	 * @param address
	 *            the address to use.
	 * @param mode
	 *            operating mode
	 * 
	 * @see I2CBus documentation
	 * 
	 * @throws IOException
	 *             if there was communication problem
	 */
	public BMP085Device(int bus, int address, OperatingMode mode) throws IOException {
		super(bus, address);
		this.mode = mode;
		readCalibrationData();
	}

	/**
	 * Returns the temperature in degrees Celcius.
	 * 
	 * @return the temperature in degrees Celcius.
	 * @throws IOException
	 *             if there was communication problem
	 */
	public float readTemperature() throws IOException {
		int UT = readRawTemp();
		int X1 = ((UT - AC6) * AC5) >> 15;
		int X2 = (MC << 11) / (X1 + MD);
		int B5 = X1 + X2;
		return ((B5 + 8) >> 4) / 10.0f;
	}

	/**
	 * Returns the pressure in Pascal.
	 * 
	 * @return the pressure in Pascal.
	 * @throws IOException
	 *             if there was communication problem
	 */
	public int readPressure() throws IOException {
		long p = 0;
		int UT = readRawTemp();
		int UP = readRawPressure();

		int X1 = ((UT - AC6) * AC5) >> 15;
		int X2 = (MC << 11) / (X1 + MD);
		int B5 = X1 + X2;

		int B6 = B5 - 4000;
		X1 = (B2 * ((B6 * B6) >> 12)) >> 11;
		X2 = (AC2 * B6) >> 11;
		int X3 = X1 + X2;
		int B3 = (((AC1 * 4 + X3) << mode.getOverSamplingSetting()) + 2) / 4;

		X1 = (AC3 * B6) >> 13;
		X2 = (B1 * ((B6 * B6) >> 12)) >> 16;
		X3 = ((X1 + X2) + 2) >> 2;
		long B4 = (AC4 * ((long) (X3 + 32768))) >> 15;
		long B7 = ((long) UP - B3) * (50000 >> mode.getOverSamplingSetting());

		if (B7 < 0x80000000) {
			p = (B7 * 2) / B4;
		} else {
			p = (B7 / B4) * 2;
		}

		X1 = (int) ((p >> 8) * (p >> 8));
		X1 = (X1 * 3038) >> 16;
		X2 = (int) (-7357 * p) >> 16;
		p = p + ((X1 + X2 + 3791) >> 4);
		return (int) p;
	}

	/**
	 * Returns the barometric altitude above sea level in meters.
	 * 
	 * @return the barometric altitude above sea level in meters.
	 * @throws IOException
	 *             if there was communication problem
	 */
	public float readAltitude() throws IOException {
		float pressure = readPressure();
		return (float) (44330.0 * (1.0 - Math.pow(pressure / PRESSURE_SEA_LEVEL, POW_FACT)));
	}

	/**
	 * Returns the raw temperature sensor data. Mostly for debugging.
	 * 
	 * @return the raw temperature sensor data.
	 * @throws IOException
	 *             if there was a communication problem
	 */
	public int readRawTemp() throws IOException {
		i2cDevice.write(BMP085_CONTROL, BMP085_READTEMPCMD);
		sleep(50);
		return readU2(BMP085_TEMPDATA);
	}

	/**
	 * Returns the raw pressure sensor data. Mostly for debugging.
	 * 
	 * @return the raw pressure sensor data.
	 * @throws IOException
	 *             if there was a communication problem
	 */
	public int readRawPressure() throws IOException {
		i2cDevice.write(BMP085_CONTROL, BMP085_READPRESSURECMD);
		sleep(mode.getWaitTime());
		return readU3(BMP085_PRESSUREDATA) >> (8 - mode.getOverSamplingSetting());
	}

	private void readCalibrationData() throws IOException {
		int totalBytes = CALIBRATION_END - CALIBRATION_START + 1;
		byte[] bytes = new byte[totalBytes];
		int bytesRead = i2cDevice.read(CALIBRATION_START, bytes, 0, totalBytes);
		if (bytesRead != totalBytes) {
			throw new IOException("Could not read calibration data. Read " + Arrays.toString(bytes) + " of " + totalBytes);
		}

		DataInputStream calibrationData = new DataInputStream(new ByteArrayInputStream(bytes));
		AC1 = calibrationData.readShort();
		AC2 = calibrationData.readShort();
		AC3 = calibrationData.readShort();
		AC4 = calibrationData.readUnsignedShort();
		AC5 = calibrationData.readUnsignedShort();
		AC6 = calibrationData.readUnsignedShort();
		B1 = calibrationData.readShort();
		B2 = calibrationData.readShort();
		calibrationData.readShort(); // MB not used for anything it seems...
		MC = calibrationData.readShort();
		MD = calibrationData.readShort();

		if (Boolean.getBoolean("se.hirt.pi.adafruit.debug")) {
			System.out
					.println(String.format("AC1:%d, AC2:%d, AC3:%d, AC4:%d, AC5:%d, AC6:%d, B1:%d, B2:%d, MC:%d, MD:%d",
							AC1, AC2, AC3, AC4, AC5, AC6, B1, B2, MC, MD));
		}
	}
}
