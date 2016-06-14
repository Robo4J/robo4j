/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This RightEngine.java is part of robo4j.
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

package com.robo4j.line.engine;

import com.robo4j.commons.annotation.RoboEngine;
import com.robo4j.lego.control.LegoEngine;
import com.robo4j.lego.enums.LegoAnalogPortEnum;
import com.robo4j.lego.enums.LegoEngineEnum;
import com.robo4j.lego.enums.LegoEnginePartEnum;

/**
 * Created by miroslavkopecky on 04/06/16.
 */

@RoboEngine(value = "right")
public class RightEngine implements LegoEngine {

    private final LegoAnalogPortEnum port;
    private final LegoEngineEnum engine;
    private final LegoEnginePartEnum part;

    public RightEngine() {
        port = LegoAnalogPortEnum.B;
        engine = LegoEngineEnum.NXT;
        part = LegoEnginePartEnum.PLATFORM;
    }

    @Override
    public LegoAnalogPortEnum getPort() {
        return port;
    }

    @Override
    public LegoEngineEnum getEngine() {
        return engine;
    }

    @Override
    public LegoEnginePartEnum getPart() {
        return part;
    }

    @Override
    public String toString() {
        return "RightEngine{" +
                "port=" + port +
                ", engine=" + engine +
                ", part=" + part +
                '}';
    }
}