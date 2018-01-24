package com.robo4j.socket.http.units;

import com.robo4j.RoboContext;

import java.util.Collection;

/**
 * interface for client, server context builder
 *
 * @see ServerContextBuilder
 * @see ClientContextBuilder
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public interface ContextBuilder<T, C extends HttpContext<?>> {

	ContextBuilder<T,C> addPaths(Collection<T> paths);

	C build(RoboContext context);

}
