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
package com.robo4j.core.scheduler;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;

/**
 * Used by the scheduler.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class ScheduledMessageWrapper<T> implements Runnable {
	private final AtomicInteger counter;
	private final RoboReference<T> reference;
	private final T message;
	private final RoboContext context;
	private final FinalInvocationListener listener;
	private ScheduledFuture<?> future;

	ScheduledMessageWrapper(RoboContext context, RoboReference<T> reference, int numberOfInvocations, T message,
			FinalInvocationListener listener) {
		this.context = context;
		this.reference = reference;
		this.message = message;
		this.listener = listener;
		counter = new AtomicInteger(numberOfInvocations);
	}

	private void cancel() {
		future.cancel(true);
	}

	public T getMessage() {
		return message;
	}

	public void onFinalInvocation(RoboContext context) {
		listener.onFinalInvocation(context);
	}

	public void setFuture(ScheduledFuture<?> future) {
		this.future = future;
	}

	@Override
	public void run() {
		sendMessage();
		if (counter.decrementAndGet() == 0) {
			onFinalInvocation(context);
			cancel();
		}
	}

	private void sendMessage() {
		DefaultScheduler.deliverMessage(reference, message);
	}
}
