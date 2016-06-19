/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This HttpUtils.java is part of robo4j.
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

package com.robo4j.brick.client.util;

import com.robo4j.brick.client.enums.RequestCommandEnum;
import com.robo4j.brick.dto.ClientRequestDTO;
import com.robo4j.brick.util.ConstantUtil;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * Basic Http constants and utils methods
 *
 * Created by miroslavkopecky on 23/05/16.
 */
public final class HttpUtils {

    public static final String HTTP_HEADER_OK = "HTTP/1.0 200 OK";
    public static final String HTTP_HEADER_NOT =  "HTTP/1.0 501 Not Implemented";
    public static final String HTTP_HEADER_NOT_ALLOWED =  "HTTP/1.0 405 Method Not Allowed";
    public static final String PAGE_WELCOME = "welcome.html";
    public static final String PAGE_ERROR = "error.html";
    public static final String PAGE_SUCCESS = "success.html";
    public static final String PAGE_STATUS = "status.html";
    public static final String PAGE_EXIT = "exit.html";
    public static final String STRING_EMPTY = "";
    public static final String HTTP_COMMAND = "command";
    public static final String HTTP_AGENT_CACHE = "cache";

    /* private values  */
    private static final String NEXT_LINE = "\r\n";
    private static final String HTTP_COMMANDS = "commands";
    private static final int POST_COMMAND_SEP = 2;

    public static String setHeader(String responseCode, int length) throws IOException {
        return new StringBuilder(STRING_EMPTY)
                .append(responseCode).append(NEXT_LINE)
                .append("Date: ")
                .append(LocalDateTime.now())
                .append(NEXT_LINE)
                .append("Server: robo4j-client").append(NEXT_LINE)
                .append("Content-length: ").append(length).append(NEXT_LINE)
                .append("Content-type: text/html; charset=utf-8").append(NEXT_LINE).append(NEXT_LINE)
                .toString();
    }

    /**
     * Parsing received buffer to the list of ClientRequestCommands
     */
    public static List<ClientRequestDTO> transformToCommands(final String buffer) throws ParseException {
        final JSONParser parser = new JSONParser();
        final JSONObject request = (JSONObject)parser.parse(String.valueOf(buffer));
        final List<ClientRequestDTO> result = new LinkedList<>();


        switch (getValidCommandElement(request)){
            case HTTP_COMMAND:
                result.addAll(parseURIQuery(request.get(HTTP_COMMAND).toString(),
                        ConstantUtil.getHttpSeparator(POST_COMMAND_SEP)));
                break;
            case HTTP_COMMANDS:
                System.out.println("RESULT= " + result);
                result.addAll(parseJSONArray((JSONArray) request.get(HTTP_COMMANDS)));
                break;
            default:
                break;
        }

        return result;
    }

    public static List<ClientRequestDTO> parseURIQuery(final String uriQuery, final String delimiter){
        return Arrays.asList(uriQuery
                .split(delimiter))
                .stream()
                .filter(e -> !e.isEmpty())
                .map(ClientRequestDTO::new)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    //Private Methods
    /* commands array is preferred way to address commands */
    private static String getValidCommandElement(final JSONObject request){
        String result = ConstantUtil.EMPTY_STRING;
        if(request.containsKey(HTTP_COMMAND) &&
                !request.get(HTTP_COMMAND).toString().isEmpty()){
            result = HTTP_COMMAND;
        }
        if(request.containsKey(HTTP_COMMANDS) &&
                !((JSONArray) request.get(HTTP_COMMANDS)).isEmpty()){
            result = HTTP_COMMANDS;
        }
        return result;
    }

    @SuppressWarnings(value = "unchecked")
    private static List<ClientRequestDTO> parseJSONArray(final JSONArray jsonArray){
        return (List<ClientRequestDTO>)jsonArray.stream()
                .map(e -> {
                    JSONObject obj = (JSONObject)e;
                    RequestCommandEnum name = RequestCommandEnum.getRequestValue(obj.get("name").toString());
                    return new ClientRequestDTO(name, obj.get("value").toString());
                })
                .collect(Collectors.toCollection(LinkedList::new));
    }


}
