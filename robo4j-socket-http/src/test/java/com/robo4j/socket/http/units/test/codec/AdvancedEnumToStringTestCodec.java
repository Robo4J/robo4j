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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.codec.SimpleCommand;
import com.robo4j.socket.http.codec.SimpleCommandCodec;
import com.robo4j.socket.http.units.SocketDecoder;
import com.robo4j.socket.http.units.SocketEncoder;
import com.robo4j.socket.http.units.HttpProducer;
import com.robo4j.socket.http.units.test.enums.AdvancedTestCommandEnum;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@HttpProducer
public class AdvancedEnumToStringTestCodec
		implements SocketDecoder<String, AdvancedTestCommandEnum>, SocketEncoder<AdvancedTestCommandEnum, String> {


    private final SimpleCommandCodec codec = new SimpleCommandCodec();
    @Override
    public String encode(AdvancedTestCommandEnum stuff) {
        final SimpleCommand simpleCommand = new SimpleCommand(stuff.getName());
        return codec.encode(simpleCommand);
    }

    @Override
    public AdvancedTestCommandEnum decode(String json) {
        final SimpleCommand simpleCommand = codec.decode(json);
        return AdvancedTestCommandEnum.getByName(simpleCommand.getValue());
    }

    @Override
    public Class<AdvancedTestCommandEnum> getEncodedClass() {
        return AdvancedTestCommandEnum.class;
    }

    @Override
    public Class<AdvancedTestCommandEnum> getDecodedClass() {
        return AdvancedTestCommandEnum.class;
    }

}
