/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.spring.configuration.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.robo4j.RoboContext;

/**
 * SimpleScheduler
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

@Component
public class SimpleScheduler {

	public static final int MESSAGES_NUMBER = 100;

	private static final Log log = LogFactory.getLog(SimpleScheduler.class);
	private static final String ROBO4J_UNIT_NAME = "consumer";
	private final RoboContext roboContext;

	@Autowired
	public SimpleScheduler(RoboContext roboContext) {
		this.roboContext = roboContext;
	}

	@Scheduled(fixedDelay = 1000)
	public void emmitMessages() {
		for (int i = 0; i < MESSAGES_NUMBER; i++) {
			roboContext.getReference(ROBO4J_UNIT_NAME).sendMessage("message:" + i);
		}
        log.info("DONE");
	}

}
