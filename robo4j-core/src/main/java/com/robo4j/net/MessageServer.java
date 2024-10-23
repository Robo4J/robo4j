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

import com.robo4j.configuration.Configuration;
import com.robo4j.scheduler.RoboThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is a server that listens on messages, and sends them off to the
 * indicated local recipient. It is associated to RoboContext.
 * <p>
 * TODO: Rewrite in NIO for better thread management.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class MessageServer {
    public static final String KEY_HOST_NAME = "hostname";
    public static final String KEY_PORT = "port";

    private record MessageHandler(Socket socket, MessageCallback callback,
                                  AtomicBoolean serverActive) implements Runnable {

        @Override
        public void run() {
            try (ObjectInputStream objectInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()))) {
                // Init protocol. First check magic...
                if (checkMagic(objectInputStream.readShort())) {
                    final var uuid = objectInputStream.readUTF();
                    final var serverRemoteContext = new ServerRemoteRoboContext(uuid, socket.getOutputStream());
                    // Then keep reading string, byte, data triplets until dead
                    ReferenceDescriptor.setCurrentContext(serverRemoteContext);
                    while (serverActive.get()) {
                        String id = objectInputStream.readUTF();
                        Object message = decodeMessage(objectInputStream);
                        callback.handleMessage(uuid, id, message);
                    }
                } else {
                    LOGGER.error("Got wrong communication magic - will shutdown communication with {}", socket.getRemoteSocketAddress());
                }

            } catch (IOException e) {
                LOGGER.error("IO Exception communicating with {}", socket.getRemoteSocketAddress(), e);
            } catch (ClassNotFoundException e) {
                LOGGER.error("Could not find class to deserialize message to - will stop receiving messages from {}", socket.getRemoteSocketAddress(), e);
            }
            LOGGER.info("Shutting down socket {}", socket.toString());

        }

        private Object decodeMessage(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
            byte dataType = objectInputStream.readByte();
            return switch (dataType) {
                case MessageProtocolConstants.OBJECT -> objectInputStream.readObject();
                case MessageProtocolConstants.MOD_UTF8 -> objectInputStream.readUTF();
                case MessageProtocolConstants.BYTE -> objectInputStream.readByte();
                case MessageProtocolConstants.SHORT -> objectInputStream.readShort();
                case MessageProtocolConstants.FLOAT -> objectInputStream.readFloat();
                case MessageProtocolConstants.INT -> objectInputStream.readInt();
                case MessageProtocolConstants.DOUBLE -> objectInputStream.readDouble();
                case MessageProtocolConstants.LONG -> objectInputStream.readLong();
                case MessageProtocolConstants.CHAR -> objectInputStream.readChar();
                default -> throw new IOException("The type with id " + dataType + " is not supported!");
            };
        }

        private boolean checkMagic(short magic) {
            return magic == MessageProtocolConstants.MAGIC;
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageServer.class);
    private static final String NAME_COMMUNICATION_WORKER_POOL = "Robo4J Communication Worker Pool";
    private static final String NAME_COMMUNICATION_THREAD_PREFIX = "Robo4J-Communication-Worker";

    private volatile int listeningPort = 0;
    private volatile String listeningHost;
    private final AtomicBoolean serverActive = new AtomicBoolean(false);
    private final MessageCallback callback;
    private final Configuration configuration;
    private final ExecutorService executors;


    /**
     * Constructor
     *
     * @param callback      message callback
     * @param configuration configuration
     */
    public MessageServer(MessageCallback callback, Configuration configuration) {
        this.callback = callback;
        this.configuration = configuration;
        // TODO : consider to have configurable thread-pool
        this.executors = Executors.newCachedThreadPool(new RoboThreadFactory(new ThreadGroup(NAME_COMMUNICATION_WORKER_POOL), NAME_COMMUNICATION_THREAD_PREFIX, true));
    }

    /**
     * This will be blocking/running until stop is called (and perhaps for longer).
     * Dispatch in whatever thread you feel appropriate.
     *
     * @throws IOException exception
     */
    public void start() throws IOException {
        String host = configuration.getString(KEY_HOST_NAME, null);
        InetAddress bindAddress = null;
        if (host != null) {
            bindAddress = InetAddress.getByName(host);
        }

        try (ServerSocket serverSocket = new ServerSocket(configuration.getInteger(KEY_PORT, 0),
                configuration.getInteger("backlog", 20), bindAddress)) {
            listeningHost = serverSocket.getInetAddress().getHostAddress();
            listeningPort = serverSocket.getLocalPort();
            var threadGroup = new ThreadGroup("Robo4J communication threads");
            serverActive.set(true);
            while (serverActive.get()) {
                var messageHandler = new MessageHandler(serverSocket.accept(), callback, serverActive);
                executors.submit(messageHandler);
            }
        } finally {
            serverActive.set(false);
            executors.shutdown();
        }
    }

    public void stop() {
        serverActive.set(false);
        executors.shutdown();
    }

    public int getListeningPort() {
        return listeningPort;
    }

    /**
     * @return the URI for the listening socket. This is the address to connect to.
     * Will return null if the server isn't up and running yet, or if badly
     * configured.
     */
    public URI getListeningURI() {
        if (!serverActive.get()) {
            return null;
        }

        try {
            String host = configuration.getString(KEY_HOST_NAME, null);
            if (host != null) {
                return new URI("robo4j", "", host, listeningPort, "", "", "");
            } else {
                return new URI("robo4j", "", listeningHost, listeningPort, "", "", "");
            }
        } catch (URISyntaxException e) {
            LOGGER.error("Could not create URI for listening URI");
            return null;
        }
    }
}
