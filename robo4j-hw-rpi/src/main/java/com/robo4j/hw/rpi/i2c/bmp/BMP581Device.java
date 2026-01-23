/*
 * Copyright (c) 2026, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.utils.I2cBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Abstraction to read a Bosch BMP581 digital barometric pressure sensor.
 * <p>
 * The BMP581 is a high-accuracy barometric pressure sensor with:
 * <ul>
 *   <li>Pressure range: 30-125 kPa</li>
 *   <li>Temperature range: -40C to 85C</li>
 *   <li>No external calibration data required (internal compensation)</li>
 * </ul>
 *
 * @see <a href="https://www.bosch-sensortec.com/media/boschsensortec/downloads/datasheets/bst-bmp581-ds004.pdf">BMP581 Datasheet</a>
 * @author Marcus Hirt (@hirt)
 */
public final class BMP581Device extends AbstractI2CDevice {
    private static final Logger LOGGER = LoggerFactory.getLogger(BMP581Device.class);

    // Default I2C address (SDO connected to VDDIO)
    private static final int DEFAULT_I2C_ADDRESS = 0x47;

    // Chip identification
    private static final int CHIP_ID_PRIMARY = 0x50;
    private static final int CHIP_ID_SECONDARY = 0x51;

    // Register addresses
    private static final int REG_CHIP_ID = 0x01;
    private static final int REG_TEMP_DATA_XLSB = 0x1D;
    private static final int REG_PRESS_DATA_XLSB = 0x20;
    private static final int REG_INT_STATUS = 0x27;
    private static final int REG_OSR_CONFIG = 0x36;
    private static final int REG_ODR_CONFIG = 0x37;
    private static final int REG_CMD = 0x7E;

    // Commands
    private static final byte CMD_SOFT_RESET = (byte) 0xB6;

    // Data ready bit in INT_STATUS register
    private static final int DRDY_DATA_REG = 0x01;

    // Bit 6 in OSR_CONFIG enables pressure measurement
    private static final int PRESS_EN = 0x40;

    // Temperature: 1 LSB = 1/2^16 °C
    private static final float TEMP_CONVERSION_FACTOR = 65536.0f;

    // Pressure: 1 LSB = 1/2^6 Pa = 1/64 Pa
    private static final float PRESS_CONVERSION_FACTOR = 64.0f;
    private static final int PRESSURE_SEA_LEVEL = 101325;
    private static final double POW_FACT = 1.0 / 5.225;

    // Maximum wait time for data ready (ms)
    private static final int MAX_WAIT_TIME_MS = 100;

    /**
     * Power modes for the BMP581 sensor.
     */
    public enum PowerMode {
        /**
         * Standby mode - low power, retains last measurement.
         */
        STANDBY(0x00),
        /**
         * Normal mode - periodic measurements at configured ODR.
         */
        NORMAL(0x01),
        /**
         * Forced mode - single-shot measurement, returns to standby.
         */
        FORCED(0x02),
        /**
         * Continuous mode - maximum frequency measurements.
         */
        CONTINUOUS(0x03);

        private final int value;

        PowerMode(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Oversampling settings for temperature and pressure measurements.
     * Higher oversampling provides better accuracy at the cost of
     * increased conversion time and power consumption.
     */
    public enum Oversampling {
        OSR_1X(0x00),
        OSR_2X(0x01),
        OSR_4X(0x02),
        OSR_8X(0x03),
        OSR_16X(0x04),
        OSR_32X(0x05),
        OSR_64X(0x06),
        OSR_128X(0x07);

        private final int value;

        Oversampling(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private Oversampling temperatureOversampling = Oversampling.OSR_4X;
    private Oversampling pressureOversampling = Oversampling.OSR_4X;
    private PowerMode powerMode = PowerMode.NORMAL;

    /**
     * Constructs a BMP581Device using default settings (I2C BUS_1, address 0x47).
     *
     * @throws IOException if there was a communication problem
     */
    public BMP581Device() throws IOException {
        this(I2cBus.BUS_1, DEFAULT_I2C_ADDRESS);
    }

    /**
     * Constructs a BMP581Device with the specified I2C bus and address.
     *
     * @param bus     the I2C bus to use
     * @param address the I2C address of the device
     * @throws IOException if there was a communication problem
     */
    public BMP581Device(I2cBus bus, int address) throws IOException {
        super(bus, address);
        initialize();
    }

    /**
     * Initializes the sensor by verifying chip ID, performing soft reset,
     * and configuring default settings.
     */
    private void initialize() throws IOException {
        // Verify chip ID
        int chipId = getChipId();
        if (chipId != CHIP_ID_PRIMARY && chipId != CHIP_ID_SECONDARY) {
            throw new IOException("Invalid BMP581 chip ID: 0x" + Integer.toHexString(chipId)
                    + ". Expected 0x50 or 0x51");
        }
        LOGGER.info("BMP581 detected with chip ID: 0x{}", Integer.toHexString(chipId));

        // Perform soft reset
        softReset();

        // Configure default oversampling (OSR_4X for both)
        setOversampling(Oversampling.OSR_4X, Oversampling.OSR_4X);

        // Set power mode to NORMAL
        setPowerMode(PowerMode.NORMAL);
    }

    /**
     * Returns the chip ID of the sensor.
     *
     * @return the chip ID (0x50 or 0x51 for valid BMP581)
     * @throws IOException if there was a communication problem
     */
    public int getChipId() throws IOException {
        return readByte(REG_CHIP_ID);
    }

    /**
     * Performs a soft reset of the sensor, restoring default settings.
     *
     * @throws IOException if there was a communication problem
     */
    public void softReset() throws IOException {
        writeByte(REG_CMD, CMD_SOFT_RESET);
        // Wait for reset to complete (typically 2ms per datasheet)
        sleep(10);
    }

    /**
     * Sets the oversampling configuration for temperature and pressure.
     *
     * @param tempOsr  oversampling setting for temperature
     * @param pressOsr oversampling setting for pressure
     * @throws IOException if there was a communication problem
     */
    public void setOversampling(Oversampling tempOsr, Oversampling pressOsr) throws IOException {
        this.temperatureOversampling = tempOsr;
        this.pressureOversampling = pressOsr;

        // OSR config: bit 6 = press_en, bits [5:3] = press_osr, bits [2:0] = temp_osr
        int osrConfig = PRESS_EN | (pressOsr.getValue() << 3) | tempOsr.getValue();
        writeByte(REG_OSR_CONFIG, (byte) osrConfig);

        LOGGER.debug("OSR config set to: temp={}, press={}", tempOsr, pressOsr);
    }

    /**
     * Sets the power mode of the sensor.
     *
     * @param mode the power mode to set
     * @throws IOException if there was a communication problem
     */
    public void setPowerMode(PowerMode mode) throws IOException {
        this.powerMode = mode;

        // ODR config: bits [1:0] = power mode
        // Read current ODR config to preserve other settings
        int odrConfig = readByte(REG_ODR_CONFIG);
        odrConfig = (odrConfig & 0xFC) | mode.getValue();
        writeByte(REG_ODR_CONFIG, (byte) odrConfig);

        LOGGER.debug("Power mode set to: {}", mode);
    }

    /**
     * Reads the temperature in degrees Celsius.
     *
     * @return the temperature in °C
     * @throws IOException if there was a communication problem
     */
    public float readTemperature() throws IOException {
        waitForDataReady();
        int rawTemp = readTemperatureRaw();
        return rawTemp / TEMP_CONVERSION_FACTOR;
    }

    /**
     * Reads the pressure in Pascals.
     *
     * @return the pressure in Pa
     * @throws IOException if there was a communication problem
     */
    public float readPressure() throws IOException {
        waitForDataReady();
        int rawPress = readPressureRaw();
        return rawPress / PRESS_CONVERSION_FACTOR;
    }

    /**
     * Reads the barometric altitude above sea level in meters.
     * Uses the standard sea level pressure of 101325 Pa.
     *
     * @return the altitude in meters
     * @throws IOException if there was a communication problem
     */
    public float readAltitude() throws IOException {
        float pressure = readPressure();
        return (float) (44330.0 * (1.0 - Math.pow(pressure / PRESSURE_SEA_LEVEL, POW_FACT)));
    }

    /**
     * Reads the raw 24-bit temperature data from the sensor.
     * The value is sign-extended to 32 bits to handle negative temperatures.
     *
     * @return the raw temperature value (signed)
     * @throws IOException if there was a communication problem
     */
    private int readTemperatureRaw() throws IOException {
        byte[] data = new byte[3];
        readBufferByAddress(REG_TEMP_DATA_XLSB, data, 0, 3);
        // Data is LSB first: XLSB, LSB, MSB
        int raw = ((data[2] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[0] & 0xFF);
        // Sign-extend from 24-bit to 32-bit for negative temperatures
        if ((raw & 0x800000) != 0) {
            raw |= 0xFF000000;
        }
        return raw;
    }

    /**
     * Reads the raw 24-bit pressure data from the sensor.
     *
     * @return the raw pressure value
     * @throws IOException if there was a communication problem
     */
    private int readPressureRaw() throws IOException {
        byte[] data = new byte[3];
        readBufferByAddress(REG_PRESS_DATA_XLSB, data, 0, 3);
        // Data is LSB first: XLSB, LSB, MSB
        return ((data[2] & 0xFF) << 16) | ((data[1] & 0xFF) << 8) | (data[0] & 0xFF);
    }

    /**
     * Waits for the data ready status bit to be set.
     *
     * @throws IOException if there was a communication problem or timeout
     */
    private void waitForDataReady() throws IOException {
        long startTime = System.currentTimeMillis();
        while ((System.currentTimeMillis() - startTime) < MAX_WAIT_TIME_MS) {
            int status = readByte(REG_INT_STATUS);
            if ((status & DRDY_DATA_REG) != 0) {
                return;
            }
            sleep(1);
        }
        LOGGER.warn("Timeout waiting for BMP581 data ready");
    }

    /**
     * Returns the current temperature oversampling setting.
     *
     * @return the temperature oversampling setting
     */
    public Oversampling getTemperatureOversampling() {
        return temperatureOversampling;
    }

    /**
     * Returns the current pressure oversampling setting.
     *
     * @return the pressure oversampling setting
     */
    public Oversampling getPressureOversampling() {
        return pressureOversampling;
    }

    /**
     * Returns the current power mode.
     *
     * @return the power mode
     */
    public PowerMode getPowerMode() {
        return powerMode;
    }
}
