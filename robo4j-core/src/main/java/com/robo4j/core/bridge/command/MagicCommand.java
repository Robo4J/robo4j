/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This MagicCommand.java is part of robo4j.
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
@BatchAnnotation(name = "magic", batch = "move(30),right(360),back(30)")
public class MagicCommand implements BatchCommand {

    private String name;
    private String batch;

    public MagicCommand() {
        this.name = "magic";
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
        return "MagicCommand{" +
                "batch='" + batch + '\'' +
                '}';
    }
}


