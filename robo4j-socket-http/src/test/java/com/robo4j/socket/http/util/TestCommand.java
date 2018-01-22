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
