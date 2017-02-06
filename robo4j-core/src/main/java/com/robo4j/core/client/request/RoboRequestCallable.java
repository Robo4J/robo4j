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

import com.robo4j.core.client.util.HttpUtils;
import com.robo4j.core.logging.SimpleLoggingUtil;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.http.HttpMessage;
import com.robo4j.http.HttpMethod;
import com.robo4j.http.HttpVersion;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Miro Wengner (@miragemiko)
 */
public class RoboRequestCallable implements Callable<String> {

    private static final String NEW_LINE = "\n";
    private static final String DEFAULT_RESPONSE = "done";
    private static final int METHOD_KEY_POSITION = 0, URI_VALUE_POSITION = 1, VERSION_POSITION = 2, HTTP_HEADER_SEP = 9;

    private DefaultRequestFactory<?> factory;
    private Socket connection;


    public RoboRequestCallable(Socket connection, DefaultRequestFactory<?> factory) {
        this.connection = connection;
        this.factory = factory;
    }

    @Override
    public String call() throws Exception {

        if(connection == null){
            SimpleLoggingUtil.error(getClass(), "no connection");
        }

        try (final Writer out = new OutputStreamWriter(new BufferedOutputStream(connection.getOutputStream()));
             final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {

            final Map<String, String> params = new HashMap<>();
            boolean firstLine = true;
            String[] tokens = null;
            HttpMethod method = null;
            String inputLine;

            //TODO: refactor
            while (!(inputLine = HttpUtils.correctLine(in.readLine())).equals(ConstantUtil.EMPTY_STRING)) {
                if (firstLine) {
                    tokens = inputLine.split(ConstantUtil.HTTP_EMPTY_SEP);
                    method = HttpMethod.getByName(tokens[METHOD_KEY_POSITION]);
                    SimpleLoggingUtil.debug(getClass(), "header method: " + method);
                    firstLine = false;
                } else {
                    final String[] array = inputLine.split(ConstantUtil.getHttpSeparator(HTTP_HEADER_SEP));
                    SimpleLoggingUtil.debug(getClass(), "header without method: " + Arrays.asList(array));
                    params.put(array[METHOD_KEY_POSITION], array[URI_VALUE_POSITION]);
                }
            }
            processWritter(out, DEFAULT_RESPONSE);

            return tokens == null ? null : parseHttpRequest(method, tokens, params);
        }

    }

    //Private Methods
    private void processWritter(final Writer out, String message) throws Exception{
        out.write("HTTP/1.1 200 OK\n");
        Map<String, String> responseValues = new HashMap<>();
        responseValues.put("Content-Length", String.valueOf(message.length()));
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

    private String parseHttpRequest(HttpMethod method, final String[] tokens, final Map<String, String> params){
     final HttpMessage httpMessage = new HttpMessage(method, URI.create(tokens[URI_VALUE_POSITION]),
             HttpVersion.getByValue(tokens[VERSION_POSITION]), params);
     switch (method){
         case GET:
             //TODO, FIXME refactor
             return factory.processGet(httpMessage).toString();
         default:
             SimpleLoggingUtil.debug(getClass(), "not implemented method: " + method);
             return null;
     }
    }
}

