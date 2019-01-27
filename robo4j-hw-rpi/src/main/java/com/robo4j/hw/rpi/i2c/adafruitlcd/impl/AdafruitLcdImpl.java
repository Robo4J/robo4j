/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.rpi.i2c.adafruitlcd.impl;

import java.io.IOException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;
import com.robo4j.hw.rpi.i2c.adafruitlcd.AdafruitLcd;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Button;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Color;

/**
 * Javaification of the python script example for the Adafruit LCD shield. I
 * have deliberately kept this close to the original python script, including
 * most comments, even though it leads to less than beautiful code.
 * 
 * Here is an example on how to use this class: <code>
 * LCD lcd = new LCD();
 * lcd.setText("Hello World!\n2nd Hello World!");
 * </code>
 * 
 * Here is an example with buttons: <code>
 * final LCD lcd = new LCD();
 * lcd.setText("LCD Test!\nPress up/down...");
 * ButtonPressedObserver observer = new ButtonPressedObserver(lcd);
 * observer.addButtonListener(new ButtonListener() {
 * 		&#64;Override
 *      public void onButtonPressed(Button button) {
 *      	lcd.clear();
 *      	lcd.setText(button.toString());
 *      }
 * });
 * </code>
 * 
 * For more examples, check out the se.hirt.adafruitlcd.test package.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class AdafruitLcdImpl extends AbstractI2CDevice implements AdafruitLcd {
	public enum Direction {
		LEFT, RIGHT;
	}

	// LCD Commands
	private static final int LCD_CLEARDISPLAY = 0x01;
	private static final int LCD_RETURNHOME = 0x02;
	private static final int LCD_ENTRYMODESET = 0x04;
	private static final int LCD_DISPLAYCONTROL = 0x08;
	private static final int LCD_CURSORSHIFT = 0x10;
	// private static final int LCD_FUNCTIONSET = 0x20;
	private static final int LCD_SETCGRAMADDR = 0x40;
	private static final int LCD_SETDDRAMADDR = 0x80;

	// Flags for display on/off control
	private static final int LCD_DISPLAYON = 0x04;
	// private static final int LCD_DISPLAYOFF = 0x00;
	private static final int LCD_CURSORON = 0x02;
	private static final int LCD_CURSOROFF = 0x00;
	private static final int LCD_BLINKON = 0x01;
	private static final int LCD_BLINKOFF = 0x00;

	// Flags for display entry mode
	// private static final int LCD_ENTRYRIGHT = 0x00;
	private static final int LCD_ENTRYLEFT = 0x02;
	private static final int LCD_ENTRYSHIFTINCREMENT = 0x01;
	private static final int LCD_ENTRYSHIFTDECREMENT = 0x00;

	// Flags for display/cursor shift
	private static final int LCD_DISPLAYMOVE = 0x08;
	private static final int LCD_CURSORMOVE = 0x00;
	private static final int LCD_MOVERIGHT = 0x04;
	private static final int LCD_MOVELEFT = 0x00;

	// Port expander registers
	// IOCON when Bank 0 active
	private static final int MCP23017_IOCON_BANK0 = 0x0A;
	// IOCON when Bank 1 active
	private static final int MCP23017_IOCON_BANK1 = 0x15;

	// These are register addresses when in Bank 1 only:
	private static final int MCP23017_GPIOA = 0x09;
	private static final int MCP23017_IODIRB = 0x10;
	private static final int MCP23017_GPIOB = 0x19;

	// The LCD data pins (D4-D7) connect to MCP pins 12-9 (PORTB4-1), in
	// that order. Because this sequence is 'reversed,' a direct shift
	// won't work. This table remaps 4-bit data values to MCP PORTB
	// outputs, incorporating both the reverse and shift.
	private static final int[] SHIFT_REVERSE = { 0x00, 0x10, 0x08, 0x18, 0x04, 0x14, 0x0C, 0x1C, 0x02, 0x12, 0x0A, 0x1A, 0x06, 0x16, 0x0E,
			0x1E };

	private static final int[] ROW_OFFSETS = new int[] { 0x00, 0x40, 0x14, 0x54 };

	private int portA = 0x00;
	private int portB = 0x00;
	private int ddrB = 0x10;
	private int displayShift = LCD_CURSORMOVE | LCD_MOVERIGHT;
	private int displayMode = LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT;
	private int displayControl = LCD_DISPLAYON | LCD_CURSOROFF | LCD_BLINKOFF;
	private Color color = Color.WHITE;

	public AdafruitLcdImpl() throws IOException, UnsupportedBusNumberException {
		// This seems to be the default for AdaFruit 1115.
		this(AdafruitLcd.DEFAULT_BUS, AdafruitLcd.DEFAULT_ADDRESS);
	}

	public AdafruitLcdImpl(int bus, int address) throws IOException, UnsupportedBusNumberException {
		super(bus, address);
		initialize();
	}

	private synchronized void initialize() throws IOException {
		// Set MCP23017 IOCON register to Bank 0 with sequential operation.
		// If chip is already set for Bank 0, this will just write to OLATB,
		// which won't seriously bother anything on the plate right now
		// (blue backlight LED will come on, but that's done in the next
		// step anyway).
		write(MCP23017_IOCON_BANK1, (byte) 0);

		// Brute force reload ALL registers to known state. This also
		// sets up all the input pins, pull-ups, etc. for the Pi Plate.
		// NOTE(marcus/9 dec 2013): 0x3F assumes that GPA5 is input too -
		// it is however not connected.
		byte[] registers = { 0x3F, // IODIRA R+G LEDs=outputs, buttons=inputs
				(byte) ddrB, // IODIRB LCD D7=input, Blue LED=output
				0x3F, // IPOLA Invert polarity on button inputs
				0x00, // IPOLB
				0x00, // GPINTENA Disable interrupt-on-change
				0x00, // GPINTENB
				0x00, // DEFVALA
				0x00, // DEFVALB
				0x00, // INTCONA
				0x00, // INTCONB
				0x00, // IOCON
				0x00, // IOCON
				0x3F, // GPPUA Enable pull-ups on buttons
				0x00, // GPPUB
				0x00, // INTFA
				0x00, // INTFB
				0x00, // INTCAPA
				0x00, // INTCAPB
				(byte) portA, // GPIOA
				(byte) portB, // GPIOB
				(byte) portA, // OLATA 0 on all outputs; side effect of
				(byte) portB // OLATB turning on R+G+B backlight LEDs.
		};
		write(0, registers, 0, registers.length);

		// Switch to Bank 1 and disable sequential operation.
		// From this point forward, the register addresses do NOT match
		// the list immediately above. Instead, use the constants defined
		// at the start of the class. Also, the address register will no
		// longer increment automatically after this -- multi-byte
		// operations must be broken down into single-byte calls.
		write(MCP23017_IOCON_BANK0, (byte) 0xA0);

		write(0x33); // Init
		write(0x32); // Init
		write(0x28); // 2 line 5x8 matrix
		write(LCD_CLEARDISPLAY);
		write(LCD_CURSORSHIFT | displayShift);
		write(LCD_ENTRYMODESET | displayMode);
		write(LCD_DISPLAYCONTROL | displayControl);
		write(LCD_RETURNHOME);
	}

	private synchronized void write(int i, byte[] registers, int j, int length) throws IOException {
		i2cDevice.write(i, registers, j, length);
	}

	private synchronized void write(int bank, byte b) throws IOException {
		i2cDevice.write(bank, b);
	}

	private synchronized void write(int value) throws IOException {
		waitOnLCDBusyFlag();
		int bitmask = portB & 0x01; // Mask out PORTB LCD control bits

		byte[] data = out4(bitmask, value);
		i2cDevice.write(MCP23017_GPIOB, data, 0, 4);
		portB = data[3];

		// If a poll-worthy instruction was issued, reconfigure D7
		// pin as input to indicate need for polling on next call.
		if (value == LCD_CLEARDISPLAY || value == LCD_RETURNHOME) {
			ddrB |= 0x10;
			i2cDevice.write(MCP23017_IODIRB, (byte) ddrB);
		}
	}

	private synchronized void waitOnLCDBusyFlag() throws IOException {
		// The speed of LCD accesses is inherently limited by I2C through the
		// port expander. A 'well behaved program' is expected to poll the
		// LCD to know that a prior instruction completed. But the timing of
		// most instructions is a known uniform 37 ms. The enable strobe
		// can't even be twiddled that fast through I2C, so it's a safe bet
		// with these instructions to not waste time polling (which requires
		// several I2C transfers for reconfiguring the port direction).
		// The D7 pin is set as input when a potentially time-consuming
		// instruction has been issued (e.g. screen clear), as well as on
		// startup, and polling will then occur before more commands or data
		// are issued.

		// If pin D7 is in input state, poll LCD busy flag until clear.
		if ((ddrB & 0x10) != 0) {
			int lo = (portB & 0x01) | 0x40;
			int hi = lo | 0x20; // E=1 (strobe)
			i2cDevice.write(MCP23017_GPIOB, (byte) lo);
			while (true) {
				i2cDevice.write((byte) hi); // Strobe high (enable)
				int bits = i2cDevice.read(); // First nybble contains busy state
				i2cDevice.write(MCP23017_GPIOB, new byte[] { (byte) lo, (byte) hi, (byte) lo }, 0, 3); // Strobe
																										// low,
																										// high,
																										// low.
																										// Second
																										// nybble
																										// (A3)
																										// is
																										// ignored.
				if ((bits & 0x02) == 0)
					break; // D7=0, not busy
			}
			portB = lo;
			ddrB &= 0xEF; // Polling complete, change D7 pin to output
			i2cDevice.write(MCP23017_IODIRB, (byte) ddrB);
		}
	}

	private byte[] out4(int bitmask, int value) {
		int hi = bitmask | SHIFT_REVERSE[value >> 4];
		int lo = bitmask | SHIFT_REVERSE[value & 0x0F];

		return new byte[] { (byte) (hi | 0x20), (byte) hi, (byte) (lo | 0x20), (byte) lo };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#setText(java.lang.String)
	 */
	@Override
	public synchronized void setText(String s) throws IOException {
		String[] rowStrings = s.split("\n");
		for (int i = 0; i < rowStrings.length; i++) {
			setText(i, pad(rowStrings[i]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#setText(int, java.lang.String)
	 */
	@Override
	public synchronized void setText(int row, String string) throws IOException {
		setCursorPosition(row, 0);
		internalWrite(string);
	}

	private void internalWrite(String s) throws IOException {
		int sLen = s.length();
		int bytesLen = 4 * sLen;
		if (sLen < 1) {
			return;
		}

		waitOnLCDBusyFlag();
		int bitmask = portB & 0x01; // Mask out PORTB LCD control bits
		bitmask |= 0x80; // Set data bit

		byte[] bytes = new byte[4 * sLen];
		for (int i = 0; i < sLen; i++) {
			byte[] data = out4(bitmask, s.charAt(i));
			for (int j = 0; j < 4; j++) {
				bytes[(i * 4) + j] = data[j];
			}
		}
		write(MCP23017_GPIOB, bytes, 0, bytesLen);
		portB = bytes[bytesLen - 1];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#setCursorPosition(int, int)
	 */
	@Override
	public synchronized void setCursorPosition(int row, int column) throws IOException {
		write(LCD_SETDDRAMADDR | (column + ROW_OFFSETS[row]));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#stop()
	 */
	@Override
	public synchronized void stop() throws IOException {
		portA = 0xC0; // Turn off LEDs on the way out
		portB = 0x01;
		sleep(2);
		write(MCP23017_IOCON_BANK1, (byte) 0);
		byte[] registers = { 0x3F, // IODIRA
				(byte) ddrB, // IODIRB
				0x0, // IPOLA
				0x0, // IPOLB
				0x0, // GPINTENA
				0x0, // GPINTENB
				0x0, // DEFVALA
				0x0, // DEFVALB
				0x0, // INTCONA
				0x0, // INTCONB
				0x0, // IOCON
				0x0, // IOCON
				0x3F, // GPPUA
				0x0, // GPPUB
				0x0, // INTFA
				0x0, // INTFB
				0x0, // INTCAPA
				0x0, // INTCAPB
				(byte) portA, // GPIOA
				(byte) portB, // GPIOB
				(byte) portA, // OLATA
				(byte) portB // OLATB
		};
		write(0, registers, 0, registers.length);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#clear()
	 */
	@Override
	public synchronized void clear() throws IOException {
		write(LCD_CLEARDISPLAY);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#home()
	 */
	@Override
	public synchronized void home() throws IOException {
		write(LCD_RETURNHOME);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#setCursorEnabled(boolean)
	 */
	@Override
	public synchronized void setCursorEnabled(boolean enable) throws IOException {
		if (enable) {
			displayControl |= LCD_CURSORON;
			write(LCD_DISPLAYCONTROL | displayControl);
		} else {
			displayControl &= ~LCD_CURSORON;
			write(LCD_DISPLAYCONTROL | displayControl);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#isCursorEnabled()
	 */
	@Override
	public synchronized boolean isCursorEnabled() {
		return (displayControl & LCD_CURSORON) > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#setDisplayEnabled(boolean)
	 */
	@Override
	public synchronized void setDisplayEnabled(boolean enable) throws IOException {
		if (enable) {
			displayControl |= LCD_DISPLAYON;
			write(LCD_DISPLAYCONTROL | displayControl);
		} else {
			displayControl &= ~LCD_DISPLAYON;
			write(LCD_DISPLAYCONTROL | displayControl);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#isDisplayEnabled()
	 */
	@Override
	public synchronized boolean isDisplayEnabled() {
		return (displayControl & LCD_DISPLAYON) > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#setBlinkEnabled(boolean)
	 */
	@Override
	public synchronized void setBlinkEnabled(boolean enable) throws IOException {
		if (enable) {
			displayControl |= LCD_BLINKON;
			write(LCD_DISPLAYCONTROL | displayControl);
		} else {
			displayControl &= ~LCD_BLINKON;
			write(LCD_DISPLAYCONTROL | displayControl);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#isBlinkEnabled()
	 */
	@Override
	public synchronized boolean isBlinkEnabled() {
		return (displayControl & LCD_BLINKON) > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#setBacklight(se.hirt.pi.adafruitlcd.LCD.
	 * Color)
	 */
	@Override
	public synchronized void setBacklight(Color color) throws IOException {
		int c = ~color.getValue();
		portA = (portA & 0x3F) | ((c & 0x03) << 6);
		portB = (portB & 0xFE) | ((c & 0x04) >> 2);
		// Has to be done as two writes because sequential operation is off.
		i2cDevice.write(MCP23017_GPIOA, (byte) portA);
		i2cDevice.write(MCP23017_GPIOB, (byte) portB);
		this.color = color;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * se.hirt.pi.adafruitlcd.ILCD#scrollDisplay(se.hirt.pi.adafruitlcd.LCD.
	 * Direction)
	 */
	@Override
	public synchronized void scrollDisplay(Direction direction) throws IOException {
		if (direction == Direction.LEFT) {
			displayShift = LCD_DISPLAYMOVE | LCD_MOVELEFT;
			write(LCD_CURSORSHIFT | displayShift);
		} else {
			displayShift = LCD_DISPLAYMOVE | LCD_MOVERIGHT;
			write(LCD_CURSORSHIFT | displayShift);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * se.hirt.pi.adafruitlcd.ILCD#setTextFlowDirection(se.hirt.pi.adafruitlcd.
	 * LCD.Direction)
	 */
	@Override
	public synchronized void setTextFlowDirection(Direction direction) throws IOException {
		if (direction == Direction.LEFT) {
			// This is for text that flows right to left
			displayMode &= ~LCD_ENTRYLEFT;
			write(LCD_ENTRYMODESET | displayMode);
		} else {
			// This is for text that flows left to right
			displayMode |= LCD_ENTRYLEFT;
			write(LCD_ENTRYMODESET | displayMode);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#setAutoScrollEnabled(boolean)
	 */
	@Override
	public synchronized void setAutoScrollEnabled(boolean enable) throws IOException {
		if (enable) {
			// This will 'right justify' text from the cursor
			displayMode |= LCD_ENTRYSHIFTINCREMENT;
			write(LCD_ENTRYMODESET | displayMode);
		} else {
			// This will 'left justify' text from the cursor
			displayMode &= ~LCD_ENTRYSHIFTINCREMENT;
			write(LCD_ENTRYMODESET | displayMode);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#isAutoScrollEnabled()
	 */
	@Override
	public synchronized boolean isAutoScrollEnabled() {
		return (displayControl & LCD_ENTRYSHIFTINCREMENT) > 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#isButtonPressed(se.hirt.pi.adafruitlcd.
	 * Button)
	 */
	@Override
	public synchronized boolean isButtonPressed(Button button) throws IOException {
		return ((read(MCP23017_GPIOA) >> button.getPin()) & 1) > 0;
	}

	private synchronized int read(int bank) throws IOException {
		return i2cDevice.read(bank);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see se.hirt.pi.adafruitlcd.ILCD#buttonsPressedBitmask()
	 */
	@Override
	public synchronized int buttonsPressedBitmask() throws IOException {
		return read(MCP23017_GPIOA) & 0x1F;
	}

	@Override
	public synchronized Color getBacklight() throws IOException {
		// Should probably read the registers instead of caching...
		return color;
	}

	@Override
	public synchronized void reset() throws IOException {
		initialize();
	}

	@Override
	public void createChar(int location, byte[] pattern) throws IOException {
		if (location < 0 || location > 7) {
			throw new IllegalArgumentException("Location should be between 0 and 7, value supplied is invalid: " + location);
		}
		if (pattern.length != 8) {
			throw new IllegalArgumentException("Pattern length should be 8, array supplied has invalid length: " + pattern.length);
		}

		// Send ccgram update command
		location &= 0x7; // Only position 0..7 are allowed
		int command = LCD_SETCGRAMADDR | (location << 3);
		write(command);

		// Send custom character definition
		internalWrite(new String(pattern));
	}

	private final static String pad(String inputString) {
		// NOTE(Marcus/Aug 30, 2017): The VRAM IIRC is 40, but I'm
		// just assuming that people will not use this with scroll (need to
		// write less). Change if this makes someone unhappy. We could also just
		// clear before writing, but then we get flicker if we, for example,
		// want to write the same characters on a certain line.
		for (int i = inputString.length(); i < 16; i++) {
			inputString += " ";
		}
		return inputString;
	}
}
