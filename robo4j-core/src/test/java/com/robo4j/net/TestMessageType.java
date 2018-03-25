/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.net;

import com.robo4j.RoboReference;

import java.io.Serializable;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

public class TestMessageType implements Serializable {
	private static final long serialVersionUID = 2L;
	private int number;
	private String text;
	private RoboReference<String> reference;
	
	public TestMessageType(int number, String text, RoboReference<String> reference) {
		this.number = number;
		this.text = text;
		this.reference = reference;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public RoboReference<String> getReference() {
		return reference;
	}
	
	public void setReference(RoboReference<String> reference) {
		this.reference = reference;
	}
	
	public String toString() {
		return "TestMessageType number=" + number + " text=" + text + " reference:" + String.valueOf(reference);
	}
	
}
