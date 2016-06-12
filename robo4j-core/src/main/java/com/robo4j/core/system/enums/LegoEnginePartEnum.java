package com.robo4j.core.system.enums;

import com.robo4j.core.control.RoboSystemConfig;
import com.robo4j.commons.enums.LegoSystemEnum;

/**
 * Created by miroslavkopecky on 05/05/16.
 */
public enum LegoEnginePartEnum implements LegoSystemEnum<String>, RoboSystemConfig {

    //@formatter:off
    PLATFORM    ("PLATFORM", "Platform Engine to control direction"),
    HAND        ("HAND", "Engine controls Lego Hand"),
    ;
    //@formatter:on

    private String type;
    private String name;

    LegoEnginePartEnum(String type, String name) {
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
        return "LegoEnginePartEnum{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
