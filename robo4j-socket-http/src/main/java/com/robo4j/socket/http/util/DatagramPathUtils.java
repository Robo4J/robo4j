package com.robo4j.socket.http.util;

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.dto.ServerUnitPathDTO;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.ServerPathConfig;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

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
			return new ServerPathConfig(e.getRoboUnit(), reference, e.getMethod(), e.getFilters());
		}).collect(Collectors.toMap(ServerPathConfig::getPath, e -> e));
		serverContext.addPaths(resultPaths);
	}
}
