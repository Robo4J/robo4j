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
package com.robo4j.hw.rpi.imu.bno;

import com.pi4j.io.spi.SpiChipSelect;
import com.pi4j.io.spi.SpiMode;
import com.robo4j.hw.rpi.imu.bno.bno08x.Bno08xFactory;
import com.robo4j.hw.rpi.utils.GpioPin;

/**
 * Factory for creating BNO080 devices.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 * @deprecated Use {@link Bno08xFactory} instead.
 */
@Deprecated(forRemoval = true)
public final class Bno080Factory {

	/**
	 * Creates a BNO080 device connected via I2C with default settings.
	 *
	 * @return an I2C connected BNO080 device
	 * @deprecated Use {@link Bno08xFactory#createDefaultI2CDevice()} instead.
	 */
	@Deprecated(forRemoval = true)
	public static Bno080Device createDefaultI2CDevice() {
		return Bno08xFactory.createDefaultI2CDevice();
	}

	/**
	 * Creates a BNO080 device connected via SPI with default settings.
	 *
	 * @return an SPI connected BNO080 device
	 * @throws InterruptedException if interrupted during initialization
	 * @deprecated Use {@link Bno08xFactory#createDefaultSPIDevice()} instead.
	 */
	@Deprecated(forRemoval = true)
	public static Bno080Device createDefaultSPIDevice() throws InterruptedException {
		return Bno08xFactory.createDefaultSPIDevice();
	}

	/**
	 * Creates a BNO080 device connected via SPI with the provided settings.
	 *
	 * @param channel   the SPI chip select
	 * @param mode      the SPI mode
	 * @param speed     the SPI clock speed in Hz
	 * @param wake      GPIO pin for wake
	 * @param cs        GPIO pin for chip select
	 * @param reset     GPIO pin for reset
	 * @param interrupt GPIO pin for interrupt
	 * @return an SPI connected BNO080 device
	 * @throws InterruptedException if interrupted during initialization
	 * @deprecated Use {@link Bno08xFactory#createSPIDevice(SpiChipSelect, SpiMode, int, GpioPin, GpioPin, GpioPin, GpioPin)} instead.
	 */
	@Deprecated(forRemoval = true)
	public static Bno080Device createDefaultSPIDevice(SpiChipSelect channel, SpiMode mode, int speed,
			GpioPin wake, GpioPin cs, GpioPin reset, GpioPin interrupt) throws InterruptedException {
		return Bno08xFactory.createSPIDevice(channel, mode, speed, wake, cs, reset, interrupt);
	}
}
