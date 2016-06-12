/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This RequestCommandEnum.java is part of robo4j.
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

package com.robo4j.brick.client.enums;

import com.robo4j.commons.enums.LegoSystemEnum;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by miroslavkopecky on 09/06/16.
 */
public enum RequestCommandEnum implements LegoSystemEnum<Integer> {

    //@formatter:on
    EXIT            (0, "exit"),
    MOVE            (1, "move"),
    RIGHT           (2, "right"),
    LEFT            (3, "left"),
    BACK            (4, "back"),
    ;
    //@formatter:off

    private int code;
    private String name;

    private volatile static Map<String, RequestCommandEnum> codeToLegoCommandNameMapping;


    RequestCommandEnum(int c, String name){
        this.code = c;
        this.name = name;
    }

    @Override
    public Integer getType() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }



    public static RequestCommandEnum getRequestValue(String name){
        if(codeToLegoCommandNameMapping == null)
            initMapping();
        return codeToLegoCommandNameMapping.get(name);
    }

    //Private Methods
    private static void initMapping(){
        codeToLegoCommandNameMapping = new HashMap<>();
        for(RequestCommandEnum ct: values()){
            codeToLegoCommandNameMapping.put(ct.getName(), ct);
        }
    }

    @Override
    public String toString() {
        return "RequestCommandEnum{" +
                "code=" + code +
                ", name='" + name + '\'' +
                '}';
    }
}
