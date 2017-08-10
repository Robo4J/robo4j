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

package com.robo4j.core.httpunit.test;

import com.robo4j.core.ConfigurationException;
import com.robo4j.core.LifecycleState;
import com.robo4j.core.RoboContext;
import com.robo4j.core.RoboUnit;
import com.robo4j.core.configuration.Configuration;
import com.robo4j.core.httpunit.codec.CameraMessage;
import com.robo4j.core.logging.SimpleLoggingUtil;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class TestServerImageController extends RoboUnit<CameraMessage> {

    private String target;

	public TestServerImageController(RoboContext context, String id) {
		super(CameraMessage.class, context, id);
	}

    @Override
    public void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString("target", null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException("target");
        }
    }

    @Override
    public void onMessage(CameraMessage message) {
        SimpleLoggingUtil.print(getClass(), "onMessage target: "+ target + ", message: " + message );
        getContext().getReference(target).sendMessage(message);
    }

    @Override
    public void stop() {
        setState(LifecycleState.STOPPING);
        setState(LifecycleState.STOPPED);
    }

    @Override
    public void shutdown() {
        setState(LifecycleState.SHUTTING_DOWN);
        setState(LifecycleState.SHUTDOWN);
        System.exit(0);
    }

}
