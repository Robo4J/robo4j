package com.robo4j.core.system.dto;

import com.robo4j.core.system.enums.LegoAnalogPortEnum;
import com.robo4j.core.system.enums.LegoEngineEnum;

/**
 * Created by miroslavkopecky on 04/05/16.
 */
public class LegoEngineDTO {

    private LegoAnalogPortEnum port;
    private LegoEngineEnum type;

    public LegoEngineDTO(LegoAnalogPortEnum port, LegoEngineEnum type) {
        this.port = port;
        this.type = type;
    }

    public LegoAnalogPortEnum getPort() {
        return port;
    }

    public LegoEngineEnum getType() {
        return type;
    }

    @Override
    public String toString() {
        return "LegoEngineDTO{" +
                "port=" + port +
                ", type=" + type +
                '}';
    }
}
