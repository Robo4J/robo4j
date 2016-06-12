/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LittleLeft40BatchCommand.java is part of robo4j.
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

import com.robo4j.core.annotation.BatchAnnotation;
import com.robo4j.core.bridge.command.cache.BatchCommand;

/**
 * Created by miroslavkopecky on 17/04/16.
 */
@BatchAnnotation(name = "littleLeft40", batch = "left(90),move(40)")
public class LittleLeft40BatchCommand implements BatchCommand {

    private String name;
    private String batch;

    public LittleLeft40BatchCommand() {
        this.name = "littleLeft40";
        this.batch = "left(90),move(40)";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBatch() {
        return batch;
    }

    @Override
    public void setBatch(String batch) {
        this.batch = batch;
    }


    @Override
    public String toString() {
        return "LittleLeft40BatchCommand{" +
                "batch='" + batch + '\'' +
                '}';
    }
}
