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
import com.robo4j.socket.http.HttpMessage;
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

		final HttpResponseProcess result = new HttpResponseProcess();
		final ServerPathConfig pathConfig = serverContext.getPathConfig(decoratedRequest.getPath());

		if (pathConfig.getMethod().equals(decoratedRequest.getMethod())) {
			result.setMethod(pathConfig.getMethod());

			// TODO: 1/23/18 (miro) http message wrap headers
			final HttpMessage httpMessage = new HttpMessage(decoratedRequest);
			result.setPath(pathConfig.getPath());

			switch (httpMessage.method()) {
			case GET:
				if (pathConfig.getPath().equals(UTF8_SOLIDUS)) {
					result.setCode(StatusCode.OK);
					result.setResult(factory.processGet(context));
				} else {
					result.setTarget(pathConfig.getRoboUnit().getId());
					final Object unitDescription = factory.processGet(pathConfig);
					result.setCode(StatusCode.OK);
					result.setResult(unitDescription);
				}
				return result;
			case POST:
				final String postValue = decoratedRequest.getMessage();
				if (pathConfig.getPath().equals(UTF8_SOLIDUS)) {
					result.setCode(StatusCode.NOT_IMPLEMENTED);
				} else {
					result.setTarget(pathConfig.getRoboUnit().getId());
					Object respObj = factory.processPost(pathConfig.getRoboUnit(), postValue);
					result.setCode(StatusCode.ACCEPTED);
					result.setResult(respObj);
				}
				return result;

			default:
				result.setCode(StatusCode.BAD_REQUEST);
				SimpleLoggingUtil.debug(getClass(), "not implemented method: " + decoratedRequest.getMethod());
			}
		} else {
			result.setCode(StatusCode.BAD_REQUEST);
		}

		return result;
	}

}
