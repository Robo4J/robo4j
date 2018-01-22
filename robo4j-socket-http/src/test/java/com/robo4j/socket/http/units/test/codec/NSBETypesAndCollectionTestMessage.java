package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.units.test.enums.TestCommandEnum;

import java.util.List;
import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class NSBETypesAndCollectionTestMessage {

    private Integer number;
    private String message;
    private Boolean active;
    private TestCommandEnum command;
    private List<TestCommandEnum> commands;

    public NSBETypesAndCollectionTestMessage() {
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public TestCommandEnum getCommand() {
        return command;
    }

    public void setCommand(TestCommandEnum command) {
        this.command = command;
    }

    public List<TestCommandEnum> getCommands() {
        return commands;
    }

    public void setCommands(List<TestCommandEnum> commands) {
        this.commands = commands;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NSBETypesAndCollectionTestMessage that = (NSBETypesAndCollectionTestMessage) o;
        return Objects.equals(number, that.number) &&
                Objects.equals(message, that.message) &&
                Objects.equals(active, that.active) &&
                command == that.command &&
                Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {

        return Objects.hash(number, message, active, command, commands);
    }

    @Override
    public String toString() {
        return "NSBETypesAndCollectionTestMessage{" +
                "number=" + number +
                ", message='" + message + '\'' +
                ", active=" + active +
                ", command=" + command +
                ", commands=" + commands +
                '}';
    }
}
