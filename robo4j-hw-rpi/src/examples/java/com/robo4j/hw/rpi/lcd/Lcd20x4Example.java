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
package com.robo4j.hw.rpi.lcd;

import java.io.IOException;

import com.robo4j.hw.rpi.lcd.Lcd20x4.Alignment;

/**
 * Demo for the 20x4 LCD.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class Lcd20x4Example {

	private static class Demo implements Runnable {
		private final Lcd20x4 lcd;
		private volatile boolean shouldRun = true;

		Demo(Lcd20x4 lcd) {
			this.lcd = lcd;
		}

		@Override
		public void run() {
			while (shouldRun) {
				lcd.sendMessage(1, "--------------------", Alignment.CENTER);
				lcd.sendMessage(2, "Rasbperry Pi", Alignment.CENTER);
				lcd.sendMessage(3, "Robo4J", Alignment.CENTER);
				lcd.sendMessage(4, "--------------------", Alignment.CENTER);
				sleep(3000);
				lcd.sendMessage(1, "--------------------", Alignment.CENTER);
				lcd.sendMessage(2, "This is a test", Alignment.CENTER);
				lcd.sendMessage(3, "20x4 LCD Module", Alignment.CENTER);
				lcd.sendMessage(4, "--------------------", Alignment.CENTER);
				sleep(3000);
			}
			lcd.clearDisplay();
			lcd.sendMessage(2, "Goodbye!", Alignment.CENTER);
		}

		private void sleep(int seconds) {
			if (shouldRun) {
				try {
					Thread.sleep(seconds);
				} catch (InterruptedException e) {
					// Don't care
				}
			}
		}

		public void quit() {
			shouldRun = false;
		}
	}

	public static void main(String[] args) throws IOException {
		Lcd20x4 lcd = new Lcd20x4();
		Demo demo = new Demo(lcd);
		Thread t = new Thread(demo, "LCD Demo Thread");
		t.start();
		System.out.println("Running LCD demo. Press enter to quit!");
		System.in.read();
		demo.quit();
	}
}
