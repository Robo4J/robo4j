/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This LegoPlatformCommand.java is part of robo4j.
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


import com.robo4j.commons.concurrent.TransferSignal;
import com.robo4j.core.engines.EngineCache;
import com.robo4j.core.lego.LegoBrickRemoteProvider;
import com.robo4j.core.platform.PlatformProperties;
import com.robo4j.core.platform.provider.LegoBrickCommandsProvider;
import com.robo4j.core.platform.provider.LegoBrickCommandsProviderImp;
import com.robo4j.core.unit.UnitCache;

/**
 *
 * LegoPlatformCommand stores attributes of an event: its command type and priority
 * - implements the comparable interface to help priority queue to decide with event
 * has higher priority
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 26.09.2014
 */
public class LegoPlatformCommand implements Comparable<LegoPlatformCommand>, TransferSignal {

    private LegoPlatformCommandEnum command;
    private final PlatformProperties properties;
    private int priority;


    private volatile LegoBrickCommandsProvider legoBrickCommandsProvider;

    public LegoPlatformCommand(final LegoBrickRemoteProvider remoteProvider,
                               final LegoPlatformCommandEnum command,
                               final EngineCache engineCache,
                               final UnitCache unitCache,
                               final int priority) {
        this.command = command;
        this.priority = priority;
        this.properties = new PlatformProperties();
        this.legoBrickCommandsProvider = new LegoBrickCommandsProviderImp(remoteProvider, properties,
                engineCache.getCache(), unitCache.getCache());
    }

    public LegoPlatformCommandEnum getCommand() {
        return command;
    }

    public int getPriority() {
        return priority;
    }

    public boolean run(){
        return legoBrickCommandsProvider.process(command);
    }

    @Override
    public int compareTo(LegoPlatformCommand o) {
        return (this.priority > o.getPriority()) ? 1 : (this.priority < o.getPriority()) ? -1 : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LegoPlatformCommand)) return false;

        LegoPlatformCommand that = (LegoPlatformCommand) o;

        if (priority != that.priority) return false;
        if (command != that.command) return false;
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) return false;
        return legoBrickCommandsProvider != null ? legoBrickCommandsProvider.equals(that.legoBrickCommandsProvider) : that.legoBrickCommandsProvider == null;

    }

    @Override
    public int hashCode() {
        int result = command != null ? command.hashCode() : 0;
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + priority;
        result = 31 * result + (legoBrickCommandsProvider != null ? legoBrickCommandsProvider.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("command= ").append(command)
                .append(" priority= ").append(priority);
        return builder.toString();
    }
}
