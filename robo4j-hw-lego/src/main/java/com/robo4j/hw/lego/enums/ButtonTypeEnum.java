/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.hw.lego.enums;

import com.robo4j.hw.lego.ILegoHardware;
import com.robo4j.hw.lego.wrapper.KeyWrapper;
import lejos.hardware.Button;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Lego Mindstorm available buttons
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum ButtonTypeEnum implements ILegoHardware<KeyWrapper> {

    //@formatter:off
	//          name        type
	ENTER		("enter",   new KeyWrapper(Button.ENTER)),
    LEFT		("left",    new KeyWrapper(Button.LEFT)),
    RIGHT		("right",   new KeyWrapper(Button.RIGHT)),
    ESCAPE	    ("escape",  new KeyWrapper(Button.ESCAPE)),
    UP          ("up",      new KeyWrapper(Button.UP)),
    DOWN        ("down",    new KeyWrapper(Button.DOWN))
	;
	//@formatter:on

    private static final Map<String, ButtonTypeEnum> internMapByName = initMapping();
    private final String name;
    private final KeyWrapper key;

    ButtonTypeEnum(String name, KeyWrapper key) {
        this.name = name;
        this.key = key;
    }

    public static ButtonTypeEnum getByName(String type) {
        return internMapByName.get(type);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public KeyWrapper getType() {
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
