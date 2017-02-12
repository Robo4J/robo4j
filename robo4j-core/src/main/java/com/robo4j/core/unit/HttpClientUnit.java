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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.unit;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboReference;
import com.robo4j.core.RoboResult;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.concurrency.RoboThreadFactory;
import com.robo4j.core.configuration.Configuration;

/**
 * Http NIO Client to communicate with external system/Robo4J units
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpClientUnit extends RoboUnit<Object> {

	private static final Set<LifecycleState> activeStates = EnumSet.of(LifecycleState.STARTED, LifecycleState.STARTING);
	private final ExecutorService executor = new ThreadPoolExecutor(RoboHttpUtils.DEFAULT_THREAD_POOL_SIZE,
			RoboHttpUtils.DEFAULT_THREAD_POOL_SIZE, RoboHttpUtils.KEEP_ALIVE_TIME, TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(), new RoboThreadFactory("Robo4J HttpClientUnit ", true));
	private boolean available;
    private Integer port;
    private String adress;

    public HttpClientUnit(RoboContext context, String id) {
        super(context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        setState(LifecycleState.UNINITIALIZED);
        adress = configuration.getString("address", null);
        port = configuration.getInteger("port", RoboHttpUtils._DEFAULT_PORT);

        final Configuration commands = configuration.getChildConfiguration(RoboHttpUtils.HTTP_COMMAND.concat("s"));
        if (adress == null && port == null && commands == null) {
            throw ConfigurationException.createMissingConfigNameException("address, method, path, commands...");
        }
        setState(LifecycleState.INITIALIZED);
    }

    @SuppressWarnings("unchecked")
    @Override
    public RoboResult<Object, ?> onMessage(Object message) {

        System.out.println(getClass().getSimpleName() + ": onMassage: " + message);

        return super.onMessage(message);
    }

    @Override
    public void start() {
        setState(LifecycleState.STARTING);
        final RoboReference<String> addressRef = getContext().getReference(adress);

        available = true;

        setState(LifecycleState.STARTED);
    }

    @Override
    public void stop() {
        setState(LifecycleState.STOPPING);
        System.out.println(getClass().getSimpleName() + " : stop");
        setState(LifecycleState.STOPPED);
    }

    @Override
    public void shutdown() {
        setState(LifecycleState.SHUTTING_DOWN);
        executor.shutdownNow();
        System.out.println(getClass().getSimpleName() + " : shutdown");
        setState(LifecycleState.SHUTDOWN);
    }


}
