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
package com.robo4j.scheduler;

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This is the default scheduler used in Robo4J.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class DefaultScheduler implements Scheduler {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultScheduler.class);
    private static final int DEFAULT_NUMBER_OF_THREADS = 2;
    private static final int TERMINATION_TIMEOUT_SEC = 4;

    private final ScheduledExecutorService executor;
    private final RoboContext context;

    /**
     * Default constructor.
     *
     * @param context the context.
     */
    public DefaultScheduler(RoboContext context) {
        this(context, DEFAULT_NUMBER_OF_THREADS);
    }

    /**
     * Constructor.
     *
     * @param context         the context.
     * @param numberOfThreads the number of threads in the thread pool.
     */
    public DefaultScheduler(RoboContext context, int numberOfThreads) {
        this.context = context;
        this.executor = new ScheduledThreadPoolExecutor(numberOfThreads,
                new RoboThreadFactory(new ThreadGroup("Robo4J Scheduler"), "Robo4J Scheduler", true));
    }

    @Override
    public <T> ScheduledFuture<?> schedule(RoboReference<T> target, T message, long delay, long interval, TimeUnit unit,
                                           int numberOfInvocations) {
        return schedule(target, message, delay, interval, unit, numberOfInvocations, null);
    }

    @Override
    public <T> ScheduledFuture<?> schedule(RoboReference<T> target, T message, long delay, long period, TimeUnit unit,
                                           int numberOfInvocations, FinalInvocationListener listener) {
        ScheduledMessageWrapper<T> command = createCommand(target, numberOfInvocations, message, listener);
        ScheduledFuture<?> future = executor.scheduleAtFixedRate(command, delay, period, unit);
        command.setFuture(future);
        return future;
    }

    private <T> ScheduledMessageWrapper<T> createCommand(RoboReference<T> reference, int numberOfInvocations, T message,
                                                         FinalInvocationListener listener) {
        return new ScheduledMessageWrapper<>(context, reference, numberOfInvocations, message, listener);
    }

    @Override
    public <T> ScheduledFuture<?> schedule(RoboReference<T> target, T message, long delay, long interval, TimeUnit unit) {
        return executor.scheduleAtFixedRate(() -> deliverMessage(target, message), delay, interval, unit);
    }

    @Override
    public void shutdown() throws InterruptedException {
        executor.shutdown();
        var status = executor.awaitTermination(TERMINATION_TIMEOUT_SEC, TimeUnit.SECONDS);
        LOGGER.debug("shutdown status:{}", status);
    }

    static <T> void deliverMessage(final RoboReference<T> reference, final T message) {
        // Performance optimization - let the scheduling thread deliver the
        // message directly if this is robo unit implementation, instead of
        // enqueuing it with the message executor.
        if (reference instanceof RoboUnit) {
            ((RoboUnit<T>) reference).onMessage(message);
        } else {
            reference.sendMessage(message);
        }
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long delay, long interval, TimeUnit unit) {
        return executor.scheduleAtFixedRate(runnable, delay, interval, unit);
    }

    @Override
    public void schedule(Runnable runnable, long delay, TimeUnit unit) {
        executor.schedule(runnable, delay, unit);
    }

    @Override
    public void execute(Runnable r) {
        executor.execute(r);
    }

    @Override
    public <T> Future<T> submit(Callable<T> r) {
        return executor.submit(r);
    }
}
