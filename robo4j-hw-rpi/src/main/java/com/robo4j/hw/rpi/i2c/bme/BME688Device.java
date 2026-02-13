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
package com.robo4j.hw.rpi.i2c.bme;

import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.utils.I2cBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Driver for the Bosch BME688 digital gas, pressure, temperature and humidity sensor.
 * <p>
 * The gas sensor can detect Volatile Organic Compounds (VOCs), volatile
 * sulfur compounds (VSCs) and other gases in the part per billion (ppb) range.
 * <p>
 * <strong>Note on Air Quality Features:</strong> For production applications requiring
 * accurate Indoor Air Quality (IAQ), CO2 equivalent, and breath VOC (bVOC) measurements,
 * Bosch recommends using their proprietary BSEC (Bosch Software Environmental Cluster)
 * library, which includes sophisticated AI algorithms trained on extensive datasets.
 * <p>
 * This driver provides simplified air quality estimation methods ({@link #readAirQuality()},
 * {@link #performGasScan(GasScanProfile...)}) that use basic algorithms to demonstrate
 * the sensor's capabilities. These simplified calculations are suitable for:
 * <ul>
 *   <li>Learning and experimentation</li>
 *   <li>Relative air quality comparisons (better/worse than baseline)</li>
 *   <li>Detecting significant air quality changes</li>
 *   <li>Applications where approximate values are acceptable</li>
 * </ul>
 * For applications requiring certified accuracy (e.g., commercial air quality monitors,
 * health-related devices), use the BSEC library.
 *
 * @author Marcus Hirt (@hirt)
 * @see <a href="https://www.bosch-sensortec.com/software-tools/software/bsec/">Bosch BSEC Library</a>
 */
public final class BME688Device extends AbstractI2CDevice {
    private static final Logger LOGGER = LoggerFactory.getLogger(BME688Device.class);

    // Default I2C address (SDO connected to GND)
    // Use 0x77 if SDO is connected to VDDIO
    private static final int DEFAULT_I2C_ADDRESS = 0x76;

    // Chip identification
    private static final int CHIP_ID_BME688 = 0x61;
    private static final int VARIANT_ID_BME688 = 0x01;

    // Register addresses
    private static final int REG_CHIP_ID = 0xD0;
    private static final int REG_VARIANT_ID = 0xF0;
    private static final int REG_RESET = 0xE0;
    private static final int REG_CTRL_HUM = 0x72;
    private static final int REG_CTRL_MEAS = 0x74;
    private static final int REG_CONFIG = 0x75;
    private static final int REG_CTRL_GAS_0 = 0x70;
    private static final int REG_CTRL_GAS_1 = 0x71;

    // Data register addresses (Field 0)
    private static final int REG_PRESS_MSB = 0x1F;
    private static final int REG_PRESS_LSB = 0x20;
    private static final int REG_PRESS_XLSB = 0x21;
    private static final int REG_TEMP_MSB = 0x22;
    private static final int REG_TEMP_LSB = 0x23;
    private static final int REG_TEMP_XLSB = 0x24;
    private static final int REG_HUM_MSB = 0x25;
    private static final int REG_HUM_LSB = 0x26;
    private static final int REG_GAS_R_MSB = 0x2C;
    private static final int REG_GAS_R_LSB = 0x2D;
    private static final int REG_MEAS_STATUS = 0x1D;

    // Heater configuration registers (10 steps: 0-9)
    private static final int REG_RES_HEAT_0 = 0x5A;  // 0x5A-0x63 for steps 0-9
    private static final int REG_GAS_WAIT_0 = 0x64;  // 0x64-0x6D for steps 0-9
    private static final int REG_IDAC_HEAT_0 = 0x50; // 0x50-0x59 for steps 0-9

    // Maximum number of heater profiles
    private static final int MAX_HEATER_PROFILES = 10;

    // Calibration data registers
    private static final int REG_RES_HEAT_RANGE = 0x02;
    private static final int REG_RES_HEAT_VAL = 0x00;

    // IAQ calculation constants
    private static final float DEFAULT_BASELINE_RESISTANCE = 250000.0f; // Typical clean air value (Ohms)
    private static final float AMBIENT_CO2_PPM = 400.0f;

    // Adaptive baseline constants
    private static final int MAX_CALIBRATION_ENTRIES = 100;
    private static final long GAS_RECAL_MILLIS = 60 * 60 * 1000L; // 1 hour
    private static final float HUMIDITY_COMPENSATION_SLOPE = 0.03f;
    private static final int BASELINE_STABILITY_THRESHOLD = 5;

    // Burn-in convergence detection: the sensor is considered stable when the max/min
    // ratio of the last BURN_IN_WINDOW compensated gas resistance readings drops below
    // BURN_IN_CONVERGENCE_RATIO and the first/second half means differ by less than
    // BURN_IN_DRIFT_RATIO. Cold start typically converges around 100-200 readings;
    // warm restart (sensor recently active) converges in ~25-40 readings.
    private static final int BURN_IN_WINDOW = 30;
    private static final float BURN_IN_CONVERGENCE_RATIO = 1.05f;
    private static final float BURN_IN_DRIFT_RATIO = 1.02f;
    private static final int BURN_IN_MAX_READINGS = 300;

    // Commands
    private static final byte SOFT_RESET_CMD = (byte) 0xB6;

    // Mode values
    private static final int MODE_SLEEP = 0x00;
    private static final int MODE_FORCED = 0x01;

    // Pressure at sea level in Pascal
    private static final int PRESSURE_SEA_LEVEL = 101325;
    private static final double POW_FACT = 1.0 / 5.225;

    private final Oversampling temperatureOversampling;
    private final Oversampling pressureOversampling;
    private final Oversampling humidityOversampling;
    private final IIRFilter filter;

    // Calibration parameters for temperature
    private int parT1;
    private int parT2;
    private int parT3;

    // Calibration parameters for pressure
    private int parP1;
    private int parP2;
    private int parP3;
    private int parP4;
    private int parP5;
    private int parP6;
    private int parP7;
    private int parP8;
    private int parP9;
    private int parP10;

    // Calibration parameters for humidity
    private int parH1;
    private int parH2;
    private int parH3;
    private int parH4;
    private int parH5;
    private int parH6;
    private int parH7;

    // Calibration parameters for gas
    private int parG1;
    private int parG2;
    private int parG3;
    private int resHeatRange;
    private int resHeatVal;

    // Temperature fine value for compensation
    private double tFine;

    // Last measured ambient temperature (C), used for heater resistance calculation
    private int lastAmbientTemp = 25;

    // Baseline gas resistance for IAQ calculation (can be calibrated or auto-adapted)
    private float baselineGasResistance = DEFAULT_BASELINE_RESISTANCE;
    private boolean baselineCalibrated = false;

    // Adaptive baseline state
    private final java.util.ArrayList<Float> gasCalibrationData = new java.util.ArrayList<>();
    private int readingCount = 0;
    private long lastRecalTimeMs = 0;
    private boolean burnInComplete = false;
    private final float[] burnInWindow = new float[BURN_IN_WINDOW];
    private int baselineStabilityCount = 0;

    /**
     * Oversampling settings for temperature, pressure and humidity measurements.
     */
    public enum Oversampling {
        SKIP(0, 0),
        X1(1, 1),
        X2(2, 2),
        X4(3, 4),
        X8(4, 8),
        X16(5, 16);

        private final int regValue;
        private final int samples;

        Oversampling(int regValue, int samples) {
            this.regValue = regValue;
            this.samples = samples;
        }

        public int getRegValue() {
            return regValue;
        }

        public int getSamples() {
            return samples;
        }
    }

    /**
     * IIR filter coefficient settings for temperature and pressure.
     */
    public enum IIRFilter {
        OFF(0, 0),
        COEFF_1(1, 1),
        COEFF_3(2, 3),
        COEFF_7(3, 7),
        COEFF_15(4, 15),
        COEFF_31(5, 31),
        COEFF_63(6, 63),
        COEFF_127(7, 127);

        private final int regValue;
        private final int coefficient;

        IIRFilter(int regValue, int coefficient) {
            this.regValue = regValue;
            this.coefficient = coefficient;
        }

        public int getRegValue() {
            return regValue;
        }

        public int getCoefficient() {
            return coefficient;
        }
    }

    /**
     * Indoor Air Quality (IAQ) level classification based on the IAQ index.
     * Based on standard air quality index scales.
     */
    public enum IAQLevel {
        EXCELLENT(0, 50, "Excellent"),
        GOOD(51, 100, "Good"),
        LIGHTLY_POLLUTED(101, 150, "Lightly Polluted"),
        MODERATELY_POLLUTED(151, 200, "Moderately Polluted"),
        HEAVILY_POLLUTED(201, 250, "Heavily Polluted"),
        SEVERELY_POLLUTED(251, 350, "Severely Polluted"),
        EXTREMELY_POLLUTED(351, 500, "Extremely Polluted");

        private final int minValue;
        private final int maxValue;
        private final String description;

        IAQLevel(int minValue, int maxValue, String description) {
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.description = description;
        }

        public int getMinValue() {
            return minValue;
        }

        public int getMaxValue() {
            return maxValue;
        }

        public String getDescription() {
            return description;
        }

        /**
         * Returns the IAQ level for a given IAQ index value.
         *
         * @param iaq the IAQ index (0-500)
         * @return the corresponding IAQ level
         */
        public static IAQLevel fromIAQ(float iaq) {
            int iaqInt = Math.round(iaq);
            for (IAQLevel level : values()) {
                if (iaqInt >= level.minValue && iaqInt <= level.maxValue) {
                    return level;
                }
            }
            return iaqInt < 0 ? EXCELLENT : EXTREMELY_POLLUTED;
        }
    }

    /**
     * Configuration for a gas sensor heater profile step.
     *
     * @param temperatureC target heater temperature in degrees Celsius (200-400)
     * @param durationMs   heating duration in milliseconds (1-4032)
     */
    public record GasScanProfile(int temperatureC, int durationMs) {
        public GasScanProfile {
            if (temperatureC < 200 || temperatureC > 400) {
                throw new IllegalArgumentException("Heater temperature must be between 200 and 400 C");
            }
            if (durationMs < 1 || durationMs > 4032) {
                throw new IllegalArgumentException("Heater duration must be between 1 and 4032 ms");
            }
        }

        /**
         * Standard profile for general air quality monitoring.
         */
        public static final GasScanProfile STANDARD = new GasScanProfile(320, 150);

        /**
         * Low temperature profile for alcohol detection.
         */
        public static final GasScanProfile LOW_TEMP = new GasScanProfile(200, 100);

        /**
         * Medium temperature profile for VOC detection.
         */
        public static final GasScanProfile MEDIUM_TEMP = new GasScanProfile(300, 100);

        /**
         * High temperature profile for combustible gas detection.
         */
        public static final GasScanProfile HIGH_TEMP = new GasScanProfile(400, 100);
    }

    /**
     * Constructs a BME688Device using the default settings.
     * (I2C BUS 1, address 0x77, 2x oversampling for T/P/H, IIR filter off)
     *
     * @throws IOException if there was communication problem
     */
    public BME688Device() throws IOException {
        this(I2cBus.BUS_1, DEFAULT_I2C_ADDRESS, Oversampling.X2, Oversampling.X2, Oversampling.X2, IIRFilter.OFF);
    }

    /**
     * Constructs a BME688Device with specified oversampling settings.
     *
     * @param temperatureOversampling the temperature oversampling setting
     * @param pressureOversampling    the pressure oversampling setting
     * @param humidityOversampling    the humidity oversampling setting
     * @throws IOException if there was communication problem
     */
    public BME688Device(Oversampling temperatureOversampling, Oversampling pressureOversampling,
                        Oversampling humidityOversampling) throws IOException {
        this(I2cBus.BUS_1, DEFAULT_I2C_ADDRESS, temperatureOversampling, pressureOversampling,
                humidityOversampling, IIRFilter.OFF);
    }

    /**
     * Creates a software interface to a BME688 sensor.
     *
     * @param bus                     the I2C bus to use
     * @param address                 the I2C address (0x76 or 0x77)
     * @param temperatureOversampling the temperature oversampling setting
     * @param pressureOversampling    the pressure oversampling setting
     * @param humidityOversampling    the humidity oversampling setting
     * @param filter                  the IIR filter setting
     * @throws IOException if there was communication problem
     */
    public BME688Device(I2cBus bus, int address, Oversampling temperatureOversampling,
                        Oversampling pressureOversampling, Oversampling humidityOversampling,
                        IIRFilter filter) throws IOException {
        super(bus, address);
        this.temperatureOversampling = temperatureOversampling;
        this.pressureOversampling = pressureOversampling;
        this.humidityOversampling = humidityOversampling;
        this.filter = filter;

        verifyChipId();
        readCalibrationData();
        initializeSensor();
    }

    private void verifyChipId() throws IOException {
        int chipId = readByte(REG_CHIP_ID);
        if (chipId != CHIP_ID_BME688) {
            throw new IOException("Invalid chip ID. Expected 0x" + Integer.toHexString(CHIP_ID_BME688)
                    + " but got 0x" + Integer.toHexString(chipId));
        }
        int variantId = readByte(REG_VARIANT_ID);
        LOGGER.debug("BME688 detected. Chip ID: 0x{}, Variant ID: 0x{}",
                Integer.toHexString(chipId), Integer.toHexString(variantId));
    }

    private void initializeSensor() throws IOException {
        // Set humidity oversampling (must be set first)
        writeByte(REG_CTRL_HUM, (byte) humidityOversampling.getRegValue());

        // Set IIR filter
        writeByte(REG_CONFIG, (byte) (filter.getRegValue() << 2));

        // Set temperature and pressure oversampling, keep in sleep mode
        int ctrlMeas = (temperatureOversampling.getRegValue() << 5)
                | (pressureOversampling.getRegValue() << 2)
                | MODE_SLEEP;
        writeByte(REG_CTRL_MEAS, (byte) ctrlMeas);
    }

    /**
     * Performs a soft reset of the sensor.
     *
     * @throws IOException if there was communication problem
     */
    public void softReset() throws IOException {
        writeByte(REG_RESET, SOFT_RESET_CMD);
        sleep(10);
        readCalibrationData();
        initializeSensor();
    }

    /**
     * Runs a hardware self-test of the BME688 sensor. Verifies chip identity, environmental
     * readings, gas heater operation, and gas sensor response to different heater temperatures.
     * <p>
     * The test checks:
     * <ol>
     *   <li>Chip ID is 0x61 (BME688)</li>
     *   <li>Temperature is in [0, 60] C</li>
     *   <li>Pressure is in [900, 1100] hPa</li>
     *   <li>Humidity is in [20, 80] %RH</li>
     *   <li>Gas measurement is valid and heater is stable</li>
     *   <li>Gas resistance ratio between 150 C and 350 C heater is at least 2.4
     *       (sensor responds correctly to heater temperature changes)</li>
     * </ol>
     * <p>
     * This method does not affect the adaptive baseline or air quality state.
     *
     * @return true if all self-test checks pass
     * @throws IOException if there was a communication problem
     */
    public boolean selfTest() throws IOException {
        LOGGER.info("Starting BME688 self-test...");

        // Step 1: Verify chip identity
        int chipId = readByte(REG_CHIP_ID);
        if (chipId != CHIP_ID_BME688) {
            LOGGER.warn("Self-test FAILED: wrong chip ID 0x{}", Integer.toHexString(chipId));
            return false;
        }

        // Step 2: Take a measurement at high heater temperature (350C, 1000ms)
        // and verify environmental readings are in plausible ranges
        SensorData initial = readAllWithGas(350, 1000);

        float temp = initial.getTemperature();
        float pressHPa = initial.getPressureHPa();
        float hum = initial.getHumidity();

        if (temp < 0 || temp > 60) {
            LOGGER.warn("Self-test FAILED: temperature {} C out of range [0, 60]", temp);
            return false;
        }
        if (pressHPa < 900 || pressHPa > 1100) {
            LOGGER.warn("Self-test FAILED: pressure {} hPa out of range [900, 1100]", pressHPa);
            return false;
        }
        if (hum < 20 || hum > 80) {
            LOGGER.warn("Self-test FAILED: humidity {}% out of range [20, 80]", hum);
            return false;
        }

        if (!initial.isGasValid()) {
            LOGGER.warn("Self-test FAILED: initial gas measurement invalid");
            return false;
        }
        if (!isHeaterStable()) {
            LOGGER.warn("Self-test FAILED: heater not stable");
            return false;
        }

        // Step 3: Alternating high (350C) and low (150C) heater measurements.
        // 6 measurements: HIGH, LOW, HIGH, LOW, HIGH, LOW
        float[] resistances = new float[6];
        for (int i = 0; i < 6; i++) {
            int heaterTemp = (i % 2 == 0) ? 350 : 150;
            SensorData measurement = readAllWithGas(heaterTemp, 2000);

            if (!measurement.isGasValid()) {
                LOGGER.warn("Self-test FAILED: gas measurement {} invalid (heater {}C)", i, heaterTemp);
                return false;
            }
            resistances[i] = measurement.getGasResistance();
        }

        // Step 4: Verify gas resistance ratio (low temp / high temp >= 2.4).
        // Uses last two low-temp (indices 3, 5) and last high-temp (index 4).
        float avgLow = (resistances[3] + resistances[5]) / 2.0f;
        float lastHigh = resistances[4];

        if (lastHigh <= 0) {
            LOGGER.warn("Self-test FAILED: invalid high-temperature gas resistance");
            return false;
        }

        float ratio = avgLow / lastHigh;
        if (ratio < 2.4f) {
            LOGGER.warn("Self-test FAILED: gas resistance ratio {} < 2.4 (low={}, high={})",
                    ratio, avgLow, lastHigh);
            return false;
        }

        LOGGER.info("Self-test PASSED: T={} C, P={} hPa, H={}%, gas ratio={}",
                String.format("%.1f", temp), String.format("%.0f", pressHPa),
                String.format("%.1f", hum), String.format("%.1f", ratio));
        return true;
    }

    /**
     * Reads the temperature in degrees Celsius.
     *
     * @return the temperature in degrees Celsius
     * @throws IOException if there was communication problem
     */
    public float readTemperature() throws IOException {
        triggerForcedMeasurement(false);
        waitForMeasurement(calculateMeasurementTime(false, 0));
        int tempAdc = readRawTemperature();
        return compensateTemperature(tempAdc);
    }

    /**
     * Reads the pressure in Pascal.
     *
     * @return the pressure in Pascal
     * @throws IOException if there was communication problem
     */
    public float readPressure() throws IOException {
        triggerForcedMeasurement(false);
        waitForMeasurement(calculateMeasurementTime(false, 0));
        // Temperature must be read first to calculate t_fine
        int tempAdc = readRawTemperature();
        compensateTemperature(tempAdc);
        int pressAdc = readRawPressure();
        return compensatePressure(pressAdc);
    }

    /**
     * Reads the humidity in percent relative humidity.
     *
     * @return the humidity in percent
     * @throws IOException if there was communication problem
     */
    public float readHumidity() throws IOException {
        triggerForcedMeasurement(false);
        waitForMeasurement(calculateMeasurementTime(false, 0));
        // Temperature must be read first to calculate t_fine
        int tempAdc = readRawTemperature();
        compensateTemperature(tempAdc);
        int humAdc = readRawHumidity();
        return compensateHumidity(humAdc);
    }

    /**
     * Reads the gas resistance in Ohms.
     * Note: Gas sensor must be configured with heater settings before calling this method.
     *
     * @param heaterTemperature the target heater temperature in degrees Celsius (200-400)
     * @param heaterDuration    the heater duration in milliseconds (1-4032)
     * @return the gas resistance in Ohms, or -1 if measurement was invalid
     * @throws IOException if there was communication problem
     */
    public float readGasResistance(int heaterTemperature, int heaterDuration) throws IOException {
        configureGasSensor(heaterTemperature, heaterDuration);
        triggerForcedMeasurement(true);
        waitForMeasurement(calculateMeasurementTime(true, heaterDuration));
        return readAndCompensateGasResistance();
    }

    /**
     * Reads all sensor data at once.
     *
     * @return a SensorData object containing temperature, pressure, humidity and optionally gas resistance
     * @throws IOException if there was communication problem
     */
    public SensorData readAll() throws IOException {
        triggerForcedMeasurement(false);
        waitForMeasurement(calculateMeasurementTime(false, 0));

        int tempAdc = readRawTemperature();
        float temperature = compensateTemperature(tempAdc);
        int pressAdc = readRawPressure();
        float pressure = compensatePressure(pressAdc);
        int humAdc = readRawHumidity();
        float humidity = compensateHumidity(humAdc);

        return new SensorData(temperature, pressure, humidity, Float.NaN, false);
    }

    /**
     * Reads all sensor data including gas resistance.
     *
     * @param heaterTemperature the target heater temperature in degrees Celsius (200-400)
     * @param heaterDuration    the heater duration in milliseconds (1-4032)
     * @return a SensorData object containing temperature, pressure, humidity and gas resistance
     * @throws IOException if there was communication problem
     */
    public SensorData readAllWithGas(int heaterTemperature, int heaterDuration) throws IOException {
        configureGasSensor(heaterTemperature, heaterDuration);
        triggerForcedMeasurement(true);
        waitForMeasurement(calculateMeasurementTime(true, heaterDuration));

        int tempAdc = readRawTemperature();
        float temperature = compensateTemperature(tempAdc);
        int pressAdc = readRawPressure();
        float pressure = compensatePressure(pressAdc);
        int humAdc = readRawHumidity();
        float humidity = compensateHumidity(humAdc);

        float gasResistance = readAndCompensateGasResistance();
        boolean gasValid = isGasValid();

        return new SensorData(temperature, pressure, humidity, gasResistance, gasValid);
    }

    /**
     * Calculates the barometric altitude above sea level in meters.
     *
     * @return the altitude in meters
     * @throws IOException if there was communication problem
     */
    public float readAltitude() throws IOException {
        float pressure = readPressure();
        return (float) (44330.0 * (1.0 - Math.pow(pressure / PRESSURE_SEA_LEVEL, POW_FACT)));
    }

    /**
     * Explicitly calibrates the baseline gas resistance for IAQ calculation.
     * This should be called in a clean air environment (e.g., outdoors or well-ventilated room).
     * Takes multiple readings and averages them for a stable baseline.
     * <p>
     * <strong>Note:</strong> Calling this method disables the automatic adaptive baseline.
     * The driver will use the calibrated value until {@link #enableAdaptiveBaseline()} is called.
     * If you don't call this method, the driver automatically adapts the baseline by tracking
     * the highest compensated gas resistance observed in a sliding window.
     *
     * @param numReadings the number of readings to average (recommended: 10-50)
     * @throws IOException if there was communication problem
     */
    public void calibrateBaseline(int numReadings) throws IOException {
        if (numReadings < 1) {
            throw new IllegalArgumentException("Number of readings must be at least 1");
        }

        LOGGER.info("Calibrating IAQ baseline with {} readings...", numReadings);
        float sum = 0;
        int validReadings = 0;

        for (int i = 0; i < numReadings; i++) {
            SensorData data = readAllWithGas(320, 150);
            if (data.isGasValid() && data.getGasResistance() > 0) {
                // Store compensated resistance so it's comparable with adaptive baseline values
                sum += compensateGasResistance(data.getGasResistance(),
                        data.getTemperature(), data.getHumidity());
                validReadings++;
            }
            if (i < numReadings - 1) {
                sleep(3000);
            }
        }

        if (validReadings > 0) {
            baselineGasResistance = sum / validReadings;
            baselineCalibrated = true;
            LOGGER.info("IAQ baseline calibrated: {} Ohms compensated ({} valid readings)",
                    baselineGasResistance, validReadings);
        } else {
            LOGGER.warn("Failed to calibrate baseline: no valid readings");
        }
    }

    /**
     * Sets the baseline gas resistance for IAQ calculation manually.
     * Use this if you have a known baseline value from previous calibration.
     * <p>
     * <strong>Note:</strong> This disables the adaptive baseline. Call
     * {@link #enableAdaptiveBaseline()} to re-enable it.
     *
     * @param resistance the baseline gas resistance in Ohms
     */
    public void setBaselineResistance(float resistance) {
        if (resistance <= 0) {
            throw new IllegalArgumentException("Baseline resistance must be positive");
        }
        this.baselineGasResistance = resistance;
        this.baselineCalibrated = true;
        LOGGER.debug("IAQ baseline set to {} Ohms", resistance);
    }

    /**
     * Gets the current baseline gas resistance used for IAQ calculation.
     * This is a humidity-compensated value. You can store this and restore it later
     * via {@link #setBaselineResistance(float)} to avoid re-calibration.
     *
     * @return the compensated baseline resistance in Ohms
     */
    public float getBaselineResistance() {
        return baselineGasResistance;
    }

    /**
     * Returns whether the baseline has been explicitly calibrated (disabling adaptive mode).
     *
     * @return true if calibrateBaseline() or setBaselineResistance() has been called
     */
    public boolean isBaselineCalibrated() {
        return baselineCalibrated;
    }

    /**
     * Re-enables the adaptive baseline after it was disabled by an explicit calibration.
     * The adaptive baseline tracks the gas resistance ceiling — the mean of compensated
     * gas resistance values that exceeded the previous ceiling, representing the cleanest
     * air observed. A periodic recalibration compensates for negative sensor drift.
     */
    public void enableAdaptiveBaseline() {
        this.baselineCalibrated = false;
        LOGGER.debug("Adaptive baseline re-enabled");
    }

    /**
     * Returns the number of gas resistance readings taken so far.
     *
     * @return the total number of readings taken
     */
    public int getReadingCount() {
        return readingCount;
    }

    /**
     * Returns whether the sensor burn-in period is complete.
     * The gas sensor's MOX hot plate needs heating cycles after a cold start to reach
     * thermal equilibrium. Burn-in is detected automatically when gas resistance readings
     * converge (stabilize within 5% over the last {@value #BURN_IN_WINDOW} readings)
     * and the first/second half means show no significant drift (within 2%).
     * Cold start typically requires ~100 readings; warm restart ~25-40 readings.
     *
     * @return true if gas resistance readings have converged
     */
    public boolean isBurnInComplete() {
        return burnInComplete;
    }

    /**
     * Reads all sensor data and calculates air quality metrics using simplified algorithms.
     * Uses the standard heater profile (320C, 150ms).
     * <p>
     * <strong>Important:</strong> This method uses simplified algorithms for IAQ, CO2 equivalent,
     * and bVOC estimation. For accurate air quality measurements in production applications,
     * use the Bosch BSEC library instead. The values returned here are approximations suitable
     * for demonstration, relative comparisons, and detecting significant air quality changes.
     *
     * @return air quality data including IAQ index, CO2 equivalent, and bVOC estimate
     * @throws IOException if there was communication problem
     * @see #calibrateBaseline(int) for improving accuracy by establishing a clean-air reference
     */
    public AirQualityData readAirQuality() throws IOException {
        return readAirQuality(GasScanProfile.STANDARD);
    }

    /**
     * Reads all sensor data and calculates air quality metrics using a specified heater profile.
     * <p>
     * <strong>Important:</strong> This method uses simplified algorithms. For accurate air quality
     * measurements in production applications, use the Bosch BSEC library instead.
     *
     * @param profile the gas heater profile to use
     * @return air quality data including IAQ index, CO2 equivalent, and bVOC estimate
     * @throws IOException if there was communication problem
     * @see #readAirQuality()
     */
    public AirQualityData readAirQuality(GasScanProfile profile) throws IOException {
        SensorData sensorData = readAllWithGas(profile.temperatureC(), profile.durationMs());
        return calculateAirQuality(sensorData);
    }

    /**
     * Performs a gas scan using multiple heater profiles for better VOC characterization.
     * Different heater temperatures provide selectivity for different gas types.
     * <p>
     * <strong>Note:</strong> The air quality metrics in the result use simplified algorithms.
     * The raw resistance values at different temperatures can be used for custom analysis
     * or pattern recognition. For production gas scan analysis, consider the Bosch BSEC library
     * which provides trained AI models for gas classification.
     *
     * @param profiles the heater profiles to use (1-10 profiles)
     * @return gas scan results containing resistance values at each temperature
     * @throws IOException if there was communication problem
     */
    public GasScanResult performGasScan(GasScanProfile... profiles) throws IOException {
        if (profiles == null || profiles.length == 0) {
            throw new IllegalArgumentException("At least one profile is required");
        }
        if (profiles.length > MAX_HEATER_PROFILES) {
            throw new IllegalArgumentException("Maximum " + MAX_HEATER_PROFILES + " profiles supported");
        }

        // Read environmental data first (without gas)
        SensorData envData = readAll();
        float temperature = envData.getTemperature();
        float humidity = envData.getHumidity();
        float pressure = envData.getPressure();

        // Perform gas measurements at each heater profile
        float[] resistances = new float[profiles.length];
        boolean[] valid = new boolean[profiles.length];

        for (int i = 0; i < profiles.length; i++) {
            GasScanProfile profile = profiles[i];
            configureGasSensorStep(i, profile.temperatureC(), profile.durationMs(), (int) temperature);

            // Select this heater step and trigger measurement
            int ctrlGas1 = (1 << 5) | i; // run_gas = 1, nb_conv = i
            writeByte(REG_CTRL_GAS_1, (byte) ctrlGas1);

            triggerForcedMeasurement(true);
            waitForMeasurement(calculateMeasurementTime(true, profile.durationMs()));

            resistances[i] = readAndCompensateGasResistance();
            valid[i] = isGasValid();
        }

        // Calculate air quality using the first (primary) profile.
        // Skip adaptive baseline update since gas scan uses non-standard heater temperatures.
        float primaryResistance = valid[0] ? resistances[0] : Float.NaN;
        AirQualityData airQuality = calculateAirQualityFromResistance(
                primaryResistance, temperature, humidity, pressure, valid[0], false);

        return new GasScanResult(profiles, resistances, valid, airQuality);
    }

    /**
     * Performs a standard 3-temperature gas scan (low, medium, high).
     * This provides good selectivity for common VOC types.
     *
     * @return gas scan results with measurements at 200C, 300C, and 400C
     * @throws IOException if there was communication problem
     */
    public GasScanResult performStandardGasScan() throws IOException {
        return performGasScan(
                GasScanProfile.LOW_TEMP,
                GasScanProfile.MEDIUM_TEMP,
                GasScanProfile.HIGH_TEMP
        );
    }

    private void configureGasSensorStep(int step, int heaterTemperature, int heaterDuration,
                                        int ambientTemp) throws IOException {
        if (step < 0 || step >= MAX_HEATER_PROFILES) {
            throw new IllegalArgumentException("Step must be 0-9");
        }

        // Calculate heater resistance value for this step
        int resHeat = calculateHeaterResistance(heaterTemperature, ambientTemp);
        writeByte(REG_RES_HEAT_0 + step, (byte) resHeat);

        // Calculate and set gas wait time for this step
        int gasWait = calculateGasWait(heaterDuration);
        writeByte(REG_GAS_WAIT_0 + step, (byte) gasWait);
    }

    private AirQualityData calculateAirQuality(SensorData sensorData) {
        return calculateAirQualityFromResistance(
                sensorData.getGasResistance(),
                sensorData.getTemperature(),
                sensorData.getHumidity(),
                sensorData.getPressure(),
                sensorData.isGasValid(),
                true
        );
    }

    private AirQualityData calculateAirQualityFromResistance(float gasResistance, float temperature,
                                                              float humidity, float pressure,
                                                              boolean gasValid,
                                                              boolean updateBaseline) {
        if (!gasValid || Float.isNaN(gasResistance) || gasResistance <= 0) {
            return new AirQualityData(
                    temperature, pressure, humidity, Float.NaN,
                    Float.NaN, Float.NaN, Float.NaN, IAQLevel.EXCELLENT, false
            );
        }

        // Apply environmental compensation to gas resistance
        float compensatedResistance = compensateGasResistance(gasResistance, temperature, humidity);

        // Update adaptive baseline from standard heater profile readings only
        if (updateBaseline) {
            updateAdaptiveBaseline(compensatedResistance);
        }

        // Calculate IAQ index (0-500 scale)
        float iaq = calculateIAQ(compensatedResistance);

        // Estimate CO2 equivalent (ppm)
        float co2Equivalent = calculateCO2Equivalent(iaq);

        // Estimate breath VOC (ppm)
        float bvoc = calculateBVOC(compensatedResistance);

        IAQLevel level = IAQLevel.fromIAQ(iaq);

        return new AirQualityData(
                temperature, pressure, humidity, gasResistance,
                iaq, co2Equivalent, bvoc, level, true
        );
    }

    /**
     * Updates the adaptive baseline. During burn-in, tracks the highest compensated resistance.
     * After burn-in, maintains a calibration list of readings that exceeded the ceiling
     * (requiring sustained stability), with periodic recalibration for negative sensor drift.
     * Skipped if the user has explicitly calibrated the baseline.
     */
    private void updateAdaptiveBaseline(float compensatedResistance) {
        readingCount++;

        // Fill convergence window for burn-in detection
        burnInWindow[(readingCount - 1) % BURN_IN_WINDOW] = compensatedResistance;

        // Track burn-in independently of baseline mode (it's a hardware property)
        if (!burnInComplete && readingCount >= BURN_IN_WINDOW) {
            if (readingCount >= BURN_IN_MAX_READINGS || isBurnInConverged()) {
                burnInComplete = true;
                LOGGER.info("Burn-in complete after {} readings. Baseline: {} Ohms",
                        readingCount, baselineGasResistance);
            }
        }

        if (baselineCalibrated) {
            return;
        }

        if (!burnInComplete) {
            // During burn-in: track only the single highest value
            if (compensatedResistance > baselineGasResistance || gasCalibrationData.isEmpty()) {
                gasCalibrationData.clear();
                gasCalibrationData.add(compensatedResistance);
                baselineGasResistance = compensatedResistance;
            }
        } else {
            // After burn-in: add readings exceeding ceiling after sustained stability
            if (compensatedResistance > baselineGasResistance) {
                baselineStabilityCount++;
                if (baselineStabilityCount > BASELINE_STABILITY_THRESHOLD) {
                    gasCalibrationData.add(compensatedResistance);
                    if (gasCalibrationData.size() > MAX_CALIBRATION_ENTRIES) {
                        gasCalibrationData.removeFirst();
                    }
                    baselineGasResistance = meanOfCalibrationData();
                }
            } else {
                baselineStabilityCount = 0;
            }

            // Periodic recalibration to compensate for negative sensor drift
            long now = System.currentTimeMillis();
            if (lastRecalTimeMs == 0) {
                lastRecalTimeMs = now;
            }
            if ((now - lastRecalTimeMs) >= GAS_RECAL_MILLIS && !gasCalibrationData.isEmpty()) {
                lastRecalTimeMs = now;
                gasCalibrationData.add(compensatedResistance);
                gasCalibrationData.removeFirst();
                baselineGasResistance = meanOfCalibrationData();
                LOGGER.debug("Baseline recalibrated: {} Ohms ({} entries)",
                        baselineGasResistance, gasCalibrationData.size());
            }
        }
    }

    /**
     * Checks if the burn-in window shows converged gas resistance readings.
     * Returns true when both conditions are met:
     * <ol>
     *   <li>The max/min ratio of the last {@value #BURN_IN_WINDOW} readings
     *       is below {@value #BURN_IN_CONVERGENCE_RATIO} (local variance check).</li>
     *   <li>The mean of the first half and second half of the window differ by less
     *       than {@value #BURN_IN_DRIFT_RATIO} (monotonic drift check).</li>
     * </ol>
     */
    private boolean isBurnInConverged() {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        int half = BURN_IN_WINDOW / 2;
        float firstHalfSum = 0;
        float secondHalfSum = 0;
        for (int i = 0; i < BURN_IN_WINDOW; i++) {
            float v = burnInWindow[i];
            if (v < min) min = v;
            if (v > max) max = v;
            if (i < half) {
                firstHalfSum += v;
            } else {
                secondHalfSum += v;
            }
        }
        if (min <= 0 || (max / min) >= BURN_IN_CONVERGENCE_RATIO) {
            return false;
        }
        float firstHalfMean = firstHalfSum / half;
        float secondHalfMean = secondHalfSum / (BURN_IN_WINDOW - half);
        float driftRatio = firstHalfMean > secondHalfMean
                ? firstHalfMean / secondHalfMean
                : secondHalfMean / firstHalfMean;
        return driftRatio < BURN_IN_DRIFT_RATIO;
    }

    private float meanOfCalibrationData() {
        float sum = 0;
        for (float value : gasCalibrationData) {
            sum += value;
        }
        return sum / gasCalibrationData.size();
    }

    /**
     * Compensates gas resistance for humidity effects. Higher absolute humidity lowers raw
     * gas resistance, so we apply an exponential correction factor to normalize readings.
     */
    private float compensateGasResistance(float gasResistance, float temperature, float humidity) {
        float absHumidity = calculateAbsoluteHumidity(temperature, humidity);
        return gasResistance * (float) Math.exp(HUMIDITY_COMPENSATION_SLOPE * absHumidity);
    }

    /**
     * Calculates absolute humidity in g/m³ using the Magnus equation.
     */
    private static float calculateAbsoluteHumidity(float temperature, float relativeHumidity) {
        double satVaporPressure = 6.112 * 100.0 * Math.exp((17.62 * temperature) / (243.12 + temperature));
        double rhoMax = satVaporPressure / (461.52 * (temperature + 273.15));
        return (float) (relativeHumidity * rhoMax / 100.0 * 1000.0); // g/m³
    }

    /**
     * Calculates IAQ index (0-500) from compensated gas resistance using a squared-ratio
     * approach. The ratio is squared for better sensitivity in the clean-air range.
     */
    private float calculateIAQ(float compensatedResistance) {
        float ratio = Math.min(compensatedResistance / baselineGasResistance, 1.0f);
        float score = ratio * ratio;
        return (1.0f - score) * 500.0f;
    }

    /**
     * Estimates CO2 equivalent concentration from IAQ using piecewise linear interpolation.
     */
    private float calculateCO2Equivalent(float iaq) {
        if (iaq <= 50) {
            return AMBIENT_CO2_PPM + iaq * 4.0f;
        } else if (iaq <= 100) {
            return 600 + (iaq - 50) * 8.0f;
        } else if (iaq <= 200) {
            return 1000 + (iaq - 100) * 10.0f;
        } else if (iaq <= 350) {
            return 2000 + (iaq - 200) * 20.0f;
        } else {
            return 5000 + (iaq - 350) * 33.3f;
        }
    }

    /**
     * Estimates breath VOC concentration (ppm) from compensated gas resistance.
     */
    private float calculateBVOC(float compensatedResistance) {
        float ratio = Math.min(compensatedResistance / baselineGasResistance, 1.0f);
        float bvoc = 0.5f + (1.0f - ratio) * 24.5f;
        return Math.max(0, Math.min(25, bvoc));
    }

    private void configureGasSensor(int heaterTemperature, int heaterDuration) throws IOException {
        int resHeat = calculateHeaterResistance(heaterTemperature, lastAmbientTemp);
        writeByte(REG_RES_HEAT_0, (byte) resHeat);

        // Calculate gas wait time
        int gasWait = calculateGasWait(heaterDuration);
        writeByte(REG_GAS_WAIT_0, (byte) gasWait);

        // Enable gas measurement and select heater step 0
        int ctrlGas1 = (1 << 5) | 0; // run_gas = 1, nb_conv = 0
        writeByte(REG_CTRL_GAS_1, (byte) ctrlGas1);
    }

    private int calculateHeaterResistance(int targetTemp, int ambTemp) {
        if (targetTemp > 400) {
            targetTemp = 400;
        }
        double var1 = (parG1 / 16.0) + 49.0;
        double var2 = ((parG2 / 32768.0) * 0.0005) + 0.00235;
        double var3 = parG3 / 1024.0;
        double var4 = var1 * (1.0 + (var2 * targetTemp));
        double var5 = var4 + (var3 * ambTemp);
        return (int) (3.4 * ((var5 * (4.0 / (4.0 + resHeatRange)) * (1.0 / (1.0 + (resHeatVal * 0.002)))) - 25));
    }

    private int calculateGasWait(int durationMs) {
        // bits [7:6] = multiplication factor, bits [5:0] = base value
        int factor = 0;
        int value = durationMs;

        while (value > 63) {
            factor++;
            value /= 4;
            if (factor >= 3) {
                value = Math.min(value, 63);
                break;
            }
        }
        return (factor << 6) | (value & 0x3F);
    }

    private void triggerForcedMeasurement(boolean withGas) throws IOException {
        ensureSleepMode();

        if (!withGas) {
            // Disable gas measurement
            writeByte(REG_CTRL_GAS_1, (byte) 0);
        }

        // Set humidity oversampling (must be written before ctrl_meas per datasheet)
        writeByte(REG_CTRL_HUM, (byte) humidityOversampling.getRegValue());

        // Trigger forced mode measurement by writing temp/press oversampling and mode
        int ctrlMeas = (temperatureOversampling.getRegValue() << 5)
                | (pressureOversampling.getRegValue() << 2)
                | MODE_FORCED;
        writeByte(REG_CTRL_MEAS, (byte) ctrlMeas);
    }

    /**
     * Ensures the sensor is in sleep mode before configuration or mode changes.
     */
    private void ensureSleepMode() throws IOException {
        int ctrlMeas;
        int attempts = 10;
        do {
            ctrlMeas = readByte(REG_CTRL_MEAS);
            if ((ctrlMeas & 0x03) != MODE_SLEEP) {
                writeByte(REG_CTRL_MEAS, (byte) (ctrlMeas & ~0x03));
                sleep(10);
            }
            attempts--;
        } while ((ctrlMeas & 0x03) != MODE_SLEEP && attempts > 0);
    }

    /**
     * Waits for the measurement to complete by polling the new_data bit (bit 7 of meas_status).
     */
    private void waitForMeasurement(int expectedTimeMs) throws IOException {
        if (expectedTimeMs > 0) {
            sleep(expectedTimeMs);
        }

        int status;
        int maxAttempts = 50;
        do {
            sleep(5);
            status = readByte(REG_MEAS_STATUS);
            maxAttempts--;
        } while ((status & 0x80) == 0 && maxAttempts > 0); // Wait for new_data bit (bit 7)

        if (maxAttempts == 0) {
            LOGGER.warn("Timeout waiting for measurement to complete. Last status: 0x{}",
                    Integer.toHexString(status));
        }
    }

    /**
     * Calculates the expected measurement time in milliseconds based on oversampling settings.
     */
    private int calculateMeasurementTime(boolean withGas, int heaterDurationMs) {
        int tphTime = 0;
        if (temperatureOversampling != Oversampling.SKIP) {
            tphTime += temperatureOversampling.getSamples();
        }
        if (pressureOversampling != Oversampling.SKIP) {
            tphTime += pressureOversampling.getSamples();
        }
        if (humidityOversampling != Oversampling.SKIP) {
            tphTime += humidityOversampling.getSamples() * 2;
        }
        if (withGas) {
            tphTime += heaterDurationMs;
        }

        return tphTime + 10; // margin for ADC conversion
    }

    private int readRawTemperature() throws IOException {
        int msb = readByte(REG_TEMP_MSB);
        int lsb = readByte(REG_TEMP_LSB);
        int xlsb = readByte(REG_TEMP_XLSB);
        return (msb << 12) | (lsb << 4) | (xlsb >> 4);
    }

    private int readRawPressure() throws IOException {
        int msb = readByte(REG_PRESS_MSB);
        int lsb = readByte(REG_PRESS_LSB);
        int xlsb = readByte(REG_PRESS_XLSB);
        return (msb << 12) | (lsb << 4) | (xlsb >> 4);
    }

    private int readRawHumidity() throws IOException {
        int msb = readByte(REG_HUM_MSB);
        int lsb = readByte(REG_HUM_LSB);
        return (msb << 8) | lsb;
    }

    private float readAndCompensateGasResistance() throws IOException {
        int gasRMsb = readByte(REG_GAS_R_MSB);
        int gasRLsb = readByte(REG_GAS_R_LSB);

        int gasAdc = (gasRMsb << 2) | ((gasRLsb & 0xC0) >> 6);
        int gasRange = gasRLsb & 0x0F;

        long var1 = 262144L >> gasRange;
        int var2 = gasAdc - 512;
        var2 *= 3;
        var2 = 4096 + var2;
        return (float) (1000000.0 * var1 / var2);
    }

    private boolean isGasValid() throws IOException {
        int gasRLsb = readByte(REG_GAS_R_LSB);
        return (gasRLsb & 0x20) != 0; // gas_valid_r bit
    }

    private boolean isHeaterStable() throws IOException {
        int gasRLsb = readByte(REG_GAS_R_LSB);
        return (gasRLsb & 0x10) != 0; // heat_stab_r bit
    }

    private float compensateTemperature(int tempAdc) {
        double var1 = ((tempAdc / 16384.0) - (parT1 / 1024.0)) * parT2;
        double var2 = (((tempAdc / 131072.0) - (parT1 / 8192.0)) *
                ((tempAdc / 131072.0) - (parT1 / 8192.0))) * (parT3 * 16.0);
        tFine = var1 + var2;
        float temperature = (float) (tFine / 5120.0);
        lastAmbientTemp = (int) temperature;
        return temperature;
    }

    private float compensatePressure(int pressAdc) {
        double var1 = (tFine / 2.0) - 64000.0;
        double var2 = var1 * var1 * (parP6 / 131072.0);
        var2 = var2 + (var1 * parP5 * 2.0);
        var2 = (var2 / 4.0) + (parP4 * 65536.0);
        var1 = (((parP3 * var1 * var1) / 16384.0) + (parP2 * var1)) / 524288.0;
        var1 = (1.0 + (var1 / 32768.0)) * parP1;

        if ((int) var1 == 0) {
            return 0;
        }

        double pressComp = 1048576.0 - pressAdc;
        pressComp = ((pressComp - (var2 / 4096.0)) * 6250.0) / var1;
        var1 = (parP9 * pressComp * pressComp) / 2147483648.0;
        var2 = pressComp * (parP8 / 32768.0);
        double var3 = (pressComp / 256.0) * (pressComp / 256.0) * (pressComp / 256.0) * (parP10 / 131072.0);
        pressComp = pressComp + (var1 + var2 + var3 + (parP7 * 128.0)) / 16.0;

        return (float) pressComp;
    }

    private float compensateHumidity(int humAdc) {
        double tempComp = tFine / 5120.0;
        double var1 = humAdc - ((parH1 * 16.0) + ((parH3 / 2.0) * tempComp));
        double var2 = var1 * ((parH2 / 262144.0) * (1.0 + ((parH4 / 16384.0) * tempComp)
                + ((parH5 / 1048576.0) * tempComp * tempComp)));
        double var3 = parH6 / 16384.0;
        double var4 = parH7 / 2097152.0;
        double humComp = var2 + ((var3 + (var4 * tempComp)) * var2 * var2);

        // Clamp to valid range
        if (humComp > 100.0) {
            humComp = 100.0;
        } else if (humComp < 0.0) {
            humComp = 0.0;
        }
        return (float) humComp;
    }

    private void readCalibrationData() throws IOException {
        // Read temperature calibration parameters
        parT1 = readU2(0xE9, 0xEA);
        parT2 = readS2(0x8A, 0x8B);
        parT3 = readByte(0x8C);
        if (parT3 > 127) parT3 -= 256; // Sign extend int8_t

        // Read pressure calibration parameters
        parP1 = readU2(0x8E, 0x8F);
        parP2 = readS2(0x90, 0x91);
        parP3 = readByte(0x92);
        if (parP3 > 127) parP3 -= 256; // Sign extend int8_t
        parP4 = readS2(0x94, 0x95);
        parP5 = readS2(0x96, 0x97);
        parP6 = readByte(0x99);
        if (parP6 > 127) parP6 -= 256; // Sign extend int8_t
        parP7 = readByte(0x98);
        if (parP7 > 127) parP7 -= 256; // Sign extend int8_t
        parP8 = readS2(0x9C, 0x9D);
        parP9 = readS2(0x9E, 0x9F);
        parP10 = readByte(0xA0); // uint8_t — no sign extension

        // Read humidity calibration parameters
        int h1Lsb = readByte(0xE2);
        int h1Msb = readByte(0xE3);
        parH1 = ((h1Msb << 4) | (h1Lsb & 0x0F));

        int h2Lsb = readByte(0xE2);
        int h2Msb = readByte(0xE1);
        parH2 = ((h2Msb << 4) | ((h2Lsb >> 4) & 0x0F));

        parH3 = readByte(0xE4);
        parH4 = readByte(0xE5);
        parH5 = readByte(0xE6);
        parH6 = readByte(0xE7);
        parH7 = readByte(0xE8);

        // Sign extend int8_t params. H6 is uint8_t — no sign extension.
        if (parH3 > 127) parH3 -= 256;
        if (parH4 > 127) parH4 -= 256;
        if (parH5 > 127) parH5 -= 256;
        if (parH7 > 127) parH7 -= 256;

        // Read gas calibration parameters
        parG1 = readByte(0xED);
        if (parG1 > 127) parG1 -= 256; // Sign extend
        parG2 = readS2(0xEB, 0xEC);
        parG3 = readByte(0xEE);
        if (parG3 > 127) parG3 -= 256; // Sign extend

        // Read heater calibration parameters
        resHeatRange = (readByte(REG_RES_HEAT_RANGE) >> 4) & 0x03;
        resHeatVal = readByte(REG_RES_HEAT_VAL);
        if (resHeatVal > 127) resHeatVal -= 256; // Sign extend

        LOGGER.debug("BME688 calibration data loaded: parT1={}, parT2={}, parT3={}", parT1, parT2, parT3);
    }

    private int readU2(int lsbAddr, int msbAddr) throws IOException {
        int lsb = readByte(lsbAddr);
        int msb = readByte(msbAddr);
        return (msb << 8) | lsb;
    }

    private int readS2(int lsbAddr, int msbAddr) throws IOException {
        int value = readU2(lsbAddr, msbAddr);
        if (value > 32767) {
            value -= 65536;
        }
        return value;
    }

    /**
     * Data class holding all sensor readings.
     */
    public static class SensorData {
        private final float temperature;
        private final float pressure;
        private final float humidity;
        private final float gasResistance;
        private final boolean gasValid;

        public SensorData(float temperature, float pressure, float humidity,
                          float gasResistance, boolean gasValid) {
            this.temperature = temperature;
            this.pressure = pressure;
            this.humidity = humidity;
            this.gasResistance = gasResistance;
            this.gasValid = gasValid;
        }

        /**
         * @return the temperature in degrees Celsius
         */
        public float getTemperature() {
            return temperature;
        }

        /**
         * @return the pressure in Pascal
         */
        public float getPressure() {
            return pressure;
        }

        /**
         * @return the pressure in hectoPascal (hPa)
         */
        public float getPressureHPa() {
            return pressure / 100.0f;
        }

        /**
         * @return the relative humidity in percent
         */
        public float getHumidity() {
            return humidity;
        }

        /**
         * @return the gas resistance in Ohms, or NaN if not measured
         */
        public float getGasResistance() {
            return gasResistance;
        }

        /**
         * @return true if the gas measurement was valid
         */
        public boolean isGasValid() {
            return gasValid;
        }

        /**
         * @return the estimated altitude in meters based on pressure
         */
        public float getAltitude() {
            return (float) (44330.0 * (1.0 - Math.pow(pressure / PRESSURE_SEA_LEVEL, POW_FACT)));
        }

        @Override
        public String toString() {
            return String.format("SensorData[temp=%.2f°C, press=%.2fhPa, hum=%.2f%%, gas=%.0fΩ, gasValid=%b]",
                    temperature, pressure / 100.0, humidity, gasResistance, gasValid);
        }
    }

    /**
     * Data class holding air quality measurements and derived metrics.
     * <p>
     * <strong>Important:</strong> The IAQ, CO2 equivalent, and bVOC values in this class
     * are calculated using simplified algorithms for demonstration purposes. For accurate
     * air quality metrics suitable for production use, the Bosch BSEC library should be used.
     * The values here are useful for:
     * <ul>
     *   <li>Relative comparisons (detecting when air quality improves or degrades)</li>
     *   <li>Educational and experimental purposes</li>
     *   <li>Applications where approximate values are acceptable</li>
     * </ul>
     */
    public static class AirQualityData {
        private final float temperature;
        private final float pressure;
        private final float humidity;
        private final float gasResistance;
        private final float iaq;
        private final float co2Equivalent;
        private final float bvoc;
        private final IAQLevel iaqLevel;
        private final boolean valid;

        public AirQualityData(float temperature, float pressure, float humidity, float gasResistance,
                              float iaq, float co2Equivalent, float bvoc, IAQLevel iaqLevel, boolean valid) {
            this.temperature = temperature;
            this.pressure = pressure;
            this.humidity = humidity;
            this.gasResistance = gasResistance;
            this.iaq = iaq;
            this.co2Equivalent = co2Equivalent;
            this.bvoc = bvoc;
            this.iaqLevel = iaqLevel;
            this.valid = valid;
        }

        /**
         * @return the temperature in degrees Celsius
         */
        public float getTemperature() {
            return temperature;
        }

        /**
         * @return the pressure in Pascal
         */
        public float getPressure() {
            return pressure;
        }

        /**
         * @return the pressure in hectoPascal (hPa)
         */
        public float getPressureHPa() {
            return pressure / 100.0f;
        }

        /**
         * @return the relative humidity in percent
         */
        public float getHumidity() {
            return humidity;
        }

        /**
         * @return the raw gas resistance in Ohms
         */
        public float getGasResistance() {
            return gasResistance;
        }

        /**
         * Returns the Indoor Air Quality (IAQ) index (0-500 scale).
         * <ul>
         *   <li>0-50: Excellent</li>
         *   <li>51-100: Good</li>
         *   <li>101-150: Lightly polluted</li>
         *   <li>151-200: Moderately polluted</li>
         *   <li>201-250: Heavily polluted</li>
         *   <li>251-350: Severely polluted</li>
         *   <li>351-500: Extremely polluted</li>
         * </ul>
         *
         * @return the IAQ index, or NaN if measurement was invalid
         */
        public float getIAQ() {
            return iaq;
        }

        /**
         * Returns the estimated CO2 equivalent concentration in ppm.
         * This is an approximation based on VOC correlation, not a direct CO2 measurement.
         * Typical values:
         * <ul>
         *   <li>400-600: Outdoor/excellent indoor</li>
         *   <li>600-1000: Good indoor air</li>
         *   <li>1000-1500: Moderate, should ventilate</li>
         *   <li>1500-2500: Poor, ventilation needed</li>
         *   <li>&gt;2500: Very poor air quality</li>
         * </ul>
         *
         * @return estimated CO2 equivalent in ppm, or NaN if invalid
         */
        public float getCO2Equivalent() {
            return co2Equivalent;
        }

        /**
         * Returns the estimated breath VOC (bVOC) concentration in ppm.
         * Represents the total VOC equivalent typically found in human breath and indoor air.
         *
         * @return estimated bVOC in ppm, or NaN if invalid
         */
        public float getBVOC() {
            return bvoc;
        }

        /**
         * @return the categorized IAQ level
         */
        public IAQLevel getIAQLevel() {
            return iaqLevel;
        }

        /**
         * @return true if the air quality measurement was valid
         */
        public boolean isValid() {
            return valid;
        }

        @Override
        public String toString() {
            if (!valid) {
                return String.format("AirQualityData[invalid, temp=%.1f°C, hum=%.1f%%]",
                        temperature, humidity);
            }
            return String.format("AirQualityData[IAQ=%.0f (%s), CO2eq=%.0fppm, bVOC=%.2fppm, " +
                            "temp=%.1f°C, hum=%.1f%%, gas=%.0fΩ]",
                    iaq, iaqLevel.getDescription(), co2Equivalent, bvoc,
                    temperature, humidity, gasResistance);
        }
    }

    /**
     * Results from a multi-temperature gas scan.
     */
    public static class GasScanResult {
        private final GasScanProfile[] profiles;
        private final float[] resistances;
        private final boolean[] valid;
        private final AirQualityData airQuality;

        public GasScanResult(GasScanProfile[] profiles, float[] resistances,
                             boolean[] valid, AirQualityData airQuality) {
            this.profiles = Arrays.copyOf(profiles, profiles.length);
            this.resistances = Arrays.copyOf(resistances, resistances.length);
            this.valid = Arrays.copyOf(valid, valid.length);
            this.airQuality = airQuality;
        }

        /**
         * @return the number of heater profiles used in the scan
         */
        public int getProfileCount() {
            return profiles.length;
        }

        /**
         * @param index the profile index (0 to profileCount-1)
         * @return the heater profile configuration
         */
        public GasScanProfile getProfile(int index) {
            return profiles[index];
        }

        /**
         * @param index the profile index
         * @return the gas resistance in Ohms at this profile's temperature
         */
        public float getResistance(int index) {
            return resistances[index];
        }

        /**
         * @param index the profile index
         * @return true if the measurement at this profile was valid
         */
        public boolean isValid(int index) {
            return valid[index];
        }

        /**
         * @return array of all gas resistance values from the scan
         */
        public float[] getAllResistances() {
            return Arrays.copyOf(resistances, resistances.length);
        }

        /**
         * @return the air quality data calculated from the primary (first) profile
         */
        public AirQualityData getAirQuality() {
            return airQuality;
        }

        /**
         * Calculates the resistance ratio between two temperature profiles.
         * Different gas types produce characteristic ratio patterns.
         *
         * @param lowTempIndex  index of the lower temperature profile
         * @param highTempIndex index of the higher temperature profile
         * @return resistance ratio (low/high), or NaN if either measurement is invalid
         */
        public float getResistanceRatio(int lowTempIndex, int highTempIndex) {
            if (!valid[lowTempIndex] || !valid[highTempIndex] ||
                    resistances[highTempIndex] == 0) {
                return Float.NaN;
            }
            return resistances[lowTempIndex] / resistances[highTempIndex];
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("GasScanResult[");
            for (int i = 0; i < profiles.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(profiles[i].temperatureC()).append("C=");
                if (valid[i]) {
                    sb.append(String.format("%.0fΩ", resistances[i]));
                } else {
                    sb.append("invalid");
                }
            }
            sb.append(", ").append(airQuality.getIAQLevel().getDescription());
            sb.append("]");
            return sb.toString();
        }
    }
}
