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
