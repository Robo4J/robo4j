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
 * LedBackpackMessage is the container for backpack messages
 *
 * @param <T> represents type of used messages see {@link PackElement}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class LedBackpackMessages<T extends PackElement> implements Serializable {
    private static final long serialVersionUID = 1L;

	private final LedBackpackMessageType type;
	private List<T> elements = new ArrayList<>();

	public LedBackpackMessages() {
		this.type = LedBackpackMessageType.CLEAR;
	}

	public LedBackpackMessages(LedBackpackMessageType type) {
		this.type = type;
	}

	public LedBackpackMessageType getType() {
		return type;
	}

	public List<T> getElements() {
		return elements;
	}

	public void addElement(T element){
		this.elements.add(element);
	}

	public void setElements(Collection<T> elements) {
		this.elements.addAll(elements);
	}

	@Override
	public String toString() {
		return "LEDBackpackMessage{" + "elements=" + elements + '}';
	}
}
