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

import com.robo4j.RoboContext;
import com.robo4j.configuration.Configuration;
import com.robo4j.scheduler.RoboThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Message client. Normally used by RemoteRoboContext to communicate with a
 * discovered MessageServer.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MessageClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageClient.class);
    public static final String KEY_SO_TIMEOUT_MILLS = "timeout";
    public static final String KEY_KEEP_ALIVE = "keepAlive";
    public static final String KEY_RETRIES = "retries";
    public static final int DEFAULT_SO_TIMEOUT_MILLS = 2000000;
    public static final int DEFAULT_FAILED_CONNECTION_MAX = 3;
    public static final boolean DEFAULT_KEEP_ALIVE = true;
    private final String sourceUUID;
    private final Configuration configuration;
    private final URI messageServerURI;
    private final int maxFailCount;
    private Socket socket;
    private ObjectOutputStream objectOutputStream;
    private int failCount;
    private RemoteReferenceListener remoteReferenceListener;

    /*
     * Executor for incoming messages from the server
     */
    private final ExecutorService remoteReferenceCallExecutor;

    /*
     * Listening to incoming messages from the server, initiated by serialized
     * robo references.
     */
    private static class RemoteReferenceListener implements Runnable {
        private final Socket socket;
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
                        LOGGER.debug("Failed to find recipient context {} for message {}", uuid, message);
                    } else {
                        context.getReference(id).sendMessage(message);
                    }
                } catch (SocketTimeoutException e) {
                    // This will likely happen.
                    LOGGER.error(e.getMessage());
                } catch (Exception e) {
                    LOGGER.debug("Message delivery failed for recipient", e);
                }
            }
        }

        private ObjectInputStream getStream() {
            try {
                return new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            } catch (IOException e) {
                LOGGER.error("Failed to get input stream for remote reference listener!", e);
            }
            return null;
        }

        public void shutdown() {
            quit = true;
        }

    }

    public MessageClient(URI messageServerURI, String sourceUUID, Configuration configuration) {
        this.messageServerURI = messageServerURI;
        this.sourceUUID = sourceUUID;
        this.configuration = configuration;
        this.maxFailCount = configuration.getInteger(KEY_RETRIES, DEFAULT_FAILED_CONNECTION_MAX);
        this.remoteReferenceCallExecutor = Executors.newSingleThreadExecutor(
                new RoboThreadFactory.Builder("Message-Client")
                        .addThreadPrefix("RemoteReferenceCallExecutor for " + messageServerURI).build());
    }

    public void connect() throws UnknownHostException, IOException {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            socket = new Socket(messageServerURI.getHost(), messageServerURI.getPort());
            socket.setKeepAlive(configuration.getBoolean(KEY_KEEP_ALIVE, DEFAULT_KEEP_ALIVE));
            socket.setSoTimeout(configuration.getInteger(KEY_SO_TIMEOUT_MILLS, DEFAULT_SO_TIMEOUT_MILLS));
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
        switch (message) {
            case String s -> {
                objectOutputStream.writeByte(MessageProtocolConstants.MOD_UTF8);
                objectOutputStream.writeUTF(s);
            }
            case Number number -> {
                switch (number) {
                    case Float v -> {
                        objectOutputStream.writeByte(MessageProtocolConstants.FLOAT);
                        objectOutputStream.writeFloat(v);
                    }
                    case Integer i -> {
                        objectOutputStream.writeByte(MessageProtocolConstants.INT);
                        objectOutputStream.writeInt(i);
                    }
                    case Double v -> {
                        objectOutputStream.writeByte(MessageProtocolConstants.DOUBLE);
                        objectOutputStream.writeDouble(v);
                    }
                    case Long l -> {
                        objectOutputStream.writeByte(MessageProtocolConstants.LONG);
                        objectOutputStream.writeLong(l);
                    }
                    case Byte b -> {
                        objectOutputStream.writeByte(MessageProtocolConstants.BYTE);
                        objectOutputStream.writeByte(b);
                    }
                    case Short i -> {
                        objectOutputStream.writeByte(MessageProtocolConstants.SHORT);
                        objectOutputStream.writeShort(i);
                    }
                    default -> {
                    }
                }
            }
            case Character c -> {
                objectOutputStream.writeByte(MessageProtocolConstants.CHAR);
                objectOutputStream.writeChar(c);
            }
            case null, default -> {
                objectOutputStream.writeByte(MessageProtocolConstants.OBJECT);
                objectOutputStream.writeObject(message);
            }
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
            LOGGER.error("Failed to close remote reference listener!", e);
        }
    }
}
