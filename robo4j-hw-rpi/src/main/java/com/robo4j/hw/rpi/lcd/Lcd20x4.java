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
package com.robo4j.hw.rpi.lcd;

import com.pi4j.Pi4J;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalOutputConfig;
import com.pi4j.io.gpio.digital.DigitalOutputConfigBuilder;
import com.pi4j.io.gpio.digital.DigitalState;
import com.robo4j.hw.rpi.utils.GpioPin;

import java.util.concurrent.TimeUnit;

/**
 * Hardware support for the 20x4 LCD module.
 * 
 * See
 * http://www.raspberrypi-spy.co.uk/2012/08/20x4-lcd-module-control-using-python/
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Lcd20x4 {
	private final static int CHAR_WIDTH = 20;

	//@formatter:off
	/**
	 * From the webpage:
	 *
		# The wiring for the LCD is as follows:
		# 1 : GND
		# 2 : 5V
		# 3 : Contrast (0-5V)*
		# 4 : RS (Register Select)
		# 5 : R/W (Read Write)       - GROUND THIS PIN
		# 6 : Enable or Strobe
		# 7 : Data Bit 0             - NOT USED
		# 8 : Data Bit 1             - NOT USED
		# 9 : Data Bit 2             - NOT USED
		# 10: Data Bit 3             - NOT USED
		# 11: Data Bit 4
		# 12: Data Bit 5
		# 13: Data Bit 6
		# 14: Data Bit 7
		# 15: LCD Backlight +5V**
		# 16: LCD Backlight GND
	**/
	//@formatter:on
//	private final GpioPinDigitalOutput gpioE;
//	private final GpioPinDigitalOutput gpioRS;
//	private final GpioPinDigitalOutput gpioD4;
//	private final GpioPinDigitalOutput gpioD5;
//	private final GpioPinDigitalOutput gpioD6;
//	private final GpioPinDigitalOutput gpioD7;
//	private final GpioPinDigitalOutput gpioOn;
	private final DigitalOutput gpioRS;
	private final DigitalOutput gpioE;
	private final DigitalOutput gpioD4;
	private final DigitalOutput gpioD5;
	private final DigitalOutput gpioD6;
	private final DigitalOutput gpioD7;
	private final DigitalOutput gpioOn;

	// Delay in nanos
	private final static int E_DELAY = (int) TimeUnit.MICROSECONDS.toNanos(500);

	private enum Mode {
		CMD(false), CHAR(true);

		private final boolean sendValue;

		Mode(boolean sendValue) {
			this.sendValue = sendValue;
		}

		public boolean getSendValue() {
			return sendValue;
		}
	}

	public enum Alignment {
		LEFT, CENTER, RIGHT
	}

	/**
	 * Default constructor.
	 *
	 * Use this if you have used the default wiring in the example.
	 */
	public Lcd20x4() {
		this(GpioPin.GPIO_11, GpioPin.GPIO_10, GpioPin.GPIO_06, GpioPin.GPIO_05, GpioPin.GPIO_04, GpioPin.GPIO_01,
				GpioPin.GPIO_16);
	}

	/**
	 * Constructor.
	 * 
	 * Use this constructor if you want a customized wiring.
	 *
	 * @param pinRS
	 *            pin rs
	 * @param pinE
	 *            pin E
	 * @param pinD4
	 *            pin D4
	 * @param pinD5
	 *            pin D5
	 * @param pinD6
	 *            pin D6
	 * @param pinD7
	 *            pin D7
	 * @param pinOn
	 *            ping 0n
	 */
	public Lcd20x4(GpioPin pinRS, GpioPin pinE, GpioPin pinD4, GpioPin pinD5, GpioPin pinD6, GpioPin pinD7, GpioPin pinOn) {
		var pi4jRpiContext = Pi4J.newAutoContext();
		var digitalOutputBuilder = DigitalOutput.newConfigBuilder(pi4jRpiContext);
		var configPinRS =createPinConfig(digitalOutputBuilder, pinRS, DigitalState.LOW);
		var configPinE =createPinConfig(digitalOutputBuilder, pinE, DigitalState.LOW);
		var configPinD4 =createPinConfig(digitalOutputBuilder, pinD4, DigitalState.LOW);
		var configPinD5 =createPinConfig(digitalOutputBuilder, pinD5, DigitalState.LOW);
		var configPinD6 =createPinConfig(digitalOutputBuilder, pinD6, DigitalState.LOW);
		var configPinD7 =createPinConfig(digitalOutputBuilder, pinD7, DigitalState.LOW);
		var configPinOn =createPinConfig(digitalOutputBuilder, pinOn, DigitalState.LOW);

		gpioRS = pi4jRpiContext.dout().create(configPinRS);
		gpioE = pi4jRpiContext.dout().create(configPinE);
		gpioD4 = pi4jRpiContext.dout().create(configPinD4);
		gpioD5 = pi4jRpiContext.dout().create(configPinD5);
		gpioD6 = pi4jRpiContext.dout().create(configPinD6);
		gpioD7 = pi4jRpiContext.dout().create(configPinD7);
		gpioOn = pi4jRpiContext.dout().create(configPinOn);

//		GpioController gpio = GpioFactory.getInstance();
//		gpioRS = gpio.provisionDigitalOutputPin(pinRS, "RS", PinState.LOW);
//		gpioE = gpio.provisionDigitalOutputPin(pinE, "E", PinState.LOW);
//		gpioD4 = gpio.provisionDigitalOutputPin(pinD4, "D4", PinState.LOW);
//		gpioD5 = gpio.provisionDigitalOutputPin(pinD5, "D5", PinState.LOW);
//		gpioD6 = gpio.provisionDigitalOutputPin(pinD6, "D6", PinState.LOW);
//		gpioD7 = gpio.provisionDigitalOutputPin(pinD7, "D7", PinState.LOW);
//		gpioOn = gpio.provisionDigitalOutputPin(pinOn, "On", PinState.HIGH);
		initialize();
	}

	private DigitalOutputConfig createPinConfig(DigitalOutputConfigBuilder builder, GpioPin pin,  DigitalState state){
		return 	builder.address(pin.address()).onState(state).build();
	}

	private void initialize() {
		sendByte(0x33, Mode.CMD);
		sendByte(0x32, Mode.CMD);
		sendByte(0x06, Mode.CMD);
		sendByte(0x0C, Mode.CMD);
		sendByte(0x28, Mode.CMD);
		sendByte(0x01, Mode.CMD);
		sleep(E_DELAY);
	}

	private void sleep(int delayNanos) {
		try {
			Thread.sleep(0l, delayNanos);
		} catch (InterruptedException e) {
			// Do not care
		}
	}

	private void sendByte(int bits, Mode cmd) {
		System.out.println(bits);
		gpioRS.setState(cmd.getSendValue());
		dataLow();

		// Send the high bits
		if ((bits & 0x10) == 0x10) {
			gpioD4.setState(true);
		}

		if ((bits & 0x20) == 0x20) {
			gpioD5.setState(true);
		}

		if ((bits & 0x40) == 0x40) {
			gpioD6.setState(true);
		}

		if ((bits & 0x80) == 0x80) {
			gpioD7.setState(true);
		}
		toggleEnable();

		// Send the low bits
		dataLow();
		if ((bits & 0x01) == 0x01) {
			gpioD4.setState(true);
		}

		if ((bits & 0x02) == 0x02) {
			gpioD5.setState(true);
		}

		if ((bits & 0x04) == 0x04) {
			gpioD6.setState(true);
		}

		if ((bits & 0x08) == 0x08) {
			gpioD7.setState(true);
		}
		toggleEnable();
	}

	private void dataLow() {
		gpioD4.setState(false);
		gpioD5.setState(false);
		gpioD6.setState(false);
		gpioD7.setState(false);
	}

	private void toggleEnable() {
		sleep(E_DELAY);
		gpioE.setState(true);
		sleep(E_DELAY);
		gpioE.setState(false);
		sleep(E_DELAY);
	}

	public void sendMessage(int line, String text, Alignment alignment) {
		switch (alignment) {
		case RIGHT:
			text = StringUtils.rightFormat(text, CHAR_WIDTH);
			break;
		case CENTER:
			text = StringUtils.centerFormat(text, CHAR_WIDTH);
			break;
		default:
			break;
		}
		sendByte(line, Mode.CMD);
		for (int i = 0; i < Math.min(text.length(), CHAR_WIDTH); i++) {
			sendByte(text.charAt(i), Mode.CHAR);
		}

	}

	public void enableBacklight(boolean enable) {
		gpioOn.setState(enable);
	}

	public void clearDisplay() {
		sendByte(0x01, Mode.CMD);
	}
}
