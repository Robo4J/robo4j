package com.robo4j.socket.http.util;

import com.robo4j.socket.http.units.test.enums.TestCommandEnum;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ReflectUtilTests {


    @Test
    public void objectWithEnumToJson(){
        final String expectedJson = "{\"command\":\"MOVE\",\"desc\":\"some description\"}";
        TestCommand command = new TestCommand();
        command.setCommand(TestCommandEnum.MOVE);
        command.setDesc("some description");

        final String result = ReflectUtils.createJson(command);

        System.out.println("result: " + result);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.equals(expectedJson));

    }

    @Test
    public void objectWithEnumListToJson(){

        final String expectedJson = "{\"commands\":[\"MOVE\",\"STOP\",\"BACK\"],\"desc\":\"commands description\"}";
        TestCommandList commands = new TestCommandList();
        commands.setCommands(Arrays.asList(TestCommandEnum.MOVE, TestCommandEnum.STOP, TestCommandEnum.BACK));
        commands.setDesc("commands description");

        final String result = ReflectUtils.createJson(commands);
        System.out.println("result: " + result);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.equals(expectedJson));


    }
}
