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
package com.robo4j.core.client.request;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.http.HttpHeaderNames;
import com.robo4j.http.HttpMessageWrapper;
import com.robo4j.http.HttpMethod;
import com.robo4j.http.util.HttpMessageUtil;

/**
 * Handling Request
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboRequestCallable implements Callable<String> {

    private static final String NEW_LINE = "\n";
    private static final String DEFAULT_RESPONSE = "done";

	private DefaultRequestFactory<String> factory;
    private Socket connection;


	public RoboRequestCallable(Socket connection, DefaultRequestFactory<String> factory) {
        this.connection = connection;
        this.factory = factory;
    }

    @Override
    public String call() throws Exception {

        if(connection == null){
            SimpleLoggingUtil.error(getClass(), "no connection");
        }

        try (Writer out = new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

			final String firstLine = RoboHttpUtils.correctLine(in.readLine());
			final String[] tokens = firstLine.split(ConstantUtil.HTTP_EMPTY_SEP);
			final HttpMethod method = HttpMethod.getByName(tokens[HttpMessageUtil.METHOD_KEY_POSITION]);

            final Map<String, String> params = new HashMap<>();
            String inputLine;
            while (!(inputLine = RoboHttpUtils.correctLine(in.readLine())).equals(ConstantUtil.EMPTY_STRING)) {
                final String[] array = inputLine
						.split(HttpMessageUtil.getHttpSeparator(HttpMessageUtil.HTTP_HEADER_SEP));
				params.put(array[HttpMessageUtil.METHOD_KEY_POSITION].toLowerCase(),
						array[HttpMessageUtil.URI_VALUE_POSITION]);
            }

			char[] buffer = null;
			if (method != null && method.equals(HttpMethod.POST)) {
				int length = Integer.valueOf(params.get(HttpHeaderNames.CONTENT_LENGTH).trim());
				buffer = new char[length];
				in.read(buffer);
			}

			processWriter(out, DEFAULT_RESPONSE);
			return parseHttpRequest(method, tokens, params, buffer);
        }

    }

    //Private Methods
	private void processWriter(final Writer out, String message) throws Exception {
        out.write(RoboHttpUtils.HTTP_HEADER_OK);
        Map<String, String> responseValues = new HashMap<>();
		responseValues.put(HttpHeaderNames.CONTENT_LENGTH, String.valueOf(message.length()));
        responseValues.forEach((k, v) -> {
            try {
                out.write(k + ": " + v + NEW_LINE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        out.write(NEW_LINE);
        out.write(message);
        out.flush();
    }

    //TODO, FIXME refactor
	private String parseHttpRequest(HttpMethod method, final String[] tokens, final Map<String, String> params,
			char[] buffer) {
		if (method != null && tokens != null) {
			/* maybe validation here */
            switch (method) {
                case GET:
				return factory.processGet(new HttpMessageWrapper(method, tokens, params));
			case POST:
				return factory.processPost(new HttpMessageWrapper(method, tokens, params, buffer));
                default:
                    SimpleLoggingUtil.debug(getClass(), "not implemented method: " + method);
                    return null;
            }
        }
        return null;
    }

}

