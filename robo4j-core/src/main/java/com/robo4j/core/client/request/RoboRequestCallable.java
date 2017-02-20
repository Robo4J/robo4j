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
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.robo4j.core.RoboUnit;
import com.robo4j.core.client.util.RoboHttpUtils;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.http.HttpHeaderNames;
import com.robo4j.http.HttpMessage;
import com.robo4j.http.HttpMessageWrapper;
import com.robo4j.http.HttpMethod;
import com.robo4j.http.HttpVersion;
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

    private final Socket connection;
    private final DefaultRequestFactory<String> factory;
    private final List<RoboUnit<?>> registeredUnits;


    public RoboRequestCallable(Socket connection, DefaultRequestFactory<String> factory, List<RoboUnit<?>> registeredUnits) {
        assert connection != null;
        this.connection = connection;
        this.factory = factory;
        this.registeredUnits = registeredUnits;
    }

    @Override
    public String call() throws Exception {
        try (Writer out = new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()));
             BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

			final String firstLine = RoboHttpUtils.correctLine(in.readLine());
			final String[] tokens = firstLine.split(ConstantUtil.HTTP_EMPTY_SEP);
			final HttpMethod method = HttpMethod.getByName(tokens[HttpMessageUtil.METHOD_KEY_POSITION]);

            if(method != null){
                final Map<String, String> params = new HashMap<>();

                String inputLine;
                while (!(inputLine = RoboHttpUtils.correctLine(in.readLine())).equals(ConstantUtil.EMPTY_STRING)) {
                    final String[] array = inputLine
                            .split(HttpMessageUtil.getHttpSeparator(HttpMessageUtil.HTTP_HEADER_SEP));
                    params.put(array[HttpMessageUtil.METHOD_KEY_POSITION].toLowerCase(),
                            array[HttpMessageUtil.URI_VALUE_POSITION]);
                }

                /* parsed http specifics, header */
                final HttpMessage httpMessage = new HttpMessage(method, URI.create(tokens[HttpMessageUtil.URI_VALUE_POSITION]),
                        HttpVersion.getByValue(tokens[HttpMessageUtil.VERSION_POSITION]), params);

                //TODO -> improve response -> here can be printed out basic system
                final String targetUnitId = httpMessage.uri().getPath().replace("/", "");
                System.out.println("RoboRequestCallable -> targetUnitId: " +  targetUnitId);

                //@formatter:off
                final List<RoboUnit<?>> desiredUnits = registeredUnits.stream()
                        .filter(u -> u.getId().equals(targetUnitId))
                        .collect(Collectors.toList());
                factory.setRoboUnits(desiredUnits);
                //@formatter:on
                System.out.println("RoboRequestCallable -> desiredUnits: " + desiredUnits);
                processWriter(out, DEFAULT_RESPONSE);
                switch (method){
                    case GET:
                        return factory.processGet(new HttpMessageWrapper<>(httpMessage));
                    case POST:
                        int length = Integer.valueOf(params.get(HttpHeaderNames.CONTENT_LENGTH).trim());
                        char[] buffer = new char[length];
                        in.read(buffer);
                        return factory.processPost(new HttpMessageWrapper<>(httpMessage, buffer));
                    default:
                        SimpleLoggingUtil.debug(getClass(), "not implemented method: " + method);
                        return null;
                }
            } else {
                SimpleLoggingUtil.error(getClass(), "error");
            }
        }
        return null;
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


}

