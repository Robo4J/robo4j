/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.units.SocketDecoder;
import com.robo4j.socket.http.units.SocketEncoder;
import com.robo4j.socket.http.units.HttpProducer;
import com.robo4j.socket.http.codec.SimpleCommand;
import com.robo4j.socket.http.codec.SimpleCommandCodec;
import com.robo4j.socket.http.units.test.enums.TestCommandEnum;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class CommandEnumToStringTestCodec implements SocketDecoder<String, TestCommandEnum>, SocketEncoder<TestCommandEnum, String> {

    private final SimpleCommandCodec codec = new SimpleCommandCodec();
    @Override
    public String encode(TestCommandEnum stuff) {
        final SimpleCommand simpleCommand = new SimpleCommand(stuff.getName());
        return codec.encode(simpleCommand);
    }

    @Override
    public TestCommandEnum decode(String json) {
        final SimpleCommand simpleCommand = codec.decode(json);
        return TestCommandEnum.getByName(simpleCommand.getValue());
    }

    @Override
    public Class<TestCommandEnum> getEncodedClass() {
        return TestCommandEnum.class;
    }

    @Override
    public Class<TestCommandEnum> getDecodedClass() {
        return TestCommandEnum.class;
    }
}
