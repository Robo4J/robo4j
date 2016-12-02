/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This ClientUnitRequestDTO.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.dto;

/**
 * @author Miro Kopecky (@miragemiko)
 * @since 06.11.2016
 */
public class ClientUnitRequestDTO {

    private Long stamp;
    private String name;
    private Boolean active;

    public ClientUnitRequestDTO(String name, Boolean active) {
        stamp = System.currentTimeMillis();
        this.name = name;
        this.active = active;
    }

    public Long getStamp() {
        return stamp;
    }

    public String getName() {
        return name;
    }

    public Boolean getActive() {
        return active;
    }

    @Override
    public String toString() {
        return "ClientUnitRequestDTO{" +
                "stamp=" + stamp +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
