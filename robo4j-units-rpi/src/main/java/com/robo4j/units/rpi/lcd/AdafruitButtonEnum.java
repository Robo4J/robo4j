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
package com.robo4j.units.rpi.lcd;

import com.robo4j.hw.rpi.i2c.adafruitlcd.Button;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This enum is sent as messages from the buttons of the Adafruit LCD.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @see AdafruitButtonUnit
 */
public enum AdafruitButtonEnum {

    // @formatter:off
	SELECT 			(com.robo4j.hw.rpi.i2c.adafruitlcd.Button.SELECT, "S", "select"),
	LEFT		    (Button.LEFT, "L", "left"),
	RIGHT		    (Button.RIGHT, "R", "right"),
	UP      		(Button.UP, "U", "up"),
	DOWN    		(Button.DOWN, "D", "down"),
    UNKNOWN         (null, "", "");
	// @formatter:on

    private static final Map<Button, AdafruitButtonEnum> buttonToEnum = initMapping();
    private final Button button;
    private final String name;
    private final String text;

    AdafruitButtonEnum(Button button, String name, String text) {
        this.button = button;
        this.name = name;
        this.text = text;
    }

    private static Map<Button, AdafruitButtonEnum> initMapping() {
        return Stream.of(values()).collect(Collectors.toMap(AdafruitButtonEnum::getButton, e -> e));
    }


    //@formatter:off
    public static AdafruitButtonEnum getByName(String def) {
        return buttonToEnum.values().stream()
                .filter(e -> e.getName().equals(def.toUpperCase()))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public static AdafruitButtonEnum getByText(String text) {
        return buttonToEnum.values().stream()
                .filter(e -> e.getText().equals(text))
                .findFirst()
                .orElse(UNKNOWN);
    }
    //@formatter:on

    public Button getButton() {
        return button;
    }

    public String getName() {
        return name;
    }

    public String getText(){
        return text;
    }

    public Set<String> commandNames() {
        //@formatter:off
        return Stream.of(values())
                .map(AdafruitButtonEnum::getText)
                .collect(Collectors.toSet());
        //@formatter:on
    }

    @Override
    public String toString() {
        return "AdaruitButtonPlateEnum{" +
                "button=" + getButton() +
                ", name='" + name + '\'' +
                '}';
    }

}
