package com.robo4j.core.system.enums;

import com.robo4j.core.control.RoboSystemConfig;
import com.robo4j.commons.enums.LegoSystemEnum;

/**
 * Created by miroslavkopecky on 04/05/16.
 */
public enum  LegoEngineEnum implements LegoSystemEnum<Character>, RoboSystemConfig {

    //@formatter:off
    //          Type    name
    NXT        ('N',    "NXTRegulatedMotor"),
    LARGE      ('L',    "EV3LargeRegulatedMotor"),
    MEDIUM     ('M',    "EV3MediumRegulatedMotor"),
    ;
    //@formatter:on

    private char type;
    private String name;

    LegoEngineEnum(char type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public Character getType() {
        return type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "LegoEngineEnum{" +
                "type=" + type +
                ", name='" + name + '\'' +
                '}';
    }
}
