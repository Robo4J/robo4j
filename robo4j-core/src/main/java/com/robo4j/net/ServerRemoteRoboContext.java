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

import com.robo4j.AttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.scheduler.Scheduler;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Future;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

public class ServerRemoteRoboContext implements RoboContext {
	private final String uuid;
	private final ObjectOutputStream outputStream;

	
	@SuppressWarnings("rawtypes")
	private class ServerRemoteRoboReference implements RoboReference {
		private final String id;
		private final Class<?> actualMessageClass;
		
		public ServerRemoteRoboReference(String id, String fqn) {
			this.id = id;
			this.actualMessageClass = resolve(fqn);
		}


		@Override
		public String getId() {
			return id;
		}

		@Override
		public LifecycleState getState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class<?> getMessageType() {
			return actualMessageClass;
		}

		@Override
		public Configuration getConfiguration() {
			return null;
		}

		@Override
		public Collection<AttributeDescriptor<?>> getKnownAttributes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Future<Map<AttributeDescriptor<?>, Object>> getAttributes() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void sendMessage(Object message) {
			try {
				// Will use the same serialization protocol as server later
				outputStream.writeUTF(getId());
				outputStream.writeObject(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public Future getAttribute(AttributeDescriptor attribute) {
			// TODO Auto-generated method stub
			return null;
		}
	
		private Class<?> resolve(String fqn) {
			try {
				return Class.forName(fqn);
			} catch (ClassNotFoundException e) {
				SimpleLoggingUtil.error(getClass(), "Could not use class for remote communication", e);
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
	public ServerRemoteRoboContext(String uuid, OutputStream out) throws IOException {
		this.uuid = uuid;
		this.outputStream = new ObjectOutputStream(out);
	}

	@Override
	public LifecycleState getState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> RoboReference<T> getReference(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<RoboReference<?>> getUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Scheduler getScheduler() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		return uuid;
	}

	@Override
	public Configuration getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	public RoboReference<?> getRoboReference(String id, String fqn) {
		// FIXME: Cache these?
		return new ServerRemoteRoboReference(id, fqn);		
	}
}
