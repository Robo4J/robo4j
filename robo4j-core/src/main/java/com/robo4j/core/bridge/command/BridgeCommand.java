/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This BridgeCommand.java is part of robo4j.
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

import com.robo4j.core.platform.PlatformProperties;
import com.robo4j.core.platform.command.LegoPlatformCommandEnum;
import com.robo4j.core.system.TransferSignal;

/**
 * Bridge Command is handle by BUS
 *
 * Created by miroslavkopecky on 03/04/16.
 */
public class BridgeCommand implements Comparable<BridgeCommand> , TransferSignal {

    private PlatformProperties properties;
    private LegoPlatformCommandEnum type;
    private String value;
    private int priority;

    public BridgeCommand(PlatformProperties properties, LegoPlatformCommandEnum type, String value, int priority) {
        this.properties = properties;
        assert type != null;
        this.type = type;
        this.value = value;
        this.priority = priority;
    }

    public PlatformProperties getProperties() {
        return properties;
    }

    public LegoPlatformCommandEnum getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public int getPriority(){
        return priority;
    }

    @Override
    public int compareTo(BridgeCommand o) {
        return (this.priority > o.getPriority()) ? 1 : (this.priority < o.getPriority()) ? -1 : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BridgeCommand)) return false;

        BridgeCommand that = (BridgeCommand) o;

        if (priority != that.priority) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;
        if (type != that.type) return false;
        return value != null ? value.equals(that.value) : that.value == null;

    }

    @Override
    public int hashCode() {
        int result = properties != null ? properties.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + priority;
        return result;
    }

    @Override
    public String toString() {
        return "BridgeProperties{" +
                "type=" + type +
                ", value='" + value + '\'' +
                '}';
    }
}
