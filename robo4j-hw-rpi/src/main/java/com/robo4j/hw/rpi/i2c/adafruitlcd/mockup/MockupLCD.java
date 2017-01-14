/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This MockupLCD.java  is part of robo4j.
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
package com.robo4j.hw.rpi.i2c.adafruitlcd.mockup;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import com.pi4j.io.i2c.I2CBus;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Button;
import com.robo4j.hw.rpi.i2c.adafruitlcd.ButtonListener;
import com.robo4j.hw.rpi.i2c.adafruitlcd.ButtonPressedObserver;
import com.robo4j.hw.rpi.i2c.adafruitlcd.Color;
import com.robo4j.hw.rpi.i2c.adafruitlcd.ILCD;
import com.robo4j.hw.rpi.i2c.adafruitlcd.impl.RealLCD.Direction;

/**
 * Swing mockup for the LCD.
 * 
 * @author Marcus Hirt
 */
public class MockupLCD implements ILCD {
	private static final int DDRAM_SIZE = 40;
	private volatile int cursorColumn;
	private volatile int cursorRow;
	private volatile int maskVal;
	private final char[] FIRST_ROW = new char[DDRAM_SIZE];
	private final char[] SECOND_ROW = new char[DDRAM_SIZE];

	private final int bus;
	private final int address;
	private final JTextArea textArea = new JTextArea(2, 16);
	private JFrame frame;
	private Color color = Color.WHITE;
	
	public MockupLCD() {
		// This seems to be the default for AdaFruit 1115.
		this(I2CBus.BUS_1, 0x20);
	}

	public MockupLCD(int bus, int address) {
		this.bus = bus;
		this.address = address;
		initialize();
	}

	private void initialize() {
		textArea.setEditable(false);
		frame = new JFrame(String.format("LCD %d@%xd", bus, address));
		frame.setSize(200, 150);
		frame.getContentPane().add(textArea, BorderLayout.CENTER);
		frame.getContentPane().add(createButtonArea(), BorderLayout.SOUTH);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void setText(String s) {
		String[] str = s.split("\n");
		for (int i = 0; i < str.length; i++) {
			setText(i, str[i]);
		}
	}

	public void setText(int row, String string) {
		setCursorPosition(row, 0);
		internalWrite(string);
	}

	private void internalWrite(String s) {
		char[] buffer = cursorRow == 0 ? FIRST_ROW : SECOND_ROW;
		for (int i = 0; i < s.length() && cursorColumn < DDRAM_SIZE; i++) {
			buffer[cursorColumn++] = s.charAt(i);
		}
		textArea.setText(createStringFromBuffers());
	}

	private String createStringFromBuffers() {
		return String.format("%16s\n%16s", new String(FIRST_ROW), new String(
				SECOND_ROW));
	}

	public void setCursorPosition(int row, int column) {
		cursorRow = row;
		cursorColumn = column;
	}

	public void stop() {
		frame.setVisible(false);
		frame.dispose();
	}

	public void clear() {
		Arrays.fill(FIRST_ROW, (char) 0);
		Arrays.fill(SECOND_ROW, (char) 0);		
	}

	public void home() {
	}

	public void setCursorEnabled(boolean enable) {
	}

	public boolean isCursorEnabled() {
		return false;
	}

	public void setDisplayEnabled(boolean enable) {
	}

	public boolean isDisplayEnabled() {
		return true;
	}

	public void setBlinkEnabled(boolean enable) {
	}

	public boolean isBlinkEnabled() {
		return false;
	}

	public void setBacklight(Color color) {
		this.color = color;
	}

	public void scrollDisplay(Direction direction) {
	}

	public void setTextFlowDirection(Direction direction) {
	}

	public void setAutoScrollEnabled(boolean enable) {
	}

	public boolean isAutoScrollEnabled() {
		return false;
	}

	public boolean isButtonPressed(Button button) {
		return button.isButtonPressed(maskVal);
	}

	public int buttonsPressedBitmask() {
		return maskVal;
	}

	public static void main(String[] args) {
		final MockupLCD lcd = new MockupLCD();
		lcd.clear();
		lcd.setText("Hello World!\nDone!");
		ButtonPressedObserver observer = new ButtonPressedObserver(lcd);
		observer.addButtonListener(new ButtonListener() {

			@Override
			public void onButtonPressed(Button button) {
				lcd.clear();
				lcd.setText("Pressed!\n" + button.toString());
			}
		});
	}

	private Component createButtonArea() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(createButton("Sel", 1), BorderLayout.CENTER);
		panel.add(createButton("Right", 2), BorderLayout.EAST);
		panel.add(createButton("Down", 4), BorderLayout.SOUTH);
		panel.add(createButton("Up", 8), BorderLayout.NORTH);
		panel.add(createButton("Left", 16), BorderLayout.WEST);
		return panel;
	}

	private Component createButton(String string, final int buttonMaskVal) {
		JButton button = new JButton(string);
		button.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				MockupLCD.this.maskVal = MockupLCD.this.maskVal & ~buttonMaskVal;
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				MockupLCD.this.maskVal = MockupLCD.this.maskVal | buttonMaskVal;
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		return button;
	}

	@Override
	public Color getBacklight() throws IOException {
		return color;
	}

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub
		
	}
}
