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

package com.robo4j.core.httpunit.test.codec;

import com.robo4j.core.httpunit.HttpDecoder;
import com.robo4j.core.httpunit.HttpEncoder;
import com.robo4j.core.httpunit.HttpProducer;
import com.robo4j.core.httpunit.test.TestCommandEnum;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class CommandEnumToStringTestCodec implements HttpDecoder<TestCommandEnum>, HttpEncoder<TestCommandEnum> {

    @Override
    public String encode(TestCommandEnum stuff) {
        return stuff.toString();
    }

    @Override
    public TestCommandEnum decode(String json) {
        return TestCommandEnum.getByName(json);
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
