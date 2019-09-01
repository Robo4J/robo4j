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

package com.robo4j.units.rpi.led;

import com.robo4j.hw.rpi.i2c.adafruitbackpack.PackElement;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LEDBackpackMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private final LEDBackpackMessageType type;
	private List<PackElement> elements = new ArrayList<>();

	public LEDBackpackMessage() {
		this.type = LEDBackpackMessageType.CLEAR;
	}

	public LEDBackpackMessage(LEDBackpackMessageType type) {
		this.type = type;
	}

	public LEDBackpackMessageType getType() {
		return type;
	}

	public List<PackElement> getElements() {
		return elements;
	}

	public void addElement(PackElement element) {
		this.elements.add(element);
	}

	public void setElements(Collection<PackElement> elements) {
		this.elements.addAll(elements);
	}

	@Override
	public String toString() {
		return "LEDBackpackMessage{" + "elements=" + elements + '}';
	}
}
