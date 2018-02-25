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

package com.robo4j.units.lego.example.codec;

import com.robo4j.socket.http.units.SocketDecoder;
import com.robo4j.socket.http.units.SocketEncoder;
import com.robo4j.socket.http.units.HttpProducer;
import com.robo4j.units.lego.enums.LegoPlatformMessageTypeEnum;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@HttpProducer
public class LegoPlatformMessageTypeEnumToStringTestCodec implements SocketDecoder<String, LegoPlatformMessageTypeEnum>, SocketEncoder<LegoPlatformMessageTypeEnum, String> {

    @Override
    public String encode(LegoPlatformMessageTypeEnum stuff) {
        return null;
    }

    @Override
    public LegoPlatformMessageTypeEnum decode(String json) {
        return LegoPlatformMessageTypeEnum.getByName(json);
    }

    @Override
    public Class<LegoPlatformMessageTypeEnum> getEncodedClass() {
        return LegoPlatformMessageTypeEnum.class;
    }

    @Override
    public Class<LegoPlatformMessageTypeEnum> getDecodedClass() {
        return LegoPlatformMessageTypeEnum.class;
    }
}
