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

import com.robo4j.RoboContext;
import com.robo4j.spring.configuration.robo4j.StringConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * SpringReceiverService get messages from instantiated robo4j context
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

@Service
public class SpringReceiverService {

	private final RoboContext roboContext;

	@Autowired
	public SpringReceiverService(RoboContext roboContext) {
		this.roboContext = roboContext;
	}

	@SuppressWarnings("unchecked")
	public List<String> getMessages() {
		try {
			return roboContext.getReference(StringConsumer.NAME).getAttribute(StringConsumer.DESCRIPTOR_TOTAL_MESSAGES)
					.get();
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
