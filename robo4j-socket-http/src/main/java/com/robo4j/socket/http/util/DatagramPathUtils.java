package com.robo4j.socket.http.util;

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.dto.ClientPathDTO;
import com.robo4j.socket.http.dto.ServerUnitPathDTO;
import com.robo4j.socket.http.enums.SystemPath;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.units.ClientPathConfig;
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
			final Collection<ServerUnitPathDTO> paths) {
		final Map<String, ServerPathConfig> resultPaths = paths.stream().map(e -> {
			RoboReference<Object> reference = context.getReference(e.getRoboUnit());
			return new ServerPathConfig(toPath(SystemPath.UNITS.getPath(), e.getRoboUnit()), reference, e.getMethod(), e.getFilters());
		}).collect(Collectors.toMap(ServerPathConfig::getPath, e -> e));
		serverContext.addPaths(resultPaths);
	}

	public static void updateDatagramClientContextPaths(final ClientContext clientContext, final Collection<ClientPathDTO> paths){
		final Map<String, ClientPathConfig> resultPaths = paths.stream()
				.map(e -> new ClientPathConfig(toPath(SystemPath.UNITS.getPath(), e.getRoboUnit()), e.getMethod(), e.getCallbacks()))
				.collect(Collectors.toMap(ClientPathConfig::getPath, e -> e));
		clientContext.addPaths(resultPaths);
	}
}
