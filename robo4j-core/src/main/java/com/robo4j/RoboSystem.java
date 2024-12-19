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
package com.robo4j;

import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.net.ContextEmitter;
import com.robo4j.net.MessageServer;
import com.robo4j.net.ReferenceDescriptor;
import com.robo4j.net.RoboContextDescriptor;
import com.robo4j.scheduler.DefaultScheduler;
import com.robo4j.scheduler.RoboThreadFactory;
import com.robo4j.scheduler.Scheduler;
import com.robo4j.util.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * This is the default implementation for a local {@link RoboContext}. Contains
 * RoboUnits, a lookup service for references to RoboUnits, and a system level
 * life cycle.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
final class RoboSystem implements RoboContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(RoboSystem.class);
    private static final String THREAD_GROUP_BLOCKING_NAME_BLOCKING_POOL = "Robo4J Blocking Pool";
    private static final String THREAD_GROUP_NAME_WORKER_POOL = "Robo4J Worker Pool";
    private static final int DEFAULT_BLOCKING_POOL_SIZE = 4;
    private static final int DEFAULT_WORKER_POOL_SIZE = 2;
    private static final int DEFAULT_SCHEDULER_POOL_SIZE = 2;
    private static final int KEEP_ALIVE_TIME = 10;

    private static final EnumSet<LifecycleState> MESSAGE_DELIVERY_CRITERIA = EnumSet.of(LifecycleState.STARTED, LifecycleState.STOPPED,
            LifecycleState.STOPPING);
    private static final int SERVER_LISTEN_URI_MILLIS = 100;
    private static final int SERVER_LISTEN_REPEATS = 5;

    private final AtomicReference<LifecycleState> state = new AtomicReference<>(LifecycleState.UNINITIALIZED);
    private final Map<String, RoboUnit<?>> units = new HashMap<>();
    private final Map<RoboUnit<?>, RoboReference<?>> referenceCache = new WeakHashMap<>();

    private final Scheduler systemScheduler;

    private final ThreadPoolExecutor workExecutor;
    // TODO: review usage of workQueue and blockingQueue, maybe better abstraction
    private final LinkedBlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>();

    private final ThreadPoolExecutor blockingExecutor;
    private final LinkedBlockingQueue<Runnable> blockingQueue = new LinkedBlockingQueue<>();

    private final String uid;
    private final Configuration configuration;

    private final MessageServer messageServer;
    private final Configuration emitterConfiguration;
    private volatile ScheduledFuture<?> emitterFuture;

    private enum DeliveryPolicy {
        SYSTEM, WORK, BLOCKING
    }

    private enum ThreadingPolicy {
        NORMAL, CRITICAL
    }

    private class LocalRoboReference<T> implements RoboReference<T>, Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
        private static final Logger LOGGER_LOCAL = LoggerFactory.getLogger(LocalRoboReference.class);
        private final RoboUnit<T> unit;
        private final DeliveryPolicy deliveryPolicy;
        private final ThreadingPolicy threadingPolicy;

        LocalRoboReference(RoboUnit<T> unit) {
            this.unit = unit;
            @SuppressWarnings("unchecked")
            Class<? extends RoboUnit<?>> clazz = (Class<? extends RoboUnit<?>>) unit.getClass();
            this.deliveryPolicy = deriveDeliveryPolicy(clazz);
            this.threadingPolicy = deriveThreadingPolicy(clazz);
        }

        private ThreadingPolicy deriveThreadingPolicy(Class<? extends RoboUnit<?>> clazz) {
            if (clazz.getAnnotation(CriticalSectionTrait.class) != null) {
                return ThreadingPolicy.CRITICAL;
            }
            return ThreadingPolicy.NORMAL;
        }

        private DeliveryPolicy deriveDeliveryPolicy(Class<? extends RoboUnit<?>> clazz) {
            if (clazz.getAnnotation(WorkTrait.class) != null) {
                return DeliveryPolicy.WORK;
            }
            if (clazz.getAnnotation(BlockingTrait.class) != null) {
                return DeliveryPolicy.BLOCKING;
            }
            return DeliveryPolicy.SYSTEM;
        }

        @Override
        public String id() {
            return unit.id();
        }

        @Override
        public LifecycleState getState() {
            return unit.getState();
        }

        @Override
        public Configuration getConfiguration() {
            return unit.getConfiguration();
        }

        @Override
        public void sendMessage(T message) {
            if (MESSAGE_DELIVERY_CRITERIA.contains(getState())) {
                switch (threadingPolicy) {
                    case NORMAL:
                        deliverOnQueue(message);
                        break;
                    case CRITICAL:
                        synchronized (unit) {
                            deliverOnQueue(message);
                        }
                        break;
                    default:
                        throw new IllegalStateException(String.format("not supported policy: %s", threadingPolicy));
                }
            }
        }

        @Override
        public String toString() {
            return "LocalReference id: " + unit.id() + " (system: " + uid + ")";
        }

        private void deliverOnQueue(T message) {
            switch (deliveryPolicy) {
                case SYSTEM -> systemScheduler.execute(new Messenger<T>(unit, message));
                case WORK -> workExecutor.execute(new Messenger<T>(unit, message));
                case BLOCKING -> blockingExecutor.execute(new Messenger<T>(unit, message));
                default -> LOGGER_LOCAL.error("not supported policy: {}", deliveryPolicy);
            }
        }

        @Override
        public <R> Future<R> getAttribute(AttributeDescriptor<R> attribute) {
            return systemScheduler.submit(() -> unit.onGetAttribute(attribute));
        }

        @Override
        public Collection<AttributeDescriptor<?>> getKnownAttributes() {
            return unit.getKnownAttributes();
        }

        @Override
        public Future<Map<AttributeDescriptor<?>, Object>> getAttributes() {
            return systemScheduler.submit(unit::onGetAttributes);
        }

        @Override
        public Class<T> getMessageType() {
            return unit.getMessageType();
        }

        @Serial
        Object writeReplace() throws ObjectStreamException {
            return new ReferenceDescriptor(RoboSystem.this.getId(), id(), getMessageType().getName());
        }
    }

    // Protects the executors from problems in the units.
    private static class Messenger<T> implements Runnable {
        private static final Logger LOGGER_MESSENGER = LoggerFactory.getLogger(Messenger.class);
        private final RoboUnit<T> unit;
        private final T message;

        public Messenger(RoboUnit<T> unit, T message) {
            this.unit = unit;
            this.message = message;
        }

        @Override
        public void run() {
            try {
                unit.onMessage(message);
            } catch (Throwable t) {
                LOGGER_MESSENGER.error("Error processing message, unit:{}", unit.id(), t);
            }
        }
    }

    /**
     * Constructor.
     */
    RoboSystem() {
        this(UUID.randomUUID().toString());
    }

    /**
     * Constructor.
     */
    RoboSystem(String uid) {
        this(uid, DEFAULT_SCHEDULER_POOL_SIZE, DEFAULT_WORKER_POOL_SIZE, DEFAULT_BLOCKING_POOL_SIZE);
    }

    /**
     * Constructor.
     */
    RoboSystem(String uid, Configuration configuration) {
        this.uid = uid;
        this.configuration = configuration;
        int schedulerPoolSize = configuration.getInteger(RoboBuilder.KEY_SCHEDULER_POOL_SIZE, DEFAULT_SCHEDULER_POOL_SIZE);
        int workerPoolSize = configuration.getInteger(RoboBuilder.KEY_WORKER_POOL_SIZE, DEFAULT_WORKER_POOL_SIZE);
        int blockingPoolSize = configuration.getInteger(RoboBuilder.KEY_BLOCKING_POOL_SIZE, DEFAULT_SCHEDULER_POOL_SIZE);

        var workThreadFactory = new RoboThreadFactory
                .Builder(THREAD_GROUP_NAME_WORKER_POOL)
                .addThreadPrefix(THREAD_GROUP_NAME_WORKER_POOL)
                .build();

        var blockingThreadFactory = new RoboThreadFactory
                .Builder(THREAD_GROUP_BLOCKING_NAME_BLOCKING_POOL)
                .addThreadPrefix(THREAD_GROUP_BLOCKING_NAME_BLOCKING_POOL)
                .build();

        workExecutor = new ThreadPoolExecutor(workerPoolSize, workerPoolSize, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue, workThreadFactory);
        blockingExecutor = new ThreadPoolExecutor(blockingPoolSize, blockingPoolSize, KEEP_ALIVE_TIME, TimeUnit.SECONDS, blockingQueue, blockingThreadFactory);
        systemScheduler = new DefaultScheduler(this, schedulerPoolSize);
        messageServer = initServer(configuration.getChildConfiguration(RoboBuilder.KEY_CONFIGURATION_SERVER));
        emitterConfiguration = configuration.getChildConfiguration(RoboBuilder.KEY_CONFIGURATION_EMITTER);
    }

    /**
     * Constructor.
     */
    RoboSystem(Configuration config) {
        this(UUID.randomUUID().toString(), config);
    }

    /**
     * Convenience constructor.
     */
    RoboSystem(String uid, int schedulerPoolSize, int workerPoolSize, int blockingPoolSize) {
        this(uid, createConfiguration(schedulerPoolSize, workerPoolSize, blockingPoolSize));
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Adds the specified units to the system.
     *
     * @param unitSet the units to add.
     */
    public void addUnits(Set<RoboUnit<?>> unitSet) {
        if (state.get() != LifecycleState.UNINITIALIZED) {
            throw new UnsupportedOperationException("All units must be registered up front for now.");
        }
        addToMap(unitSet);
    }

    /**
     * Adds the specified units to the system.
     *
     * @param units the units to add.
     */
    public void addUnits(RoboUnit<?>... units) {
        if (state.get() != LifecycleState.UNINITIALIZED) {
            throw new UnsupportedOperationException("All units must be registered up front for now.");
        }
        addToMap(units);
    }

    @Override
    public void start() {
        var currentState = state.get();
        switch (currentState) {
            case INITIALIZED, STOPPED -> {
                state.compareAndSet(currentState, LifecycleState.STARTING);
                startUnits();
            }
        }
        // If we have a server, start it, then set up emitter
        blockingExecutor.execute(() -> {
            if (messageServer != null) {
                try {
                    messageServer.start();
                } catch (IOException e) {
                    LOGGER.error("Could not start the message server. Proceeding without.", e);
                }
            }
        });

        // TODO : make emitter configurable
        final ContextEmitter emitter = initEmitter(emitterConfiguration, getListeningURI(messageServer));
        if (emitter != null) {
            emitterFuture = getScheduler().scheduleAtFixedRate(emitter::emit, 0, emitter.getHeartBeatInterval(), TimeUnit.MILLISECONDS);
        }
    }

    private void startUnits() {
        // NOTE(Marcus/Sep 4, 2017): May want to schedule the starts.
        for (RoboUnit<?> unit : units.values()) {
            unit.setState(LifecycleState.STARTING);
            unit.start();
            unit.setState(LifecycleState.STARTED);
        }
        state.set(LifecycleState.STARTED);
    }

    @Override
    public void stop() {
        // NOTE(Marcus/Sep 4, 2017): We may want schedule the stops in the
        // future.
        if (emitterFuture != null) {
            emitterFuture.cancel(true);
        }
        if (messageServer != null) {
            messageServer.stop();
        }
        if (state.compareAndSet(LifecycleState.STARTED, LifecycleState.STOPPING)) {
            units.values().forEach(RoboUnit::stop);
        }
        state.set(LifecycleState.STOPPED);
    }

    @Override
    public void shutdown() {
        stop();
        state.set(LifecycleState.SHUTTING_DOWN);
        units.values().forEach((unit) -> unit.setState(LifecycleState.SHUTTING_DOWN));

        // First shutdown all executors. We don't care at this point, as any
        // messages will no longer be delivered.
        workExecutor.shutdown();
        blockingExecutor.shutdown();

        // Then schedule shutdowns on the scheduler threads...
        for (RoboUnit<?> unit : units.values()) {
            getScheduler().execute(() -> RoboSystem.shutdownUnit(unit));
        }

        // Then shutdown the system scheduler. Will wait until the termination
        // shutdown of the system scheduler (or the timeout).
        try {
            systemScheduler.shutdown();
        } catch (InterruptedException e) {
            LOGGER.error("System scheduler was interrupted when shutting down.", e);
        }
        state.set(LifecycleState.SHUTDOWN);
    }

    @Override
    public LifecycleState getState() {
        return state.get();
    }

    public void setState(LifecycleState state) {
        this.state.set(state);
    }

    @Override
    public Collection<RoboReference<?>> getUnits() {
        return units.values().stream().map(this::getReference).collect(Collectors.toList());
    }

    @Override
    public <T> RoboReference<T> getReference(String id) {
        @SuppressWarnings("unchecked")
        RoboUnit<T> roboUnit = (RoboUnit<T>) units.get(id);
        if (roboUnit == null) {
            return null;
        }
        return getReference(roboUnit);
    }

    @Override
    public Scheduler getScheduler() {
        return systemScheduler;
    }

    @Override
    public String getId() {
        return uid;
    }

    /**
     * Returns the reference for a specific unit.
     *
     * @param roboUnit the robo unit for which to retrieve a reference.
     * @return the {@link RoboReference} to the unit.
     */
    public <T> RoboReference<T> getReference(RoboUnit<T> roboUnit) {
        @SuppressWarnings("unchecked")
        RoboReference<T> reference = (RoboReference<T>) referenceCache.get(roboUnit);
        if (reference == null) {
            reference = createReference(roboUnit);
            referenceCache.put(roboUnit, reference);
        }
        return reference;
    }

    @Override
    public String toString() {
        return "RoboSystem id: " + uid + " unit count: " + units.size();
    }

    private <T> RoboReference<T> createReference(RoboUnit<T> roboUnit) {
        return new LocalRoboReference<>(roboUnit);
    }

    private void addToMap(Set<RoboUnit<?>> unitSet) {
        unitSet.forEach(unit -> units.put(unit.id(), unit));
    }

    private void addToMap(RoboUnit<?>... unitArray) {
        // NOTE(Marcus/Aug 9, 2017): Do not streamify...
        for (RoboUnit<?> unit : unitArray) {
            units.put(unit.id(), unit);
        }
    }

    private static void shutdownUnit(RoboUnit<?> unit) {
        // NOTE(Marcus/Aug 11, 2017): Should really be scheduled and done in
        // parallel.
        unit.shutdown();
        unit.setState(LifecycleState.SHUTDOWN);
    }

    private static Configuration createConfiguration(int schedulerPoolSize, int workerPoolSize, int blockingPoolSize) {
        return new ConfigurationBuilder().addInteger(RoboBuilder.KEY_SCHEDULER_POOL_SIZE, schedulerPoolSize)
                .addInteger(RoboBuilder.KEY_WORKER_POOL_SIZE, workerPoolSize).addInteger(RoboBuilder.KEY_BLOCKING_POOL_SIZE, blockingPoolSize).build();
    }

    private MessageServer initServer(Configuration serverConfiguration) {
        if (serverConfiguration != null) {
            return new MessageServer((sourceUuid, id, message) -> {
                // TODO: save message null message or not registered id
                Objects.requireNonNull(getReference(id)).sendMessage(message);
            }, serverConfiguration);
        } else {
            return null;
        }
    }

    private ContextEmitter initEmitter(Configuration emitterConfiguration, URI uri) {
        if (messageServer != null && uri != null) {
            if (emitterConfiguration.getBoolean(ContextEmitter.KEY_ENABLED, Boolean.FALSE)) {
                try {
                    return new ContextEmitter(createDescriptor(uri, emitterConfiguration), emitterConfiguration);
                } catch (SocketException | UnknownHostException e) {
                    LOGGER.error("Could not initialize autodiscovery emitter. Proceeding without!", e);
                }
            } else {
                LOGGER.info("Context emitter disabled in settings. Proceeding without!");
            }
        } else {
            LOGGER.info("Will not use context emitter. Server was: " + messageServer + " uri was: " + uri);
        }
        return null;
    }

    private RoboContextDescriptor createDescriptor(URI uri, Configuration emitterConfiguration) {
        int heartbeatInterval = emitterConfiguration.getInteger(ContextEmitter.KEY_HEARTBEAT_INTERVAL,
                ContextEmitter.DEFAULT_HEARTBEAT_INTERVAL);
        Map<String, String> metadata = toStringMap(emitterConfiguration.getChildConfiguration(RoboBuilder.KEY_CONFIGURATION_EMITTER_METADATA));
        metadata.put(RoboContextDescriptor.KEY_URI, uri.toString());
        return new RoboContextDescriptor(getId(), heartbeatInterval, metadata);
    }

    private Map<String, String> toStringMap(Configuration configuration) {
        Map<String, String> map = new HashMap<>();
        for (String name : configuration.getValueNames()) {
            map.put(name, configuration.getString(name, null));
        }
        return map;
    }

    private static URI getListeningURI(MessageServer server) {
        if (server != null) {
            for (int i = 0; i < SERVER_LISTEN_REPEATS; i++) {
                URI uri = server.getListeningURI();
                if (uri != null) {
                    return uri;
                }
                SystemUtil.sleep(SERVER_LISTEN_URI_MILLIS);
            }
            LOGGER.warn("getListeningURI server:{}, not found", server);
        }
        LOGGER.warn("getListeningURI undefined server");
        return null;
    }
}
