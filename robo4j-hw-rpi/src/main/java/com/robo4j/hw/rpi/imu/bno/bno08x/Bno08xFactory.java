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

package com.robo4j.hw.rpi.imu.bno.bno08x;

import com.pi4j.io.spi.SpiChipSelect;
import com.pi4j.io.spi.SpiMode;
import com.robo4j.hw.rpi.imu.bno.Bno080Device;
import com.robo4j.hw.rpi.utils.GpioPin;
import com.robo4j.hw.rpi.utils.I2cBus;

/**
 * Factory for creating BNO08x devices with either SPI or I2C transport.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class Bno08xFactory {

    private Bno08xFactory() {
    }

    // --- I2C factory methods ---
    // WARNING: BNO08x uses I2C clock stretching which is not well supported on Raspberry Pi.
    // This may cause communication failures or bus lockups. Consider using SPI instead,
    // or connect the INT pin for interrupt-driven communication.

    /**
     * Creates a BNO08x device connected via I2C with default settings
     * (bus 1, address 0x4B, no GPIO pins). Suitable for SparkFun QWIIC boards.
     * <p>
     * <b>WARNING:</b> BNO08x uses I2C clock stretching which is not well supported
     * on Raspberry Pi hardware I2C. Consider using {@link #createDefaultSPIDevice()}
     * instead, or use {@link #createI2CDevice(I2cBus, int, GpioPin, GpioPin)} with
     * an interrupt pin for more reliable communication.
     * </p>
     *
     * @return a BNO08x device using I2C transport
     */
    public static Bno080Device createDefaultI2CDevice() {
        ShtpI2CTransport transport = new ShtpI2CTransport();
        return new Bno08xDriver(transport);
    }

    /**
     * Creates a BNO08x device connected via I2C with specified bus and address.
     *
     * @param bus     the I2C bus
     * @param address the I2C address (0x4A or 0x4B)
     * @return a BNO08x device using I2C transport
     */
    public static Bno080Device createI2CDevice(I2cBus bus, int address) {
        ShtpI2CTransport transport = new ShtpI2CTransport(bus, address);
        return new Bno08xDriver(transport);
    }

    /**
     * Creates a BNO08x device connected via I2C with specified bus, address,
     * and optional GPIO pins for interrupt and reset.
     *
     * @param bus       the I2C bus
     * @param address   the I2C address (0x4A or 0x4B)
     * @param interrupt GPIO pin for interrupt (may be null)
     * @param reset     GPIO pin for reset (may be null)
     * @return a BNO08x device using I2C transport
     */
    public static Bno080Device createI2CDevice(I2cBus bus, int address,
                                                GpioPin interrupt, GpioPin reset) {
        ShtpI2CTransport transport = new ShtpI2CTransport(bus, address, interrupt, reset);
        return new Bno08xDriver(transport);
    }

    // --- SPI factory methods ---

    /**
     * Creates a BNO08x device connected via SPI with default settings.
     * Backward compatible with the old {@code Bno080Factory.createDefaultSPIDevice()}.
     *
     * @return a BNO08x device using SPI transport
     * @throws InterruptedException if interrupted during pin configuration
     */
    public static Bno080Device createDefaultSPIDevice() throws InterruptedException {
        ShtpSpiTransport transport = new ShtpSpiTransport();
        return new Bno08xDriver(transport);
    }

    /**
     * Creates a BNO08x device connected via SPI with custom settings.
     *
     * @param channel   the SPI chip select
     * @param mode      the SPI mode
     * @param speed     the SPI clock speed in Hz
     * @param wake      GPIO pin for wake
     * @param cs        GPIO pin for chip select
     * @param reset     GPIO pin for reset
     * @param interrupt GPIO pin for interrupt
     * @return a BNO08x device using SPI transport
     * @throws InterruptedException if interrupted during pin configuration
     */
    public static Bno080Device createSPIDevice(SpiChipSelect channel, SpiMode mode,
                                                int speed, GpioPin wake, GpioPin cs,
                                                GpioPin reset, GpioPin interrupt)
            throws InterruptedException {
        ShtpSpiTransport transport = new ShtpSpiTransport(channel, mode, speed, wake, cs, reset, interrupt);
        return new Bno08xDriver(transport);
    }
}
