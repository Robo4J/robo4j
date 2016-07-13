/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This FrontHandTouchSensor.java is part of robo4j.
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

package com.robo4j.line.sensor;

import com.robo4j.commons.annotation.RoboSensor;
import com.robo4j.lego.control.LegoSensor;
import com.robo4j.lego.enums.LegoEnginePartEnum;
import com.robo4j.lego.enums.LegoSensorEnum;
import com.robo4j.lego.enums.LegoSensorPortEnum;

/**
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 19.06.2016
 */

@RoboSensor(value = "frontHandSensor")
public class FrontHandTouchSensor implements LegoSensor {

    private LegoSensorPortEnum port;
    private LegoEnginePartEnum part;
    private LegoSensorEnum sensor;
    private String holder;

    private String limit;


    public FrontHandTouchSensor() {
        this.port = LegoSensorPortEnum.S1;
        this.part = LegoEnginePartEnum.HAND;
        this.sensor = LegoSensorEnum.TOUCH;
        this.holder = "frontHandEngine";
        this.limit = "500";
    }

    @Override
    public LegoSensorPortEnum getPort() {
        return port;
    }

    @Override
    public LegoEnginePartEnum getPart() {
        return part;
    }

    @Override
    public LegoSensorEnum getSensor() {
        return sensor;
    }

    public String getHolder() {
        return holder;
    }

    public String getLimit() {
        return limit;
    }

    @Override
    public String toString() {
        return "FrontHandTouchSensor{" +
                "port=" + port +
                ", part=" + part +
                ", sensor=" + sensor +
                ", holder='" + holder + '\'' +
                ", limit='" + limit + '\'' +
                '}';
    }
}
