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

package com.robo4j.units.rpi.led;

import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.i2c.adafruitoled.BiColor24BarDevice;
import com.robo4j.hw.rpi.i2c.adafruitoled.BiColor8x8MatrixDevice;
import com.robo4j.hw.rpi.i2c.adafruitoled.LEDBackpackType;
import com.robo4j.hw.rpi.i2c.adafruitoled.MatrixRotation;
import com.robo4j.hw.rpi.i2c.adafruitoled.PackElement;

import java.util.List;

/**
 * Adafruit Bi-Color 8x8 Matrix
 *
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-8x8-matrix
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Adafruit8x8MatrixUnit extends AbstractLEDBackpackUnit<BiColor8x8MatrixDevice> {

    public static final String DEFAULT_MATRIX_ROTATION = "one";
    public static final String ATTRIBUTE_ADDRESS = "address";
    public static final String ATTRIBUTE_BUS = "bus";
    public static final String ATTRIBUTE_ROTATION = "rotation";

    private BiColor8x8MatrixDevice device;

	public Adafruit8x8MatrixUnit(RoboContext context, String id) {
		super(LEDBackpackMessage.class, context, id);
	}

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        int address = configuration.getInteger(ATTRIBUTE_ADDRESS, BiColor24BarDevice.DEFAULT_I2C_ADDRESS);
        int bus = configuration.getInteger(ATTRIBUTE_BUS, BiColor24BarDevice.DEFAULT_I2C_BUS);
        MatrixRotation rotation = MatrixRotation.valueOf(configuration.getString(ATTRIBUTE_ROTATION, DEFAULT_MATRIX_ROTATION).toUpperCase());
        device = getBackpackDevice(LEDBackpackType.BI_COLOR_MATRIX_8x8, bus, address);
        device.setRotation(rotation);
        System.out.println("init DONE");
    }

    @Override
    public void onMessage(LEDBackpackMessage message) {
        processMessage(device, message);
    }

    @Override
    void addElements(List<PackElement> elements) {
        device.addPixels(elements);
    }
}
