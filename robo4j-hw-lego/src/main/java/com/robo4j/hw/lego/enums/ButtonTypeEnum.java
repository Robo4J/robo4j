/*
 * Copyright (C) 2014, 2017. Miroslav Wengner, Marcus Hirt
 * This ButtonTypeEnum.java  is part of robo4j.
 * module: robo4j-hw-lego
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

package com.robo4j.hw.lego.enums;

import com.robo4j.hw.lego.ILegoHardware;
import lejos.hardware.Button;
import lejos.hardware.Key;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Lego Mindstorm available buttons
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum ButtonTypeEnum implements ILegoHardware<Key> {

    //@formatter:off
	//          name        type
	ENTER		("enter",   Button.ENTER),
    LEFT		("left",    Button.LEFT),
    RIGHT		("right",   Button.RIGHT),
    ESCAPE	    ("escape",  Button.ESCAPE),
    UP          ("up",      Button.UP),
    DOWN        ("down",    Button.DOWN)
	;
	//@formatter:on

    private volatile static Map<String, ButtonTypeEnum> internMapByName;
    private String name;
    private Key key;

    ButtonTypeEnum(String name, Key key) {
        this.name = name;
        this.key = key;
    }

    public static ButtonTypeEnum getByName(String type) {
        if (internMapByName == null) {
            internMapByName = initMapping();
        }
        return internMapByName.get(type);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Key getType() {
        return key;
    }

    public static void applyLEDPattern(int pattern){
        Button.LEDPattern(pattern);
    }

    //Private Methods
    private static Map<String, ButtonTypeEnum> initMapping() {
        return Stream.of(values()).collect(Collectors.toMap(ButtonTypeEnum::getName, e -> e));
    }

    @Override
    public String toString() {
        return "ButtonTypeEnum{" +
                "name='" + name + '\'' +
                ", key=" + key +
                '}';
    }
}
