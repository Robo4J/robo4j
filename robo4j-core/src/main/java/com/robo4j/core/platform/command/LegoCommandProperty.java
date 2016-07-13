/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoCommandProperty.java is part of robo4j.
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

package com.robo4j.core.platform.command;

/**
 *
 * Lego Property Is holding values for Lego Parts
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 28.03.2016
 */
public class LegoCommandProperty {

    private String value;
    private int speed;

    public LegoCommandProperty(String value) {
        this.value = value;
    }

    public LegoCommandProperty(String value, int speed) {
        this.value = value;
        this.speed = speed;
    }

    public String getValue() {
        return value;
    }

    public int getSpeed() {
        return speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LegoCommandProperty)) return false;

        LegoCommandProperty that = (LegoCommandProperty) o;

        if (speed != that.speed) return false;
        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + speed;
        return result;
    }

    @Override
    public String toString() {
        return "LegoCommandProperty{" +
                "value='" + value + '\'' +
                ", speed='" + speed + '\'' +
                '}';
    }
}
