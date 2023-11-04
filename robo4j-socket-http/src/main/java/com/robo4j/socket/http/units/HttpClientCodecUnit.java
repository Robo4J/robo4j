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
package com.robo4j.socket.http.units;

import com.robo4j.CriticalSectionTrait;
import com.robo4j.RoboContext;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpRequestDenominator;

import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;

/**
 * HttpClientCodecUnit accepts message wrapper and provides proper decoration before
 * the final result is send to the http client.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@CriticalSectionTrait
public final class HttpClientCodecUnit extends AbstractClientCodecUnit {

	public HttpClientCodecUnit(RoboContext clientContext, String id) {
		super(ClientMessageWrapper.class, clientContext, id);
	}

	@Override
	public void onMessage(ClientMessageWrapper message) {

		final String encodedMessage = clientContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY)
				.containsEncoder(message.getClazz()) ? processMessage(message.getClazz(), message.getMessage())
						: processMessage(String.class, message.toString());

		ClientPathConfig pathConfig = clientContext.getPathConfig(new PathHttpMethod(message.getPath(), HttpMethod.POST));
		final HttpRequestDenominator denominator = new HttpRequestDenominator(pathConfig.getMethod(), pathConfig.getPath(),
				HttpVersion.HTTP_1_1);
		final HttpDecoratedRequest request = new HttpDecoratedRequest(denominator);
		request.addMessage(encodedMessage);

		request.addCallbacks(pathConfig.getCallbacks());
		getContext().getReference(target).sendMessage(request);
	}

	private String processMessage(Class<?> clazz, Object message) {
		return processMessage(message,
				clientContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY).getEncoder(clazz));
	}

	@SuppressWarnings("unchecked")
	private <T> String processMessage(T message, SocketEncoder<?, String> encoder) {
		return ((SocketEncoder<T, String>) encoder).encode(message);
	}

}
