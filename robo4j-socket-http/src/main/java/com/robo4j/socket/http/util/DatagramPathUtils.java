package com.robo4j.socket.http.util;

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.enums.SystemPath;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.units.ClientPathConfig;
import com.robo4j.socket.http.units.PathHttpMethod;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.ServerPathConfig;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import static com.robo4j.socket.http.util.HttpPathUtils.toPath;

/**
 * Datagram Utils
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class DatagramPathUtils {

	public static void updateDatagramServerContextPaths(final RoboContext context, final ServerContext serverContext,
			final Collection<HttpPathMethodDTO> paths) {
		final Map<PathHttpMethod, ServerPathConfig> resultPaths = paths.stream().map(e -> {
			RoboReference<Object> reference = context.getReference(e.getRoboUnit());
			return new ServerPathConfig(toPath(SystemPath.UNITS.getPath(), e.getRoboUnit()), reference, e.getMethod(), e.getCallbacks());
		}).collect(Collectors.toMap(e -> new PathHttpMethod(e.getPath(), null), e -> e));
		serverContext.addPaths(resultPaths);
	}

	public static void updateDatagramClientContextPaths(final ClientContext clientContext, final Collection<HttpPathMethodDTO> paths){
		final Map<PathHttpMethod, ClientPathConfig> resultPaths = paths.stream()
				.map(e -> new ClientPathConfig(toPath(SystemPath.UNITS.getPath(), e.getRoboUnit()), e.getMethod(), e.getCallbacks()))
				.collect(Collectors.toMap(e -> new PathHttpMethod(e.getPath(), null), e -> e));
		clientContext.addPaths(resultPaths);
	}
}
