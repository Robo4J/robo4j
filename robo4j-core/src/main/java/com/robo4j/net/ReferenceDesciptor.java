/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 * 
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.net;

import com.robo4j.logging.SimpleLoggingUtil;

import java.io.ObjectStreamException;
import java.io.Serializable;

/**
 * Used for serializing descriptors over the network.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ReferenceDesciptor implements Serializable {
	private static final long serialVersionUID = 1L;
	private transient static final ThreadLocal<ServerRemoteRoboContext> activeContex = new ThreadLocal<>();
	private String id;
	private String fqn;

	public ReferenceDesciptor() {
	}
	
	public ReferenceDesciptor(String id, String fqn) {
		this.id = id;
		this.fqn = fqn;
	}
		
    Object readResolve() throws ObjectStreamException {
    	ServerRemoteRoboContext remoteRoboContext = activeContex.get();
    	if (remoteRoboContext == null) {
    		SimpleLoggingUtil.error(getClass(), "No remote context set!");
    		return null;
    	}
    	return remoteRoboContext.getRoboReference(id, fqn);
    }
    
    public static void setCurrentContext(ServerRemoteRoboContext context) {
    	activeContex.set(context);
    }
}
