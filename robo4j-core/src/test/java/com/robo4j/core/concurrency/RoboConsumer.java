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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.concurrency;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.logging.SimpleLoggingUtil;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboConsumer<T extends Comparable<T>> implements Runnable{

    private ScheduledExecutorService executor;
    private RoboBusQueue<QueueFIFOEntry<T>> queue;
    private int period;

    public RoboConsumer(RoboBusQueue<QueueFIFOEntry<T>> queue, int period) {
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.queue = queue;
        this.period = period;
    }

    public void stop(){
        executor.shutdown();
    }

    @Override
    public void run() {
        executor.scheduleAtFixedRate(() -> {
            try{
                T element = queue.take().getEntry();
                System.out.println(Thread.currentThread().getName() + ":" + getClass().getSimpleName() + " mess: "
                        + element + " queueSize: " + queue.size());
            } catch (InterruptedException e){
                SimpleLoggingUtil.error(getClass(), "consumer error", e);
            }

        }, 0, period, TimeUnit.MILLISECONDS);

    }
}
