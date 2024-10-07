/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
 *
 * ********************************************************************
 * Robo4J: math module
 * ********************************************************************
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
module robo4j.math {
    requires transitive jdk.jfr;
    requires org.slf4j;

    exports com.robo4j.math.features;
    exports com.robo4j.math.geometry;
    exports com.robo4j.math.geometry.impl;
    exports com.robo4j.math.jfr;
}