/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.logging.SimpleLoggingUtil;

/**
 * A simple unit which will count upwards from zero. Useful, for example, as a
 * heart beat generator.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CounterUnit extends RoboUnit<CounterCommand> {
	private final AtomicInteger counter = new AtomicInteger(0);

	private int interval;

	/**
	 * This configuration key controls the interval between the updates, in ms.
	 */
	public static final String KEY_INTERVAL = "interval";

	/**
	 * The default period, if no period is configured.
	 */
	public static final int DEFAULT_INTERVAL = 1000;

	/**
	 * This configuration key controls the target of the counter updates. This
	 * configuration key is mandatory. Also, the target must exist when the
	 * counter unit is started, and any change whilst running will be ignored.
	 */
	public static final String KEY_TARGET = "target";

	/*
	 * The currently running timer updater.
	 */
	private ScheduledFuture<?> scheduledFuture;

	/*
	 * The id of the target.
	 */
	private String targetId;

	private final class CounterUnitAction implements Runnable {
		private RoboReference<Integer> target;

		public CounterUnitAction(RoboReference<Integer> target) {
			this.target = target;
		}

		@Override
		public void run() {
			if (target != null) {
				target.sendMessage(counter.getAndIncrement());
			} else {
				SimpleLoggingUtil.error(CounterUnit.class,
						"The target " + targetId + " for the CounterUnit does not exist! Could not send count!");
			}
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            the RoboContext.
	 * @param id
	 *            the id of the RoboUnit.
	 */
	public CounterUnit(RoboContext context, String id) {
		super(CounterCommand.class, context, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		interval = configuration.getInteger(KEY_INTERVAL, DEFAULT_INTERVAL);
		targetId = configuration.getString(KEY_TARGET, null);
		if (targetId == null) {
			throw ConfigurationException.createMissingConfigNameException(KEY_TARGET);
		}
	}

	@Override
	public void onMessage(CounterCommand message) {
		synchronized (this) {
			super.onMessage(message);
			switch (message) {
			case START:
				scheduledFuture = getContext().getScheduler().scheduleAtFixedRate(
						new CounterUnitAction(getContext().getReference(targetId)), 0, interval, TimeUnit.MILLISECONDS);
				break;
			case STOP:
				scheduledFuture.cancel(false);
				break;
			case RESET:
				counter.set(0);
				break;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized <R> R onGetAttribute(AttributeDescriptor<R> attribute) {
		if (attribute.getAttributeName().equals("Counter") && attribute.getAttributeType() == Integer.class) {
			return (R) (Integer) counter.get();
		}
		return null;
	}

}
