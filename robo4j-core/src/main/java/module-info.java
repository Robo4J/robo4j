/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
 *
 * ********************************************************************
 * Robo4J: core module
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
module robo4j.core {
    requires java.logging;
    requires java.xml;

    exports com.robo4j;
    exports com.robo4j.util;
    exports com.robo4j.configuration;
    exports com.robo4j.logging;
    exports com.robo4j.reflect;
    exports com.robo4j.scheduler;
    exports com.robo4j.net;

    uses com.robo4j.BlockingTrait;
    uses com.robo4j.util.Utf8Constant;
    uses com.robo4j.configuration.Configuration;

}