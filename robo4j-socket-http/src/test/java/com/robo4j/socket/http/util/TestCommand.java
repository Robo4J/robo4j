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
package com.robo4j.socket.http.util;

import com.robo4j.socket.http.units.test.enums.TestCommandEnum;

/**
 * Test command with Enum field and description
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class TestCommand {

	private TestCommandEnum command;
	private String desc;

	public TestCommand() {
	}

	public TestCommandEnum getCommand() {
		return command;
	}

	public void setCommand(TestCommandEnum command) {
		this.command = command;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

    @Override
    public String toString() {
        return "TestCommand{" +
                "command=" + command +
                ", desc='" + desc + '\'' +
                '}';
    }
}
