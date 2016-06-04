/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LineOptionsEnum.java is part of robo4j.
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

package com.robo4j.core.line;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by miroslavkopecky on 04/06/16.
 */
public enum LineOptionsEnum {

    //@formatter:off
    INFO            (42, "help"),
    EXIT            (0, "exit"),
    HELP            (1, "help"),
    COMMANDS        (2, "commands"),
    NEW_COMMAND     (3, "new_command"),
    COMMAND_LINE    (4, "command_line"),
    ;
    //@formatter:on

    private int code;
    private String name;

    private volatile static Map<Integer, LineOptionsEnum> codeToLineOptionsMapping;


    LineOptionsEnum(int c, String name){
        this.code = c;
        this.name = name;
    }

    private static void initMapping(){
        codeToLineOptionsMapping = new HashMap<>();
        for(LineOptionsEnum ct: values()){
            codeToLineOptionsMapping.put(ct.getCode(), ct);
        }
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static LineOptionsEnum getOption(int code){
        if(codeToLineOptionsMapping == null)
            initMapping();
        return codeToLineOptionsMapping.get(code);
    }


    @Override
    public String toString() {
        return "LineOptionsEnum{" +
                "code=" + code +
                ", name='" + name + '\'' +
                '}';
    }
}
