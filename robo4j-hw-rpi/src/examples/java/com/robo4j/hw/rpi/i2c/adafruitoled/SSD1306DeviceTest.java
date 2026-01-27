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
package com.robo4j.hw.rpi.i2c.adafruitoled;

import com.robo4j.hw.rpi.i2c.adafruitoled.SSD1306Device.OLEDVariant;
import com.robo4j.hw.rpi.utils.GpioPin;

import java.awt.*;
import java.io.IOException;

import static com.robo4j.hw.rpi.lcd.StringUtils.STRING_SPACE;

/**
 * Example which prints Hello World and draws a little. It also shows the image
 * in a JFrame, so that it is easy to know what to expect.
 * <p>
 * Takes one argument, the number of lines of your specific device (32 or 64).
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class SSD1306DeviceTest {
    private static final String DEFAULT_LINES = "32";
    private static final GpioPin RESET_PIN = GpioPin.GPIO_25;

    /**
     * Start the example with either 32 or 64 as argument to select the number
     * of lines. Will default to 32.
     */
    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "true");
        System.out.println("Headless:" + System.getProperty("java.awt.headless"));
        String lines = DEFAULT_LINES;
        if (args.length > 0) {
            lines = args[0];
        }

        OLEDVariant variant = lines.equals(DEFAULT_LINES) ? OLEDVariant.Type128x32 : OLEDVariant.Type128x64;
        final SSD1306Device oled = new SSD1306Device(variant, RESET_PIN);

        System.out.println("Running OLED device example for " + variant + " with reset pin " + RESET_PIN + ".");
        System.out.println("If the number of lines do not match your device,");
        System.out.println("please add the number of lines as the first argument!");

        String text = args.length > 0 ? String.join(STRING_SPACE, args) : "Hello Maxi!";
        Graphics2D gc = oled.getGraphicsContext();
        gc.setColor(Color.white);
        gc.setBackground(Color.black);
        gc.clearRect(0, 0, 127, 31);
        gc.drawLine(0, 0, 127, 31);
        gc.drawString(text, 0, 30);
        gc.setBackground(Color.white);
        gc.fillOval(127 - 16, -16, 32, 32);
        oled.pushImage();
        System.out.println("There is nothing");

        // TODO : create optional possibility to use JFrame as an output
//		JFrame frame = new JFrame();
//		frame.addWindowListener(new WindowAdapter() {
//
//			@Override
//			public void windowClosing(WindowEvent e) {
//				try {
//					oled.setEnabled(false);
//				} catch (IOException e1) {
//					e1.printStackTrace();
//					// TODO: is it possible ?
//				} finally {
//					System.exit(0);
//				}
//			}
//		});
//		frame.setSize(256, 256);
//		frame.getContentPane().add(new JLabel(new ImageIcon(oled.getImage())));
//		frame.setVisible(true);
        System.out.println("Press <Enter> to quit!");
        System.in.read();
        oled.setEnabled(false);
//		frame.setVisible(false);
//		frame.dispose();
    }

}
