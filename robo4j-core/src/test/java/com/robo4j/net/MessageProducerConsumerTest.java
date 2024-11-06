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

package com.robo4j.net;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageProducerConsumerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageProducerConsumerTest.class);
    private static final int TIMEOUT_SEC = 30;
    private static final String CONST_MYUUID = "myuuid";
    private static final String PROPERTY_SERVER_NAME = "ServerName";
    private static final int SERVER_LISTEN_DELAY_MILLIS = 250;
}
