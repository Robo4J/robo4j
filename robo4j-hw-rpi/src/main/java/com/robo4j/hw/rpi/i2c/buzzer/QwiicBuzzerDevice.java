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
package com.robo4j.hw.rpi.i2c.buzzer;

import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.utils.I2cBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Driver for the SparkFun Qwiic Buzzer (BOB-24474).
 * <p>
 * The Qwiic Buzzer is an I2C-controlled magnetic buzzer that provides
 * programmable tones with adjustable frequency, duration, and volume.
 * It uses an ATtiny84 microcontroller with custom firmware.
 * <p>
 * Features:
 * <ul>
 *   <li>Frequency control (resonant frequency: 2730 Hz)</li>
 *   <li>Duration control in milliseconds (0 = continuous)</li>
 *   <li>4 volume levels (OFF, MIN, LOW, MID, MAX)</li>
 *   <li>10 built-in sound effects</li>
 *   <li>Configurable I2C address</li>
 *   <li>Settings can be saved to EEPROM</li>
 * </ul>
 *
 * @see <a href="https://github.com/sparkfun/SparkFun_Qwiic_Buzzer">SparkFun Qwiic Buzzer</a>
 * @author Marcus Hirt (@hirt)
 */
public final class QwiicBuzzerDevice extends AbstractI2CDevice {
    private static final Logger LOGGER = LoggerFactory.getLogger(QwiicBuzzerDevice.class);

    /**
     * Default I2C address for the Qwiic Buzzer.
     */
    public static final int DEFAULT_I2C_ADDRESS = 0x34;

    /**
     * Device ID that should be read from the ID register.
     */
    public static final int DEVICE_ID = 0x5E;

    /**
     * Resonant frequency of the buzzer in Hz.
     * This frequency produces the loudest tone.
     */
    public static final int RESONANT_FREQUENCY = 2730;

    private static final int REG_ID = 0x00;
    private static final int REG_FIRMWARE_MINOR = 0x01;
    private static final int REG_FIRMWARE_MAJOR = 0x02;
    private static final int REG_TONE_FREQUENCY_MSB = 0x03;
    private static final int REG_TONE_FREQUENCY_LSB = 0x04;
    private static final int REG_VOLUME = 0x05;
    private static final int REG_DURATION_MSB = 0x06;
    private static final int REG_DURATION_LSB = 0x07;
    private static final int REG_ACTIVE = 0x08;
    private static final int REG_SAVE_SETTINGS = 0x09;
    private static final int REG_I2C_ADDRESS = 0x0A;

    /**
     * Volume levels for the Qwiic Buzzer.
     */
    public enum Volume {
        OFF(0),
        MIN(1),
        LOW(2),
        MID(3),
        MAX(4);

        private final int value;

        Volume(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Built-in sound effects available on the Qwiic Buzzer.
     */
    public enum SoundEffect {
        /**
         * Siren sound - single up and down cycle.
         */
        SIREN(0),
        /**
         * Three fast siren cycles.
         */
        FAST_SIRENS(1),
        /**
         * Robot saying "Yes" - rising tone.
         */
        ROBOT_YES(2),
        /**
         * Robot yelling "YES!" - faster rising tone.
         */
        ROBOT_YES_FAST(3),
        /**
         * Robot saying "No" - falling tone.
         */
        ROBOT_NO(4),
        /**
         * Robot yelling "NO!" - faster falling tone.
         */
        ROBOT_NO_FAST(5),
        /**
         * Laughing robot sound.
         */
        LAUGH(6),
        /**
         * Faster laughing robot sound.
         */
        LAUGH_FAST(7),
        /**
         * Crying robot sound.
         */
        CRY(8),
        /**
         * Faster crying robot sound.
         */
        CRY_FAST(9);

        private final int value;

        SoundEffect(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Constructs a QwiicBuzzerDevice using default settings (I2C BUS_1, address 0x34).
     *
     * @throws IOException if there was a communication problem
     */
    public QwiicBuzzerDevice() throws IOException {
        this(I2cBus.BUS_1, DEFAULT_I2C_ADDRESS);
    }

    /**
     * Constructs a QwiicBuzzerDevice with the specified I2C bus and address.
     *
     * @param bus     the I2C bus to use
     * @param address the I2C address of the device
     * @throws IOException if there was a communication problem
     */
    public QwiicBuzzerDevice(I2cBus bus, int address) throws IOException {
        super(bus, address);
        initialize();
    }

    /**
     * Initializes the device by verifying the device ID.
     */
    private void initialize() throws IOException {
        int deviceId = getDeviceId();
        if (deviceId != DEVICE_ID) {
            throw new IOException("Invalid Qwiic Buzzer device ID: 0x" + Integer.toHexString(deviceId)
                    + ". Expected 0x" + Integer.toHexString(DEVICE_ID));
        }
        LOGGER.info("Qwiic Buzzer detected with device ID: 0x{}", Integer.toHexString(deviceId));
    }

    /**
     * Returns the device ID.
     *
     * @return the device ID (should be 0x5E for a valid Qwiic Buzzer)
     * @throws IOException if there was a communication problem
     */
    public int getDeviceId() throws IOException {
        return readByte(REG_ID);
    }

    /**
     * Returns the firmware major version.
     *
     * @return the firmware major version number
     * @throws IOException if there was a communication problem
     */
    public int getFirmwareMajor() throws IOException {
        return readByte(REG_FIRMWARE_MAJOR);
    }

    /**
     * Returns the firmware minor version.
     *
     * @return the firmware minor version number
     * @throws IOException if there was a communication problem
     */
    public int getFirmwareMinor() throws IOException {
        return readByte(REG_FIRMWARE_MINOR);
    }

    /**
     * Returns the firmware version as a string (e.g., "1.0").
     *
     * @return the firmware version string
     * @throws IOException if there was a communication problem
     */
    public String getFirmwareVersion() throws IOException {
        return getFirmwareMajor() + "." + getFirmwareMinor();
    }

    /**
     * Configures the buzzer with the specified parameters without starting it.
     * This allows silent configuration before calling {@link #on()}.
     * Use this in combination with {@link #saveSettings()} to configure the buzzer
     * for use with the physical TRIGGER pin.
     *
     * @param frequency the tone frequency in Hz
     * @param duration  the duration in milliseconds (0 = continuous until off() is called)
     * @param volume    the volume level
     * @throws IOException if there was a communication problem
     */
    public void configure(int frequency, int duration, Volume volume) throws IOException {
        byte[] data = new byte[5];
        data[0] = (byte) ((frequency >> 8) & 0xFF);
        data[1] = (byte) (frequency & 0xFF);
        data[2] = (byte) volume.getValue();
        data[3] = (byte) ((duration >> 8) & 0xFF);
        data[4] = (byte) (duration & 0xFF);

        writeByteBufferByAddress(REG_TONE_FREQUENCY_MSB, data);
        LOGGER.debug("Configured buzzer: frequency={}Hz, duration={}ms, volume={}", frequency, duration, volume);
    }

    /**
     * Configures the buzzer using a musical note.
     *
     * @param note     the musical note to play
     * @param duration the duration in milliseconds (0 = continuous)
     * @param volume   the volume level
     * @throws IOException if there was a communication problem
     */
    public void configure(Note note, int duration, Volume volume) throws IOException {
        configure(note.getFrequency(), duration, volume);
    }

    /**
     * Turns on the buzzer with the currently configured settings.
     *
     * @throws IOException if there was a communication problem
     */
    public void on() throws IOException {
        writeByte(REG_ACTIVE, (byte) 1);
        LOGGER.debug("Buzzer turned on");
    }

    /**
     * Turns off the buzzer.
     *
     * @throws IOException if there was a communication problem
     */
    public void off() throws IOException {
        writeByte(REG_ACTIVE, (byte) 0);
        LOGGER.debug("Buzzer turned off");
    }

    /**
     * Plays a tone with the specified parameters.
     * This is a convenience method that configures and starts the buzzer in one call.
     *
     * @param frequency the tone frequency in Hz
     * @param duration  the duration in milliseconds (0 = continuous until off() is called)
     * @param volume    the volume level
     * @throws IOException if there was a communication problem
     */
    public void playTone(int frequency, int duration, Volume volume) throws IOException {
        configure(frequency, duration, volume);
        on();
    }

    /**
     * Plays a musical note with the specified parameters.
     *
     * @param note     the musical note to play
     * @param duration the duration in milliseconds (0 = continuous)
     * @param volume   the volume level
     * @throws IOException if there was a communication problem
     */
    public void playNote(Note note, int duration, Volume volume) throws IOException {
        playTone(note.getFrequency(), duration, volume);
    }

    /**
     * Plays a beep at the resonant frequency (loudest).
     *
     * @param duration the duration in milliseconds
     * @param volume   the volume level
     * @throws IOException if there was a communication problem
     */
    public void beep(int duration, Volume volume) throws IOException {
        playTone(RESONANT_FREQUENCY, duration, volume);
    }

    /**
     * Plays a beep at the resonant frequency with maximum volume.
     *
     * @param duration the duration in milliseconds
     * @throws IOException if there was a communication problem
     */
    public void beep(int duration) throws IOException {
        beep(duration, Volume.MAX);
    }

    /**
     * Plays a built-in sound effect.
     *
     * @param effect the sound effect to play
     * @param volume the volume level
     * @throws IOException if there was a communication problem
     */
    public void playSoundEffect(SoundEffect effect, Volume volume) throws IOException {
        switch (effect) {
            case SIREN -> playSiren(volume, 1);
            case FAST_SIRENS -> playSiren(volume, 3);
            case ROBOT_YES -> playRobotYes(volume, 40);
            case ROBOT_YES_FAST -> playRobotYes(volume, 10);
            case ROBOT_NO -> playRobotNo(volume, 40);
            case ROBOT_NO_FAST -> playRobotNo(volume, 10);
            case LAUGH -> playLaugh(volume, 400, 10);
            case LAUGH_FAST -> playLaugh(volume, 200, 15);
            case CRY -> playCry(volume, 500, 10);
            case CRY_FAST -> playCry(volume, 200, 20);
        }
    }

    /**
     * Saves the current buzzer settings to EEPROM.
     * These settings will be used when the buzzer is triggered via the TRIGGER pin.
     *
     * @throws IOException if there was a communication problem
     */
    public void saveSettings() throws IOException {
        writeByte(REG_SAVE_SETTINGS, (byte) 1);
        LOGGER.info("Settings saved to EEPROM");
    }

    /**
     * Changes the I2C address of the buzzer.
     * The new address is saved to EEPROM and takes effect immediately.
     *
     * @param newAddress the new I2C address (must be between 0x08 and 0x77)
     * @throws IOException if there was a communication problem or the address is invalid
     */
    public void setI2CAddress(int newAddress) throws IOException {
        if (newAddress < 0x08 || newAddress > 0x77) {
            throw new IllegalArgumentException("Address must be between 0x08 and 0x77, got: 0x"
                    + Integer.toHexString(newAddress));
        }
        writeByte(REG_I2C_ADDRESS, (byte) newAddress);
        LOGGER.info("I2C address changed to: 0x{}", Integer.toHexString(newAddress));
    }

    private void playSiren(Volume volume, int cycles) throws IOException {
        int stepDelay = cycles == 1 ? 10 : 2;
        for (int c = 0; c < cycles; c++) {
            for (int freq = 150; freq < 4000; freq += 150) {
                configure(freq, 0, volume);
                on();
                sleep(stepDelay);
            }
            for (int freq = 4000; freq > 150; freq -= 150) {
                configure(freq, 0, volume);
                on();
                sleep(stepDelay);
            }
        }
        off();
    }

    private void playRobotYes(Volume volume, int stepDelay) throws IOException {
        for (int freq = 150; freq < 4000; freq += 150) {
            configure(freq, 0, volume);
            on();
            sleep(stepDelay);
        }
        off();
    }

    private void playRobotNo(Volume volume, int stepDelay) throws IOException {
        for (int freq = 4000; freq > 150; freq -= 150) {
            configure(freq, 0, volume);
            on();
            sleep(stepDelay);
        }
        off();
    }

    private void playLaugh(Volume volume, int laughDelay, int laughStep) throws IOException {
        int[][] ranges = {{1538, 1905}, {1250, 1515}, {1111, 1342}, {1010, 1176}};
        for (int i = 0; i < ranges.length; i++) {
            for (int freq = ranges[i][0]; freq < ranges[i][1]; freq += laughStep) {
                configure(freq, 0, volume);
                on();
                sleep(10);
            }
            off();
            if (i < ranges.length - 1) {
                sleep(laughDelay);
            }
        }
    }

    private void playCry(Volume volume, int cryDelay, int step) throws IOException {
        int[][] ranges = {{2000, 1429}, {1667, 1250}, {1429, 1053}};
        for (int i = 0; i < ranges.length; i++) {
            for (int freq = ranges[i][0]; freq > ranges[i][1]; freq -= step) {
                configure(freq, 0, volume);
                on();
                sleep(10);
            }
            off();
            if (i < ranges.length - 1) {
                sleep(cryDelay);
            }
        }
    }
}
