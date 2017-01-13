/*
 * Copyright (C) 2014-2017, Marcus Hirt
 * 
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.hw.rpi.i2c.adafruitoled;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;

/**
 * Support for SSD1306 devices over I2C. A good example is the Adafruit 128x64
 * (or 128x32) monochrome OLED.
 * 
 * @author Marcus Hirt
 */
public class SSD1306Device extends AbstractI2CDevice {
	private final static byte CHARGE_PUMP_VALUE_ENABLE = 0x14;
	private final static byte CHARGE_PUMP_VALUE_DISABLE = 0x10;
	private final static int DEFAULT_CONTRAST = 0x88;

	public enum OLEDVariant {
		Type96x16(96, 16, 0x2, 1),
		Type128x32(128, 32, 0x2, 3),
		Type128x64(128, 64, 0x12, 7);

		private final int width;
		private final int height;
		private final int comPins;
		private final int pageEnd;

		private OLEDVariant(int width, int height, int comPins, int pageEnd) {
			this.width = width;
			this.height = height;
			this.comPins = comPins;
			this.pageEnd = pageEnd;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public int getComPins() {
			return comPins;
		}

		public int getPageEnd() {
			return pageEnd;
		}
	}

	private enum Commands {
		DISPLAY_OFF((byte) 0xae),
		DISPLAY_ON((byte) 0xaf),
		INVERTED_ON((byte) 0xa7),
		INVERTED_OFF((byte) 0xa6),
		SET_DISPLAY_CLOCK_DIV((byte) 0xd5),
		CHARGE_PUMP((byte) 0x8d),
		MEMORY_MODE((byte) 0x20),
		SEGMENT_REMAP_0((byte) 0xa0),
		SEGMENT_REMAP_127((byte) 0xa1),
		SET_MULTIPLEX_RATIO((byte) 0xa8),
		SET_DISPLAY_OFFSET((byte) 0xd3),
		SET_START_LINE_ZERO((byte) 0x40),
		COM_OUTPUT_SCAN_DIR_ASCENDING((byte) 0xc0),
		COM_OUTPUT_SCAN_DIR_DESCENDING((byte) 0xc8),
		SET_COM_PINS((byte) 0xda),
		SET_CONTRAST((byte) 0x81),
		SET_PRE_CHARGE_PERIOD((byte) 0xd9),
		SET_VCOM_DESELECT_LEVEL((byte) 0xdb),
		RAM_CONTENT_DISPLAY((byte) 0xa4),
		ENTIRE_DISPLAY_ON((byte) 0xa5),
		DEACTIVATE_SCROLL((byte) 0x2e),
		SET_COLUMN_ADDRESS((byte) 0x21),
		SET_PAGE_ADDRESS((byte) 0x22);

		private byte commandValue;

		private Commands(byte commandValue) {
			this.commandValue = commandValue;
		}

		public byte getCommandValue() {
			return commandValue;
		}
	}

	private enum MemoryModes {
		HORIZONTAL((byte) 0),
		VERTICAL((byte) 1),
		PAGE((byte) 2);

		private byte value;

		private MemoryModes(byte value) {
			this.value = value;
		}

		public byte getValue() {
			return value;
		}

	}

	private final BufferedImage image;
	private final GpioController gpio = GpioFactory.getInstance();
	private final GpioPinDigitalOutput resetPin;
	private final boolean useExtenalVCC;
	private final OLEDVariant oledType;

	/**
	 * Constructor.
	 * 
	 * @param variant
	 *            the oled variant, most commonly the 32 or 64 line version.
	 * @param resetPin
	 *            the GPIO pin used for the reset.
	 * 
	 * @throws IOException
	 *             if there was a communication problem.
	 */
	public SSD1306Device(OLEDVariant variant, Pin resetPin) throws IOException {
		this(I2CBus.BUS_1, 0x3c, variant, resetPin, false);
	}

	/**
	 * Constructor.
	 * 
	 * @param bus
	 *            the I2C bus used.
	 * @param address
	 *            the I2C address of the OLED device, most commonly 0x3c.
	 * @param oledType
	 *            the variant of the oled (depends on which version you own).
	 * @param resetPinId
	 *            the GPIO pin used for reset (depends on your wiring).
	 * @param useExternalVCC
	 *            use external VCC to drive the OLED. If false, the internal
	 *            charge pump will be used to regulate to the necessary voltage.
	 *            This is most commonly false.
	 * 
	 * @throws IOException
	 *             if there was a communication problem.
	 */
	public SSD1306Device(int bus, int address, OLEDVariant oledType,
			Pin resetPinId, boolean useExternalVCC) throws IOException {
		super(bus, address);
		this.image = new BufferedImage(oledType.getWidth(),
				oledType.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
		this.resetPin = gpio.provisionDigitalOutputPin(resetPinId, "reset",
				PinState.HIGH);
		this.useExtenalVCC = useExternalVCC;
		this.oledType = oledType;
		initialize();
	}

	/**
	 * @return the graphics context upon which to draw. This being a monochrome
	 *         display, only the colors {@link Color}.black and {@link Color}
	 *         .white should be used.
	 */
	public Graphics2D getGraphicsContext() {
		return image.createGraphics();
	}

	/**
	 * Pushes the image data to the device over I2C.
	 * 
	 * @throws IOException
	 */
	public void pushImage() throws IOException {
		executeCommand(Commands.SET_COLUMN_ADDRESS, 0, oledType.getWidth() - 1);
		executeCommand(Commands.SET_PAGE_ADDRESS, 0, oledType.getPageEnd());

		// Transmitting image data in one write
		byte[] byteArray = toByteArray();
		System.out.println(Arrays.toString(byteArray));
		i2cDevice.write(0x40, byteArray);
	}

	/**
	 * @param enable
	 *            false to disable (turn off), true to enable (turn on).
	 *            
	 * @throws IOException
	 */
	public void setEnabled(boolean enable) throws IOException {
		if (enable) {
			executeCommand(Commands.DISPLAY_ON);
		} else {
			executeCommand(Commands.DISPLAY_OFF);
		}
	}

	/**
	 * Sets the contrast between 0 (minimum) and 1.0 (max).
	 *  
	 * @param contrast a value between 0 and 1.0.
	 * @throws IOException 
	 */
	public void setContrast(float contrast) throws IOException {
		executeCommand(Commands.SET_CONTRAST, Math.max(Math.round(contrast * 0xff), 0xff));
	}

	/**
	 * @return the image used to draw upon.
	 */
	public BufferedImage getImage() {
		return image;
	}
	
	private byte[] toByteArray() {
		int byteCount = 0;
		byte[] bytes = new byte[oledType.getHeight() * oledType.getWidth() / 8];
		for (int y = 0; y < oledType.getHeight();) {
			for (int x = 0; x < oledType.getWidth(); x++) {
				int next = 0;
				int step = 0;
				for (; step < 8 && y + step < oledType.getHeight(); step++) {
					if (image.getRGB(x, y + step) != Color.black.getRGB()) {
						next |= (1 << step);
					}
				}
				bytes[byteCount] = (byte) next;
				byteCount++;
			}
			y += 8;
		}
		return bytes;
	}
	
	private void initialize() throws IOException {
		sleep(1);
		resetPin.setState(PinState.LOW);
		sleep(10);
		resetPin.setState(PinState.HIGH);
		executeCommand(Commands.DISPLAY_OFF);
		executeCommand(Commands.SET_DISPLAY_CLOCK_DIV, 0x80);
		if (!useExtenalVCC) {
			executeCommand(Commands.CHARGE_PUMP, CHARGE_PUMP_VALUE_ENABLE);
		} else {
			executeCommand(Commands.CHARGE_PUMP, CHARGE_PUMP_VALUE_DISABLE);
		}
		setMemoryMode(MemoryModes.HORIZONTAL);
		executeCommand(Commands.SEGMENT_REMAP_127);
		executeCommand(Commands.SET_MULTIPLEX_RATIO, oledType.getHeight() - 1);
		executeCommand(Commands.SET_DISPLAY_OFFSET, 0);
		executeCommand(Commands.SET_START_LINE_ZERO);
		executeCommand(Commands.COM_OUTPUT_SCAN_DIR_DESCENDING);
		executeCommand(Commands.SET_COM_PINS, oledType.getComPins());
		executeCommand(Commands.SET_CONTRAST, DEFAULT_CONTRAST);
		executeCommand(Commands.SET_PRE_CHARGE_PERIOD, useExtenalVCC ? 0x22
				: 0xf1);
		executeCommand(Commands.SET_VCOM_DESELECT_LEVEL, 0x40);
		executeCommand(Commands.RAM_CONTENT_DISPLAY);
		executeCommand(Commands.INVERTED_OFF);
		executeCommand(Commands.DEACTIVATE_SCROLL);
		executeCommand(Commands.DISPLAY_ON);
	}

	private void executeCommand(Commands command, int value1, int value2)
			throws IOException {
		executeCommand(command);
		writeCommand((byte) value1);
		writeCommand((byte) value2);
	}

	private void executeCommand(Commands command, int value) throws IOException {
		executeCommand(command);
		writeCommand((byte) value);
	}

	private void executeCommand(Commands command) throws IOException {
		writeCommand(command.getCommandValue());
	}

	private void writeCommand(byte commandValue) throws IOException {
		i2cDevice.write(0x00, commandValue);
	}

	private void setMemoryMode(MemoryModes mode) throws IOException {
		executeCommand(Commands.MEMORY_MODE, mode.getValue());
	}

}
