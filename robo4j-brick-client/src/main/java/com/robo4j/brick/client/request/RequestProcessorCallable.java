/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This RequestProcessorCallable.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.brick.client.request;


import com.robo4j.brick.client.http.HttpMessage;
import com.robo4j.brick.client.http.HttpMethod;
import com.robo4j.brick.client.http.HttpVersion;
import com.robo4j.brick.client.util.HttpUtils;
import com.robo4j.brick.util.ConstantUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Responsible for handling incoming request
 *
 * Created by miroslavkopecky on 28/02/16.
 */
public class RequestProcessorCallable implements Callable<String> {

    private static final int METHOD_KEY_POSITION = 0, URI_VALUE_POSITION = 1, VERSION_POSITION = 2, HTTP_HEADER_SEP = 9;
    private final RequestProcessorFactory processorFactory;
    private Socket connection;

    public RequestProcessorCallable(Socket connection) {
        this.connection = connection;
        this.processorFactory = RequestProcessorFactory.getInstance();
    }

    private static String correctLine(String line){
        return line == null ? "" : line;
    }

    @Override
    public String call() throws IOException{
        // for security checks
        String result = HttpUtils.STRING_EMPTY;
        try(OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
            Writer out =  new OutputStreamWriter(raw);
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))){

            final Map<String, String> tmpMap = new ConcurrentHashMap<>();
            boolean firstLine = true;
            String[] tokens = null;
            String method = null;
            String inputLine;


            while (!(inputLine = correctLine(in.readLine())).equals(ConstantUtil.EMPTY_STRING) ){
                if(firstLine){
                    tokens = inputLine.split(ConstantUtil.HTTP_EMPTY_SEP);
                    method = tokens[METHOD_KEY_POSITION];
                    firstLine = false;
                } else {
                    final String[] array = inputLine.split(ConstantUtil.getHttpSeparator(HTTP_HEADER_SEP));
                    tmpMap.put(array[METHOD_KEY_POSITION], array[URI_VALUE_POSITION]);
                }
            }

            final HttpMethod httpMethod = HttpMethod.getByName(method);

            if(Objects.nonNull(httpMethod) && Objects.nonNull(tokens)){
                final HttpMessage httpMessage = new HttpMessage(httpMethod, URI.create(tokens[URI_VALUE_POSITION]),
                        HttpVersion.getByValue(tokens[VERSION_POSITION]), tmpMap);
                switch (httpMethod){
                    case GET:
                        result = processorFactory.processGet(httpMessage, out);
                        break;
                    case POST:
                        result = processorFactory.processPost(httpMessage, in);
                        break;
                    case HEAD:
                    case PUT:
                    case DELETE:
                    case CONNECT:
                    case TRACE:
                    case OPTIONS:
                    default:
                        processorFactory.processDefault(httpMessage, out);
                }

            }

            return result;
        } catch (IOException | InterruptedException ex){
            System.err.println("Error talking to " + connection.getRemoteSocketAddress() + " ex= " + ex);
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                System.err.println("Error Closing Connection to " + connection.getRemoteSocketAddress() + " : " + e);
            }
        }

        return result;

    }

}
