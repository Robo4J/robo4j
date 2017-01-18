/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This CommandProcessor.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.client.command;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.command.AdafruitLcdCommandEnum;
import com.robo4j.commons.command.GenericCommand;
import com.robo4j.commons.concurrent.QueueFIFOEntry;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.core.bus.ClientBusQueue;
import com.robo4j.commons.command.PlatformUnitCommandEnum;
import com.robo4j.core.dto.ClientAdafruitLcdCommandRequestDTO;
import com.robo4j.core.dto.ClientCommandDTO;
import com.robo4j.core.dto.ClientMotorCommandRequestDTO;
import com.robo4j.core.util.ConstantUtil;

/**
 *
 * Command Processor is singleton Command Processor is producer
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 09.06.2016
 */
public final class CommandProcessor implements AgentProducer, Runnable {

	private static final int AWAIT_SECONDS = 2;
	private volatile AtomicBoolean active;
	private volatile ClientBusQueue messageQueue;
	private volatile LinkedBlockingQueue<List<ClientCommandDTO<?>>> inputQueue;

	public CommandProcessor(AtomicBoolean active, LinkedBlockingQueue<List<ClientCommandDTO<?>>> inputQueue) {
		messageQueue = new ClientBusQueue<QueueFIFOEntry<?>>(AWAIT_SECONDS);
		this.active = active;
		this.inputQueue = inputQueue;
		SimpleLoggingUtil.print(getClass(), "PRODUCER UP");
	}

	@Override
	public ClientBusQueue getMessageQueue() {
		return messageQueue;
	}

	@Override
	@SuppressWarnings(value = "unchecked")
	public void run() {
		// TODO: improve this part separate
		try {
			while (active.get()) {
				final List<ClientCommandDTO<?>> commandQueue = inputQueue.take();
				for (ClientCommandDTO<?> element : commandQueue) {

					if(element instanceof ClientMotorCommandRequestDTO){
						ClientMotorCommandRequestDTO commandElement = (ClientMotorCommandRequestDTO) element;
						messageQueue.transfer(getCommand(commandElement.getCommand(), element.getValue(), commandElement.getSpeed()));

					}

					if(element instanceof ClientAdafruitLcdCommandRequestDTO){
						SimpleLoggingUtil.debug(getClass(), "ALMOST DONE: " + element);
						messageQueue.transfer(getCommand(((ClientAdafruitLcdCommandRequestDTO) element).getCommand()));
					}

				}

			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	// Private Methods
	@SuppressWarnings(value = "unchecked")
	private QueueFIFOEntry getCommand(PlatformUnitCommandEnum type, String value, String speed) {
		/* client command holding default values */
		final ClientCommandProperties properties = new ClientCommandProperties(Integer.parseInt(speed));
		final GenericCommand<PlatformUnitCommandEnum> command = new GenericCommand<>(properties, type, value,
				ConstantUtil.DEFAULT_PRIORITY);
		return new QueueFIFOEntry<>(command);
	}

	@SuppressWarnings(value = "unchecked")
	private QueueFIFOEntry getCommand(AdafruitLcdCommandEnum element){
		final ClientCommandProperties properties = new ClientCommandProperties(0);
		final GenericCommand<AdafruitLcdCommandEnum> command = new GenericCommand<>(properties, element, "",
				ConstantUtil.DEFAULT_PRIORITY);
		return new QueueFIFOEntry<>(command);
	}
}
