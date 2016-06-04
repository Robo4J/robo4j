/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This CommandParsed.java is part of robo4j.
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

/**
 * Created by miroslavkopecky on 25/04/16.
 */
public class CommandParsed {

    private CommandTypeEnum type;
    private String line;

    public CommandParsed(CommandTypeEnum type, String line) {
        this.type = type;
        this.line = line;
    }

    public CommandTypeEnum getType() {
        return type;
    }

    public void setType(CommandTypeEnum type) {
        this.type = type;
    }

    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Override
    public String toString() {
        return "CommandTypePersed{" +
                "type=" + type +
                ", line='" + line + '\'' +
                '}';
    }
}
