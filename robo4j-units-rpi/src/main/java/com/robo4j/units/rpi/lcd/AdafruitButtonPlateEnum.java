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
package com.robo4j.units.rpi.lcd;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.robo4j.core.enums.IRoboHardwareEnum;

/**
 * Adafruit Button Plat possible control buttons
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum AdafruitButtonPlateEnum implements IRoboHardwareEnum<Integer> {

    // @formatter:off
	SELECT 			(0, "S", "select"),
	LEFT		    (1, "L", "left"),
	RIGHT		    (2, "R", "right"),
	UP      		(3, "U", "up"),
	DOWN    		(4, "D", "down"),
	;
	// @formatter:on

    private volatile static Map<Integer, AdafruitButtonPlateEnum> defToCommandTargetMapping;
    private int type;
    private String name;
    private String text;

    AdafruitButtonPlateEnum(int type, String name, String text) {
        this.type = type;
        this.name = name;
        this.text = text;
    }

    private static Map<Integer, AdafruitButtonPlateEnum> initMapping() {
        return Stream.of(values()).collect(Collectors.toMap(AdafruitButtonPlateEnum::getType, e -> e));
    }

    //@formatter:off
    public static AdafruitButtonPlateEnum getByName(String def) {
        if (defToCommandTargetMapping == null)
            defToCommandTargetMapping = initMapping();

        return defToCommandTargetMapping.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(e -> e.getName().equals(def.toUpperCase()))
                .findFirst().get();
    }

    public static AdafruitButtonPlateEnum getByText(String text) {
        if (defToCommandTargetMapping == null)
            defToCommandTargetMapping = initMapping();
        return defToCommandTargetMapping.entrySet().stream()
                .map(Map.Entry::getValue)
                .filter(e -> e.getText().equals(text))
                .findFirst().get();
    }
    //@formatter:on

    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getText(){
        return text;
    }

    @Override
    public String toString() {
        return "AdaruitButtonPlateEnum{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
