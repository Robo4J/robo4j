/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This CameraUnitCommandEnum.java  is part of robo4j.
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

package com.robo4j.core.command;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.robo4j.core.enums.RoboHardwareEnumI;
import com.robo4j.core.enums.RoboTargetEnumI;

/**
 * @pa
 * @author Miro Wengner (@miragemiko)
 * @since 18.01.2017
 */
public enum OneServoUnitCommandEnum implements RoboUnitCommand, RoboHardwareEnumI<CommandTypeEnum>, RoboTargetEnumI<CommandTargetEnum> {

    //@formatter:off
    FRONT_EXIT  (0, "exit"),
    FRONT_INIT  (1, "init"),
    FRONT_LEFT	(2, "front_left"),
	FRONT_RIGHT	(3, "front_right"),
    ;
    //@formatter:on
    private int code;
    private String name;

    private volatile static Map<Integer, OneServoUnitCommandEnum> codeToCommandCodeMapping;

    OneServoUnitCommandEnum(int c, String name) {
        this.code = c;
        this.name = name;
    }

    public static OneServoUnitCommandEnum getCommand(String name) {
        if (codeToCommandCodeMapping == null) {
            codeToCommandCodeMapping = initMapping();
        }
        //@ formatter::off
        //TODO: can be generalised
        return codeToCommandCodeMapping.entrySet().stream()
                .filter(e -> e.getValue().getName().equals(name))
                .map(Map.Entry::getValue)
                .reduce(null, (e1, e2) -> e2);
        //@ formatter::on
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandTypeEnum getType() {
        return CommandTypeEnum.DIRECT;
    }

    @Override
    public CommandTargetEnum getTarget() {
        return CommandTargetEnum.FRONT_UNIT;
    }

    // Private Methods
    private static Map<Integer, OneServoUnitCommandEnum> initMapping() {
        return Arrays.stream(values()).collect(Collectors.toMap(OneServoUnitCommandEnum::getCode, e -> e));
    }

}
