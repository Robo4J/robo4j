/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This AdafruitLcdCommandEnum.java  is part of robo4j.
 * module: robo4j-commons
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

package com.robo4j.commons.command;

import static com.robo4j.commons.command.CommandTargetEnum.LCD_UNIT;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.robo4j.commons.enums.RoboHardwareEnum;

/**
 * @author Miro Wengner (@miragemiko)
 * @since 15.01.2017
 */
public enum AdafruitLcdCommandEnum implements RoboUnitCommand, RoboHardwareEnum<CommandTypeEnum, CommandTargetEnum> {

    // @formatter:off
	EXIT		    (0, 	LCD_UNIT, "exit"),
	BUTTON_SET 		(1, 	LCD_UNIT, "button_set"),
    BUTTON_RIGHT	(2, 	LCD_UNIT, "button_right"),
    BUTTON_LEFT		(3, 	LCD_UNIT, "button_left"),
    BUTTON_UP		(4, 	LCD_UNIT, "button_up"),
    BUTTON_DOWN     (5, 	LCD_UNIT, "button_down"),
	;
	// @formatter:on

    private volatile static Map<Integer, AdafruitLcdCommandEnum> codeToLcdCommandCodeMapping;

    private int code;
    private CommandTargetEnum target;
    private String name;

    AdafruitLcdCommandEnum(int c, CommandTargetEnum target, String name) {
        this.code = c;
        this.target = target;
        this.name = name;
    }

    public static AdafruitLcdCommandEnum getRequestValue(String name) {
        if (codeToLcdCommandCodeMapping == null) {
            codeToLcdCommandCodeMapping = initMapping();
        }
        return codeToLcdCommandCodeMapping.entrySet().stream().filter(e -> e.getValue().getName().equals(name))
                .map(Map.Entry::getValue).reduce(null, (e1, e2) -> e2);
    }

    public static AdafruitLcdCommandEnum getRequestCommand(CommandTargetEnum target, String name) {
        if (codeToLcdCommandCodeMapping == null) {
            codeToLcdCommandCodeMapping = initMapping();
        }

        return codeToLcdCommandCodeMapping.entrySet().stream().map(Map.Entry::getValue)
                .filter(v -> v.getTarget().equals(target)).filter(v -> v.getName().equals(name))
                .reduce(null, (e1, e2) -> e2);
    }

    // Private Methods
    private static Map<Integer, AdafruitLcdCommandEnum> initMapping() {
        return Arrays.stream(values()).collect(Collectors.toMap(AdafruitLcdCommandEnum::getCode, e -> e));
    }

    public int getCode() {
        return code;
    }

    @Override
    public CommandTypeEnum getType() {
        return CommandTypeEnum.DIRECT;
    }

    @Override
    public String getName() {
        return name;
    }

    public CommandTargetEnum getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "AdafruitLcdCommandEnum{" + "code=" + code + ", target=" + target + ", name='" + name + '\'' + '}';
    }

}
