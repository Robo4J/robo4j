/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LineCommandDTO.java is part of robo4j.
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

package com.robo4j.core.line;

import com.robo4j.commons.command.CommandTypeEnum;

/**
 *
 * DTO object for command line command
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 04.06.2016
 */
public class LineCommandDTO {

    private CommandTypeEnum type;
    private String name;
    private String content;

    public LineCommandDTO(String type, String name, String content) {
        this.type = CommandTypeEnum.getByDefinition(type.trim());
        this.name = name;
        this.content = content;
    }

    public CommandTypeEnum getType() {
        return type;
    }

    public void setType(CommandTypeEnum type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "LineCommandDTO{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
