/*
 * Copyright (C) 2014-2017. Miroslav Wengner, Marcus Hirt
 * This SSD1306DeviceTest.java  is part of robo4j.
 * module: robo4j-hw-rpi
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.hw.rpi.i2c.adafruitoled;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.pi4j.io.gpio.RaspiPin;
import com.robo4j.hw.rpi.i2c.adafruitoled.SSD1306Device.OLEDVariant;

/**
 * Example which prints Hello World and draws a little. It also shows the image
 * in a JFrame, so that it is easy to know what to expect.
 * 
 * @author Marcus Hirt
 */
public class SSD1306DeviceTest {
	public static void main(String[] args) throws IOException {
		final SSD1306Device oled = new SSD1306Device(OLEDVariant.Type128x32,
				RaspiPin.GPIO_25);

		Graphics2D gc = oled.getGraphicsContext();
		gc.setColor(Color.white);
		gc.setBackground(Color.black);
		gc.clearRect(0, 0, 127, 31);
		gc.drawLine(0, 0, 127, 31);
		gc.drawString("Hello World!", 0, 32);
		gc.setBackground(Color.white);
		gc.fillOval(127 - 16, -16, 32, 32);
		oled.pushImage();

		JFrame frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					oled.setEnabled(false);
				} catch (IOException e1) {
					e1.printStackTrace();
				//TODO: is it possible ?
				} finally {
					System.exit(0);
				}
			}
		});
		frame.setSize(256, 256);
		frame.getContentPane().add(new JLabel(new ImageIcon(oled.getImage())));
		frame.setVisible(true);
	}

}
