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
 * Created by miroslavkopecky on 28/03/16.
 */
public class LegoCommandProperty {

    private String value;

    public LegoCommandProperty(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LegoCommandProperty)) return false;

        LegoCommandProperty that = (LegoCommandProperty) o;

        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LegoPlatformProperty{" +
                "value='" + value + '\'' +
                '}';
    }
}
