/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.test.utils;

import com.robo4j.socket.http.test.units.config.enums.TestCommandEnum;

import java.util.List;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class TestCommandList {

    private List<TestCommandEnum> commands;
    private String desc;

    public TestCommandList() {
    }

    public List<TestCommandEnum> getCommands() {
        return commands;
    }

    public void setCommands(List<TestCommandEnum> commands) {
        this.commands = commands;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "TestCommandList{" +
                "commands=" + commands +
                ", desc='" + desc + '\'' +
                '}';
    }
}
