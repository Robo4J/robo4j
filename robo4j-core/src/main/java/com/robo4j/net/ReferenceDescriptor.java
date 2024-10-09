/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;

/**
 * Used for serializing robo references over the network.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ReferenceDescriptor implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferenceDescriptor.class);
    private static final ThreadLocal<ServerRemoteRoboContext> ACTIVE_CONTEXT = new ThreadLocal<>();

    private final String ctxId;
    private final String id;
    private final String fqn;

    public ReferenceDescriptor(String ctxId, String id, String fqn) {
        this.ctxId = ctxId;
        this.id = id;
        this.fqn = fqn;
    }

    @Serial
    Object readResolve() throws ObjectStreamException {
        ServerRemoteRoboContext remoteRoboContext = ACTIVE_CONTEXT.get();
        if (remoteRoboContext == null) {
            LOGGER.error("No remote context set!");
            return null;
        }
        return remoteRoboContext.getRoboReference(ctxId, id, fqn);
    }

    public static void setCurrentContext(ServerRemoteRoboContext context) {
        ACTIVE_CONTEXT.set(context);
    }
}
