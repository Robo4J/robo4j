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

package com.robo4j.socket.http.request;

import com.robo4j.RoboContext;
import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.units.ServerContext;
import com.robo4j.socket.http.units.ServerPathConfig;

import java.util.Objects;
import java.util.concurrent.Callable;

import static com.robo4j.util.Utf8Constant.UTF8_SOLIDUS;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboRequestCallable implements Callable<HttpResponseProcess> {

	private final RoboContext context;
	private final ServerContext serverContext;
	private final HttpDecoratedRequest decoratedRequest;
	private final DefaultRequestFactory<?> factory;

	public RoboRequestCallable(RoboContext context, ServerContext serverContext, HttpDecoratedRequest decoratedRequest,
			DefaultRequestFactory<Object> factory) {
		Objects.requireNonNull(context, "not allowed empty context");
		Objects.requireNonNull(serverContext, "not allowed empty serverContext");
		this.context = context;
		this.serverContext = serverContext;
		this.decoratedRequest = decoratedRequest;
		this.factory = factory;
	}

	@Override
	public HttpResponseProcess call() throws Exception {

		final HttpResponseProcessBuilder resultBuilder = HttpResponseProcessBuilder.Builder();
		final ServerPathConfig pathConfig = serverContext.getPathConfig(decoratedRequest.getPathMethod());

		if (isValidPath(pathConfig)) {
			resultBuilder.setMethod(pathConfig.getMethod());
			resultBuilder.setPath(pathConfig.getPath());

			switch (pathConfig.getMethod()) {
			case GET:
				if (pathConfig.getPath().equals(UTF8_SOLIDUS)) {
					resultBuilder.setCode(StatusCode.OK);
					resultBuilder.setResult(factory.processGet(context));
				} else {
					resultBuilder.setTarget(pathConfig.getRoboUnit().getId());
					final Object unitDescription = factory.processGet(pathConfig);
					resultBuilder.setCode(StatusCode.OK);
					resultBuilder.setResult(unitDescription);
				}
				break;
			case POST:
				if (pathConfig.getPath().equals(UTF8_SOLIDUS)) {
					resultBuilder.setCode(StatusCode.BAD_REQUEST);
				} else {
					resultBuilder.setTarget(pathConfig.getRoboUnit().getId());
					Object respObj = factory.processPost(pathConfig.getRoboUnit(), decoratedRequest.getMessage());
					if (respObj == null) {
						resultBuilder.setCode(StatusCode.BAD_REQUEST);
					} else {
						resultBuilder.setCode(StatusCode.ACCEPTED);
						resultBuilder.setResult(respObj);
					}
				}
				break;
			default:
				resultBuilder.setCode(StatusCode.BAD_REQUEST);
				SimpleLoggingUtil.debug(getClass(), "not implemented method: " + decoratedRequest.getPathMethod());
			}
		} else {
			resultBuilder.setCode(StatusCode.BAD_REQUEST);
		}
		return resultBuilder.build();
	}

	private boolean isValidPath(ServerPathConfig pathConfig) {
		return pathConfig != null && decoratedRequest.getPathMethod() != null
				&& decoratedRequest.getPathMethod().getMethod().equals(pathConfig.getMethod());
	}

}
