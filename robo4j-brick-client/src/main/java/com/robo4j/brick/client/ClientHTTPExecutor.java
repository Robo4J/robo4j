/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ClientHTTPExecutor.java is part of robo4j.
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

package com.robo4j.brick.client;


import com.robo4j.brick.client.io.ClientThreadFactory;
import com.robo4j.brick.client.util.ExecutorTaskDetails;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by miroslavkopecky on 28/02/16.
 */
public class ClientHTTPExecutor extends ThreadPoolExecutor {
    private static final int NUM_THREADS = 2;
    private static final long KEEP_ALIVE = 0L;
    /**
     * A HashMap to stor the start of the task being executed
     */
    private ConcurrentHashMap<String, ExecutorTaskDetails> startTimes;

    public ClientHTTPExecutor(){
        super(NUM_THREADS, NUM_THREADS, KEEP_ALIVE, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        startTimes = new ConcurrentHashMap<>();
        this.setThreadFactory(new ClientThreadFactory("clientCenter"));
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return super.shutdownNow();
    }

    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        ExecutorTaskDetails executorTaskDetails = new ExecutorTaskDetails(t.getName(), LocalDateTime.now());
        startTimes.put(String.valueOf(r.hashCode()), executorTaskDetails);
    }
    private static long getMillsByLocalDateTime(LocalDateTime localDateTime){
        return localDateTime.toInstant(ZoneOffset.ofTotalSeconds(0)).toEpochMilli();
    }
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        Future<?> result = (Future)r;
        ExecutorTaskDetails executorTaskDetails = startTimes.remove(String.valueOf(r.hashCode()));
        LocalDateTime startDate= executorTaskDetails.getLocalDateTime();
        LocalDateTime finishDate = LocalDateTime.now();
        long diff= getMillsByLocalDateTime(finishDate) - getMillsByLocalDateTime(startDate);

    }
}
