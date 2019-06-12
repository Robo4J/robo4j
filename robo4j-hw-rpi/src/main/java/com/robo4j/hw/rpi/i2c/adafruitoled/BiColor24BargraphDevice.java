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

package com.robo4j.hw.rpi.i2c.adafruitoled;

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.AbstractI2CDevice;

import java.io.IOException;

/**
 * https://learn.adafruit.com/adafruit-led-backpack/bi-color-24-bargraph
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class BiColor24BargraphDevice extends AbstractI2CDevice {

	private enum Color {
		//@formatter:off
        OFF     (0),
        RED     (1),
        YELLOW  (2),
        GREEN   (3)
        ;
        //@formatter:on

		private final int value;

		Color(int value) {
			this.value = value;
		}

		public static Color getByValue(int code) {
			for (Color r : values()) {
				if (code == r.value) {
					return r;
				}
			}
			return OFF;
		}

	}

	private static final int DEFAULT_I2C_ADDRESS = 0x70;
	private final int[] displaybuffer = new int[8]; // uint16_t

	public BiColor24BargraphDevice(int bus, int address) throws IOException {
		super(bus, address);
	}

	public BiColor24BargraphDevice() throws IOException {
		this(I2CBus.BUS_1, DEFAULT_I2C_ADDRESS);
	}

	void init() {

		for (int b = 0; b < 28; b++) {
			int colorNumber = b % 3 + 1;
			Color color = Color.getByValue(colorNumber);
			setBar(b, color);
		}

	}

	public void setBar(int bar, Color color) {
		int a, c;

		if (bar < 12) {
			c = (bar / 4) & 0xFFFF;
		} else {
			c = ((bar - 12) / 4) & 0xFFFF;
		}

		a = bar % 4 & 0xFFFF;
		if (bar >= 12) {
			a += 4 & 0xFFFF;
		}

		switch (color){
            case RED:
                // Turn on red LED.
                displaybuffer[c] |= _BV(a) & 0xFFFF;
                // Turn off green LED.
                displaybuffer[c] &= ~(_BV((a+8)& 0xFFFF) )& 0xFFFF;
                break;
            case YELLOW:
                // Turn on red and green LED.
                displaybuffer[c] |=  (_BV(a) & 0xFFFF)  | _BV((a+8)& 0xFFFF)& 0xFFFF;
                break;
            case GREEN:
                // Turn on green LED.
                displaybuffer[c] |= (_BV(a+8) & 0xFFFF);
                // Turn off red LED.
                displaybuffer[c] &= ~(_BV(a & 0xFFFF) & 0xFFFF) ;
                break;
            case OFF:
                // Turn off red and green LED.
                displaybuffer[c] &= ~(_BV(a) & 0xFFFF ) & ~(_BV((a+8) & 0xFFFF) & 0xFFFF);
                break;
            default:
                System.out.println("setBar ERROR: " + color);
                break;
        }
	}

	public void writeDisplay(){
	    for(int i=0; i < displaybuffer.length; i++){
        }
    }

	private int _BV(int i){
	    return (1 << i);
    }

}
