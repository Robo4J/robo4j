/*
 * Copyright (C)  2016. Miroslav Wengner, Marcus Hirt
 * This AgentStatus.java  is part of robo4j.
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

package com.robo4j.commons.agent;

import java.util.LinkedList;
import java.util.List;

/**
 * Agent status is communication between Agent and the rest of the application
 *
 * @author Miroslav Wengner (@miragemiko)
 * @since 29.05.2016
 */
public class AgentStatus<T> {

	private AgentStatusEnum status;
	private List<T> messages;

	public AgentStatus(AgentStatusEnum status) {
		this.status = status;
		this.messages = new LinkedList<>();
	}

	public AgentStatusEnum getStatus() {
		return status;
	}

	public void addMessage(T message) {
		messages.add(message);
	}

	public List<T> getMessages() {
		return messages;
	}

	@Override
	public String toString() {
		return "AgentStatus{" + "status=" + status + ", messages=" + messages + '}';
	}
}
