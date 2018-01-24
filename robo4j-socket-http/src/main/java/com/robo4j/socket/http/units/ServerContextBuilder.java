package com.robo4j.socket.http.units;

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.ServerUnitPathDTO;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.util.Utf8Constant;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ServerContextBuilder implements ContextBuilder<ServerUnitPathDTO, ServerContext> {

	private final List<ServerUnitPathDTO> paths;

	private ServerContextBuilder() {
		this.paths = new LinkedList<>();
	}

	public static ServerContextBuilder Builder() {
		return new ServerContextBuilder();
	}

	@Override
	public ServerContextBuilder addPaths(Collection<ServerUnitPathDTO> paths) {
		this.paths.addAll(paths);
		return this;
	}

	@Override
	public ServerContext build(RoboContext context) {
		final Map<String, ServerPathConfig> resultPaths = paths.stream().map(e -> {
			RoboReference<Object> reference = context.getReference(e.getRoboUnit());
			return HttpPathUtils.toServerPathConfig(e, reference);
		}).collect(Collectors.toMap(ServerPathConfig::getPath, e -> e));
		resultPaths.put(Utf8Constant.UTF8_SOLIDUS,
				new ServerPathConfig(Utf8Constant.UTF8_SOLIDUS, null, HttpMethod.GET));
		return new ServerContext(resultPaths);
	}

}
