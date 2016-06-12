/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoPlatformCommandEnum.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.core.platform.command;

import com.robo4j.core.bridge.command.CommandTypeEnum;
import com.robo4j.core.control.RoboCoreCommand;
import com.robo4j.core.control.RoboTypeCommand;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Supported direct plaform command
 * Created by miroslavkopecky on 26/09/14.
 */
public enum LegoPlatformCommandEnum implements RoboCoreCommand, RoboTypeCommand {

    //@formatter:off
    EXIT            (0, "exit"),
    LEFT            (1, "left"),
    RIGHT           (2, "right"),
    MOVE            (3, "move"),
    BACK            (4, "back"),
    STOP            (5, "stop"),
    INIT            (6, "init"),
    CLOSE           (7, "close"),
    MOVE_CYCLES     (8, "move_cycle"),
    MOVE_DISTANCE   (9, "move_distance"),
    BACK_CYCLES     (10, "back_cycle"),
    BACK_DISTANCE   (11, "back_distance"),
    LEFT_CYCLES     (12, "left_cycle"),
    RIGHT_CYCLES    (13, "right_cycle"),
    EMERGENCY_STOP  (14, "emergency_stop"),
    ;
    //@formatter:on

    private int code;
    private String name;

    private volatile static Map<Integer, LegoPlatformCommandEnum> codeToLegoCommandTypeMapping;
    private volatile static Map<String, LegoPlatformCommandEnum> codeToLegoCommandNameMapping;


    LegoPlatformCommandEnum(int c, String name){
        this.code = c;
        this.name = name;
    }

    private static void initMapping(){
        codeToLegoCommandTypeMapping = new HashMap<>();
        codeToLegoCommandNameMapping = new HashMap<>();
        for(LegoPlatformCommandEnum ct: values()){
            codeToLegoCommandTypeMapping.put(ct.getCode(), ct);
            codeToLegoCommandNameMapping.put(ct.getName(), ct);
        }
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


    public static LegoPlatformCommandEnum getCommand(int code){
        if(codeToLegoCommandTypeMapping == null)
            initMapping();

        return codeToLegoCommandTypeMapping.get(code);
    }

    public static LegoPlatformCommandEnum getCommand(String name){
        if(codeToLegoCommandNameMapping == null)
            initMapping();

        return codeToLegoCommandNameMapping.get(name);
    }

    @Override
    public String toString() {
        return "LegoPlatformCommandEnum{" +
                "code=" + code +
                ", name='" + name + '\'' +
                '}';
    }
}
