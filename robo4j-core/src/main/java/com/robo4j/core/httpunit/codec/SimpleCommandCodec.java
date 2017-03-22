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

package com.robo4j.core.httpunit.codec;

import com.robo4j.core.httpunit.HttpDecoder;
import com.robo4j.core.httpunit.HttpEncoder;
import com.robo4j.core.httpunit.HttpProducer;
import com.robo4j.core.util.ConstantUtil;

/**
 * default simple codec for simple commands
 *
 * @see SimpleCommand
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@HttpProducer
public class SimpleCommandCodec implements HttpDecoder<SimpleCommand>, HttpEncoder<SimpleCommand> {

	@Override
	public String encode(SimpleCommand stuff) {
		return "{ \"value\":\"" + stuff.getValue() + "\" }";
	}

	@Override
	public SimpleCommand decode(String json) {
		final String clearData = json.replaceAll("(value|:|\"|\\{|})", ConstantUtil.EMPTY_STRING).trim();
		return new SimpleCommand(clearData);
	}

	@Override
	public Class<SimpleCommand> getEncodedClass() {
		return SimpleCommand.class;
	}

	@Override
	public Class<SimpleCommand> getDecodedClass() {
		return SimpleCommand.class;
	}
}
