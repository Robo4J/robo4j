package com.robo4j.core.system.enums;

import com.robo4j.core.control.RoboSystemConfig;
import com.robo4j.commons.enums.LegoSystemEnum;

/**
 * Created by miroslavkopecky on 04/05/16.
 */
public enum  LegoAnalogPortEnum implements LegoSystemEnum<String>, RoboSystemConfig {

    //@formatter:off
    A ("A", "Analog Bug A"),
    B ("B", "Analog Bug B"),
    C ("C", "Analog Bug C"),
    D ("D", "Analog Bug D"),
    ;
    //@formatter:on

    private String type;
    private String name;

    LegoAnalogPortEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "LegoAnalogPortEnum{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
