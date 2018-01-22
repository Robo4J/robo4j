package com.robo4j.socket.http.util;

import com.robo4j.socket.http.units.test.enums.TestCommandEnum;

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
