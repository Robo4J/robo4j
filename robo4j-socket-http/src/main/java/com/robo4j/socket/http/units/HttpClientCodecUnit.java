package com.robo4j.socket.http.units;

import com.robo4j.ConfigurationException;
import com.robo4j.CriticalSectionTrait;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.message.HttpRequestDenominator;
import com.robo4j.socket.http.util.RoboHttpUtils;

import java.util.List;
import java.util.Objects;

import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;
import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_TARGET;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_UNIT_PATHS_CONFIG;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@CriticalSectionTrait
public final class HttpClientCodecUnit extends RoboUnit<HttpClientMessageWrapper> {

	private final ClientContext clientContext = new ClientContext();
	private final CodecRegistry codecRegistry = new CodecRegistry();
	private String target;

	public HttpClientCodecUnit(RoboContext clientContext, String id) {
		super(HttpClientMessageWrapper.class, clientContext, id);
	}

	@Override
	protected void onInitialization(Configuration configuration) throws ConfigurationException {
		target = configuration.getString(HTTP_PROPERTY_TARGET, null);
		Objects.requireNonNull(target, "empty target");

		final List<ClientPathDTO> paths = JsonUtil.readPathConfig(ClientPathDTO.class,
				configuration.getString(PROPERTY_UNIT_PATHS_CONFIG, null));
		if (paths.isEmpty()) {
			throw ConfigurationException.createMissingConfigNameException(PROPERTY_UNIT_PATHS_CONFIG);
		}
		HttpPathUtils.updateHttpClientContextPaths(clientContext, paths);

		String packages = configuration.getString(PROPERTY_CODEC_PACKAGES, null);
		if (RoboHttpUtils.validatePackages(packages)) {
			codecRegistry.scan(Thread.currentThread().getContextClassLoader(), packages.split(UTF8_COMMA));
			clientContext.putProperty(PROPERTY_CODEC_REGISTRY, codecRegistry);
		} else {
			throw new IllegalStateException("not available codec packages");
		}
	}

	@Override
	public void onMessage(HttpClientMessageWrapper message) {

		final String encodedMessage = clientContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY)
				.containsEncoder(message.getClazz()) ? processMessage(message.getClazz(), message.getMessage())
						: processMessage(String.class, message.toString());

		ClientPathConfig pathConfig = clientContext.getPathConfig(message.getPath());
		final HttpRequestDenominator denominator = new HttpRequestDenominator(pathConfig.getMethod(), pathConfig.getPath(),
				HttpVersion.HTTP_1_1);
		final HttpDecoratedRequest request = new HttpDecoratedRequest(denominator);
		request.addMessage(encodedMessage);

		// FIXME: 1/27/18 (miro) -> fix null list
		if (pathConfig.getCallbacks() != null) {
			request.addCallbacks(pathConfig.getCallbacks());
		}

		getContext().getReference(target).sendMessage(request);
	}

	private String processMessage(Class<?> clazz, Object message) {
		return processMessage(message,
				clientContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY).getEncoder(clazz));
	}

	@SuppressWarnings("unchecked")
	private <T> String processMessage(T message, HttpEncoder<?> encoder) {
		return ((HttpEncoder<T>) encoder).encode(message);
	}

}
