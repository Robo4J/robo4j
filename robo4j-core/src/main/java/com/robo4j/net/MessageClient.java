/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Message client. Normally used by RemoteRoboContext to communicate with a
 * discovered MessageServer.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MessageClient {
	public final static String KEY_SO_TIMEOUT = "timeout";
	public final static String KEY_KEEP_ALIVE = "keepAlive";
	public final static String KEY_RETRIES = "retries";
	public final static int DEFAULT_SO_TIMEOUT = 2000000;
	public final static boolean DEFAULT_KEEP_ALIVE = true;

	/*
	 * Executor for incoming messages from the server
	 */
	private final ExecutorService remoteReferenceCallExecutor = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "RemoteReferenceCallExecutor for " + messageServerURI);
			t.setDaemon(true);
			return t;
		}
	});

	/*
	 * Listening to incoming messages from the server, initiated by serialized
	 * robo references.
	 */
	private class RemoteReferenceListener implements Runnable {
		private Socket socket;
		private volatile boolean quit;

		public RemoteReferenceListener(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			ObjectInputStream ois = getStream();
			while (!quit) {
				try {
					String uuid = ois.readUTF();
					String id = ois.readUTF();
					Object message = ois.readObject();
					RoboContext context = LookupServiceProvider.getDefaultLookupService().getContext(uuid);
					if (context == null) {
						SimpleLoggingUtil.debug(MessageClient.class,
								"Failed to find recipient context " + uuid + " for message " + message);
					} else {
						context.getReference(id).sendMessage(message);
					}
				} catch (SocketTimeoutException e) {
					// This will likely happen.
				} catch (Exception e) {
					SimpleLoggingUtil.debug(MessageClient.class, "Message delivery failed for recipient", e);
				}
			}
		}

		private ObjectInputStream getStream() {
			try {
				return new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			} catch (IOException e) {
				SimpleLoggingUtil.error(getClass(), "Failed to get input stream for remote reference listener!");
				e.printStackTrace();
			}
			return null;
		}

		public void shutdown() {
			quit = true;
		}

	}

	private final URI messageServerURI;
	private final String sourceUUID;
	private Socket socket;
	private ObjectOutputStream objectOutputStream;
	private Configuration configuration;
	private int failCount;
	private final int maxFailCount;
	private RemoteReferenceListener remoteReferenceListener;

	public MessageClient(URI messageServerURI, String sourceUUID, Configuration configuration) {
		this.messageServerURI = messageServerURI;
		this.sourceUUID = sourceUUID;
		this.configuration = configuration;
		this.maxFailCount = configuration.getInteger(KEY_RETRIES, 3);
	}

	public void connect() throws UnknownHostException, IOException {
		if (socket == null || socket.isClosed() || !socket.isConnected()) {
			socket = new Socket(messageServerURI.getHost(), messageServerURI.getPort());
			socket.setKeepAlive(configuration.getBoolean(KEY_KEEP_ALIVE, DEFAULT_KEEP_ALIVE));
			socket.setSoTimeout(configuration.getInteger(KEY_SO_TIMEOUT, DEFAULT_SO_TIMEOUT));
		}
		objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		objectOutputStream.writeShort(MessageProtocolConstants.MAGIC);
		objectOutputStream.writeUTF(sourceUUID);
		remoteReferenceListener = new RemoteReferenceListener(socket);
		remoteReferenceCallExecutor.execute(remoteReferenceListener);
	}

	public void sendMessage(String id, Object message) throws IOException {
		try {
			deliverMessage(id, message);
		} catch (IOException e) {
			if (failCount < maxFailCount) {
				failCount++;
				connect();
				sendMessage(id, message);
			} else {
				throw e;
			}
		}
	}

	private void deliverMessage(String id, Object message) throws IOException {
		objectOutputStream.writeUTF(id);
		if (message instanceof String) {
			objectOutputStream.writeByte(MessageProtocolConstants.MOD_UTF8);
			objectOutputStream.writeUTF((String) message);
		} else if (message instanceof Number) {
			if (message instanceof Float) {
				objectOutputStream.writeByte(MessageProtocolConstants.FLOAT);
				objectOutputStream.writeFloat((Float) message);
			} else if (message instanceof Integer) {
				objectOutputStream.writeByte(MessageProtocolConstants.INT);
				objectOutputStream.writeInt((Integer) message);
			} else if (message instanceof Double) {
				objectOutputStream.writeByte(MessageProtocolConstants.DOUBLE);
				objectOutputStream.writeDouble((Double) message);
			} else if (message instanceof Long) {
				objectOutputStream.writeByte(MessageProtocolConstants.LONG);
				objectOutputStream.writeLong((Long) message);
			} else if (message instanceof Byte) {
				objectOutputStream.writeByte(MessageProtocolConstants.BYTE);
				objectOutputStream.writeByte((Byte) message);
			} else if (message instanceof Short) {
				objectOutputStream.writeByte(MessageProtocolConstants.SHORT);
				objectOutputStream.writeShort((Short) message);
			}
		} else if (message instanceof Character) {
			objectOutputStream.writeByte(MessageProtocolConstants.CHAR);
			objectOutputStream.writeChar((Character) message);
		} else {
			objectOutputStream.writeByte(MessageProtocolConstants.OBJECT);
			objectOutputStream.writeObject(message);
		}
		objectOutputStream.flush();
	}

	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}

	public void shutdown() {
		try {
			objectOutputStream.flush();
			objectOutputStream.close();
			remoteReferenceListener.shutdown();
			remoteReferenceCallExecutor.shutdown();
			socket.close();
		} catch (IOException e) {
			// Do not care.
		}
	}
}
