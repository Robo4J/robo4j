/*
 * Copyright (C) 2017. Miroslav Wengner, Marcus Hirt
 * This ClientSystemRequestDTO.java  is part of robo4j.
 * module: robo4j-core
 *
 * robo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * robo4j is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.dto;

import com.robo4j.commons.command.SystemCommandEnum;

/**
 * @author Miro Wengner (@miragemiko)
 * @since 18.01.2017
 */


public class ClientSystemRequestDTO implements ClientCommandDTO<SystemCommandEnum> {

    private Long stamp;
    private SystemCommandEnum type;

    public ClientSystemRequestDTO(SystemCommandEnum type) {
        this.stamp = System.currentTimeMillis();
        this.type = type;
    }

    @Override
    public SystemCommandEnum getCommand() {
        return type;
    }

    @Override
    public String getValue() {
        return "";
    }

    @Override
    public String toString() {
        return "ClientSystemRequestDTO{" +
                "stamp=" + stamp +
                ", type=" + type +
                '}';
    }
}
