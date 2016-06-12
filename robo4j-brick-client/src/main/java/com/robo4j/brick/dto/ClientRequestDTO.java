/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ClientRequestDTO.java is part of robo4j.
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

package com.robo4j.brick.dto;

import com.robo4j.brick.client.enums.RequestCommandEnum;
import com.robo4j.brick.util.QueryElement;

/**
 *
 * Client Request Holder for incoming requests
 * Single instance
 *
 * Created by miroslavkopecky on 11/06/16.
 */
public class ClientRequestDTO {

    private Long stamp;
    private RequestCommandEnum type;
    private String value;


    public ClientRequestDTO(QueryElement element) {
        this.stamp = System.currentTimeMillis();
        this.type = element.getName();
        this.value = element.getValue();
    }

    public Long getStamp() {
        return stamp;
    }

    public RequestCommandEnum getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ClientRequestDTO{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
