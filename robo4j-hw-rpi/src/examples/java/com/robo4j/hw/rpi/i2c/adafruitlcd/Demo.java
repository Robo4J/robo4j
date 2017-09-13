/* * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner *  * Robo4J is free software: you can redistribute it and/or modify * it under the terms of the GNU General Public License as published by * the Free Software Foundation, either version 3 of the License, or * (at your option) any later version. * * Robo4J is distributed in the hope that it will be useful, * but WITHOUT ANY WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the * GNU General Public License for more details. * * You should have received a copy of the GNU General Public License * along with Robo4J. If not, see <http://www.gnu.org/licenses/>. */package com.robo4j.hw.rpi.i2c.adafruitlcd;import java.io.IOException;import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.RealLcd;import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.RealLcd.Direction;/** * Here is a demonstration of things that can be done with the LCD shield. Run * this as a stand alone java program. Do not forget to have sudo rights. *  * @author Marcus Hirt (@hirt) * @author Miroslav Wengner (@miragemiko) */public class Demo {	private static final LCDDemo[] TESTS = { new HelloWorldDemo(),			new ScrollDemo(), new CursorDemo(), new DisplayDemo(),			new ColorDemo(), new AutoScrollDemo(), new ExitDemo() };	private static int currentTest = -1;	public static void main(String[] args) throws IOException, UnsupportedBusNumberException {		final AdafruitLcd lcd = LcdFactory.createLCD();		lcd.setText("LCD Test!\nPress up/down...");		ButtonPressedObserver observer = new ButtonPressedObserver(lcd);		observer.addButtonListener(new ButtonListener() {			@Override			public void onButtonPressed(Button button) {				try {					switch (button) {					case UP:						currentTest = --currentTest < 0 ? 0 : currentTest;						lcd.clear();						lcd.setText(String.format("#%d:%s\nPress Sel to run!",								currentTest, TESTS[currentTest].getName()));						break;					case DOWN:						currentTest = ++currentTest > (TESTS.length - 1) ? TESTS.length - 1								: currentTest;						lcd.clear();						lcd.setText(String.format("#%d:%s\nPress Sel to run!",								currentTest, TESTS[currentTest].getName()));						break;					case RIGHT:						lcd.scrollDisplay(Direction.LEFT);						break;					case LEFT:						lcd.scrollDisplay(Direction.RIGHT);						break;					case SELECT:						runTest(currentTest);						break;					default:						lcd.clear();						lcd.setText(String.format(								"Button %s\nis not in use...",								button.toString()));					}				} catch (IOException e) {					handleException(e);				}			}			private void runTest(int currentTest) {				LCDDemo test = TESTS[currentTest];				System.out.println("Running test " + test.getName());				try {					test.run(lcd);				} catch (IOException e) {					handleException(e);				}			}		});		System.out.println("Press enter to quit!");		System.in.read();		lcd.stop();	}	private static void handleException(IOException e) {		System.out.println("Problem talking to LCD! Exiting!");		e.printStackTrace();		System.exit(2);	}}