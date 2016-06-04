/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This CommandTypeEnum.java is part of robo4j.
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

package com.robo4j.core.bridge.command;

import com.robo4j.core.system.LegoSystemEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by miroslavkopecky on 24/04/16.
 */
public enum CommandTypeEnum implements LegoSystemEnum<Integer> {


    //@formatter:off
    BATCH            (0, "B".concat(CommandUtil.COMMAND_TYPE_CLOSE)),
    DIRECT           (1, "D".concat(CommandUtil.COMMAND_TYPE_CLOSE)),
    HAND             (2, "H".concat(CommandUtil.COMMAND_TYPE_CLOSE)),
    COMPLEX          (3, "C".concat(CommandUtil.COMMAND_TYPE_CLOSE)),
    ACTIVE           (4, "A".concat(CommandUtil.COMMAND_TYPE_CLOSE)),
    ;
    //@formatter:on

    private int code;
    private String def;

    private volatile static Map<String, CommandTypeEnum> defToCommandTypeMapping;

    CommandTypeEnum(int code, String def) {
        this.code = code;
        this.def = def;
    }

    @Override
    public Integer getType() {
        return code;
    }

    @Override
    public String getDesc() {
        return "Command = " + def;
    }

    public String getDef() {
        return def;
    }



    private static void initMapping(){
        defToCommandTypeMapping = new HashMap<>();
        for(CommandTypeEnum ct: values()){
            defToCommandTypeMapping.put(ct.getDef(), ct);
        }
    }

    public static CommandTypeEnum getByDefinition(String def){
        if(defToCommandTypeMapping == null)
            initMapping();
        return defToCommandTypeMapping.get(def);
    }


    @Override
    public String toString() {
        return "CommandTypeEnum{" +
                "code=" + code +
                ", def='" + def + '\'' +
                '}';
    }
}
