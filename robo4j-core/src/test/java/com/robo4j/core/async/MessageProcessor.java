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

package com.robo4j.core.async;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Solution inspired byt the post office and sending registered letter
 * ticket is created and allow to check it's status
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class MessageProcessor {

    /* only for holding information about ticket */
    private Map<String, String> ticketAndResults = new HashMap<>();
    private LinkedBlockingQueue<ProcessMessage> ticketWorkQueue = new LinkedBlockingQueue<>();
    private volatile AtomicInteger counter = new AtomicInteger(0);
    private volatile AtomicInteger asyncCounter = new AtomicInteger(0);
    private volatile AtomicBoolean activeAsyncWorker = new AtomicBoolean(false);
    private ExecutorService executor = Executors.newCachedThreadPool();
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    private Random random = new Random();

    public MessageProcessor() {
        startAsyncWorker();
    }

    /* process synchronous message -> after message process you get result */
    /* theoretically we can implement sync queue -> where messages are done in parallel */
    public String processSync(String message){
        return transform("s", message);
    }

    /* process async message -> you will receive ticket  */
    public String processAsync(String message){
        int ticketNumber = asyncCounter.getAndIncrement();
        String ticket = "ticket_AC" + String.valueOf(ticketNumber) + "end";
        ticketWorkQueue.add(new ProcessMessage(ticket, message));
        return ticket;
    }

    public String getMessageByTicket(String message){
        return ticketAndResults.remove(message);
    }

    /* clone current map */
    public Map<String, String> getTicketAndResults(){
		lock.lock();
		try {
			//@formatter:off
            return ticketAndResults.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
             //@formatter:on
		} finally {
			lock.unlock();
		}

    }

    public int getCount(){
        return counter.get();
    }

    public int getAllAsync(){
        lock.lock();
        try {
            while(activeAsyncWorker.get() && asyncCounter.get() != ticketAndResults.size()){
                lock.lock();
				condition.await();
                lock.unlock();
            }
            return asyncCounter.get();
        } catch (InterruptedException e) {
            throw new RuntimeException("something wrong: ", e);
        } finally {
            lock.unlock();
        }
    }

    public boolean stopAsyncWorker(){
        if(activeAsyncWorker.get()){
            activeAsyncWorker.set(false);
            executor.shutdown();
        }
        return activeAsyncWorker.get();
    }

    //Private
    private String transform(String type, String message){
        return type.concat(String.valueOf(counter.getAndIncrement())).concat(message);
    }


    private void startAsyncWorker()  {
        executor.execute(() -> {
            System.out.println(getClass().getSimpleName() + " start worker");
            activeAsyncWorker.set(true);
            while (activeAsyncWorker.get()){
                try {
                    ProcessMessage processMessage = ticketWorkQueue.take();
                    TimeUnit.SECONDS.sleep(random.nextInt(3));
                    ticketAndResults.put(processMessage.getTicket(), transform("asyn", processMessage.getMessage()));
                    if(ticketAndResults.size() == asyncCounter.get()){
                        lock.lock();
                        try{
                            condition.signalAll();
                        }finally {
                            lock.unlock();
                        }
                    }
                } catch (InterruptedException e) {
                    System.err.println("magic: " + e.getCause());
                }
            }
            System.out.println(getClass().getSimpleName() + " end worker");
        });
    }
}
