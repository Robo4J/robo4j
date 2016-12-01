/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This MotorRotationEnum.java  is part of robo4j.
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

package com.robo4j.commons.motor;

/**
 * Enum is used to distinguished the movement direction
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 03.11.2016
 */
public enum MotorRotationEnum {

    //@formatter:on
    FORWARD            (0, 1,  "forward"),
    STOP               (1, 0,  "stop"),
    BACKWARD           (2, -1, "backward"),
    ;
    //@formatter:on

    private int id;
    private int code;
    private String decs;

    MotorRotationEnum(int id, int code, String desc) {
        this.id = id;
        this.code = code;
        this.decs = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDecs() {
        return decs;
    }

    @Override
    public String toString() {
        return "MotorRotationEnum{" +
                "id=" + id +
                ", code=" + code +
                ", decs='" + decs + '\'' +
                '}';
    }
}
