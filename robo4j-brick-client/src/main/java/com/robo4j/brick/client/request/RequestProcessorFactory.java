/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This RequestProcessorFactory.java is part of robo4j.
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


import com.robo4j.brick.client.agent.BrickMainAgent;
import com.robo4j.brick.client.command.CommandExecutor;
import com.robo4j.brick.client.command.CommandProcessor;
import com.robo4j.brick.client.http.HttpException;
import com.robo4j.brick.client.http.HttpMessage;
import com.robo4j.brick.client.http.HttpPageLoader;
import com.robo4j.brick.client.http.HttpVersion;
import com.robo4j.brick.client.util.ClientCommException;
import com.robo4j.brick.client.util.HttpUtils;
import com.robo4j.brick.dto.ClientRequestDTO;
import com.robo4j.brick.system.CommandProviderImpl;
import com.robo4j.brick.util.ConstantUtil;
import com.robo4j.brick.util.QueryElement;
import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.AgentStatusEnum;
import com.robo4j.commons.concurrent.LegoThreadFactory;
import com.robo4j.commons.http.RequestHeaderProcessor;
import com.robo4j.page.PageParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Request Factory Should be Singleton
 * Created by miroslavkopecky on 24/05/16.
 */
final class RequestProcessorFactory {

    private static final int POST_COMMAND_SEP = 2;
    private static volatile  RequestProcessorFactory INSTANCE;
    private volatile ExecutorService factoryExecutor;
    private volatile AtomicBoolean activeThread;
    private volatile LinkedBlockingQueue<List<ClientRequestDTO>> commandQueue;
    private final HttpPageLoader pageLoader;
    private final BrickMainAgent agent;

    private RequestProcessorFactory(){
        this.pageLoader = new HttpPageLoader();
        this.factoryExecutor = Executors.newFixedThreadPool(ConstantUtil.PLATFORM_FACTORY,
                new LegoThreadFactory(ConstantUtil.FACTORY_BUS));

        this.activeThread = new AtomicBoolean(true);
        this.commandQueue = new LinkedBlockingQueue<>();

        this.agent = getAgent(new CommandProcessor(activeThread, commandQueue),
                new CommandExecutor(activeThread, new CommandProviderImpl()));
    }

    static RequestProcessorFactory getInstance(){
        if(INSTANCE == null){
            synchronized (RequestProcessorFactory.class){
                if(INSTANCE == null){
                    INSTANCE = new RequestProcessorFactory();
                }
            }
        }
        return INSTANCE;
    }


    String processGet(final HttpMessage httpMessage, final Writer out) throws IOException, InterruptedException {

        String result = ConstantUtil.ACTIVE;

        if(HttpVersion.containsValue(httpMessage.getVersion())){
            HttpUtils.setHeader(out, HttpUtils.HTTP_HEADER_OK);  // send a MIME header
            final URI uri  = httpMessage.getUri();
            final List<String> paths = Arrays.asList(httpMessage.getUri().getPath().split(ConstantUtil.getHttpSeparator(12))).stream()
                    .filter(e -> !e.isEmpty())
                    .collect(Collectors.toList());

            if(paths.size() > ConstantUtil.DEFAULT_VALUE && ConstantUtil.availablePaths.containsAll(paths)){
                switch (paths.get(ConstantUtil.DEFAULT_VALUE).toLowerCase()){
                    case ConstantUtil.STATUS:
                        out.write(getStatusWebPage(agent.getCache().toString()));
                        break;
                    case ConstantUtil.EXIT:
                        activeThread.set(false);
                        out.write(pageLoader.getWebPage(HttpUtils.PAGE_EXIT));
                        result = ConstantUtil.EXIT;
                    default:
                        break;
                }
            } else if(uri != null && uri.getQuery() != null && !uri.getQuery().isEmpty()){
                final List<ClientRequestDTO> resultList = parseURIQuery(uri.getQuery(), ConstantUtil.HTTP_QUERY_SEP);
                commandQueue.put(resultList);
                addAgentMessage(AgentStatusEnum.REQUEST_GET, resultList.toString());
                out.write(getSuccessWebPage(resultList.toString()));
            } else {
                out.write(pageLoader.getWebPage(HttpUtils.PAGE_WELCOME));
            }

        } else {
            activeThread.set(false);
            HttpUtils.setHeader(out, HttpUtils.HTTP_HEADER_NOT_ALLOWED);
            out.write(pageLoader.getWebPage(HttpUtils.PAGE_ERROR));
            result = ConstantUtil.EXIT;
        }
        out.flush();
        return result;
    }

    String processPost(final HttpMessage httpMessage, final BufferedReader in){
        final JSONParser parser = new JSONParser();
        char[] buffer = new char[RequestHeaderProcessor.getContentLength(httpMessage.getHeader())];
        if(buffer.length != ConstantUtil.DEFAULT_VALUE){
            try {
                in.read(buffer);
                final JSONObject request = (JSONObject)parser.parse(String.valueOf(buffer));

                final String requestedCommands = request.containsKey(HttpUtils.HTTP_COMMAND) ?
                        request.get(HttpUtils.HTTP_COMMAND).toString() :
                        ConstantUtil.EMPTY_STRING;
                final String delimiter = ConstantUtil.getHttpSeparator(POST_COMMAND_SEP);

                final List<ClientRequestDTO> resultList = parseURIQuery(requestedCommands, delimiter);
                if(resultList.size() > ConstantUtil.DEFAULT_VALUE){
                    addAgentMessage(AgentStatusEnum.REQUEST_POST, request.toString());
                    commandQueue.put(resultList);
                    return ConstantUtil.ACTIVE;
                } else {
                    return ConstantUtil.EXIT;
                }
            } catch (IOException | ParseException | InterruptedException e) {
                throw new HttpException("POST request issue", e);
            }
        } else {
            return ConstantUtil.EXIT;
        }
    }

    void processDefault(final HttpMessage httpMessage, final Writer out) throws IOException{
        if(HttpVersion.containsValue(httpMessage.getVersion())){
            HttpUtils.setHeader(out, HttpUtils.HTTP_HEADER_NOT);  // send a MIME header
        } else {
            HttpUtils.setHeader(out, HttpUtils.HTTP_HEADER_NOT_ALLOWED);
        }
        out.write(pageLoader.getWebPage(HttpUtils.PAGE_ERROR));
        out.flush();
    }


    //Private Methods

    private String getSuccessWebPage(final String input) throws IOException{
        final Map<String, String > valuesMap = new HashMap<>();
        valuesMap.put(HttpUtils.HTTP_COMMAND, input);
        return PageParser.parseAndReplace(
                pageLoader.getWebPage(HttpUtils.PAGE_SUCCESS),
                valuesMap);
    }

    private String getStatusWebPage(final String input) throws IOException {
        final Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put(HttpUtils.HTTP_AGENT_CACHE, input);
        return PageParser.parseAndReplace(
                pageLoader.getWebPage(HttpUtils.PAGE_STATUS),
                valuesMap);

    }

    private void addAgentMessage(final AgentStatusEnum type, final String message){
        final AgentStatus<String> agentStatus = new AgentStatus<>(type);
        agentStatus.addMessage(message);
        agent.addStatus(agentStatus);
    }

    private List<ClientRequestDTO> parseURIQuery(final String uriQuery, final String delimiter){
        return Arrays.asList(uriQuery
                .split(delimiter))
                .stream()
                .filter(e -> !e.isEmpty())
                .map(QueryElement::new)
                .map(ClientRequestDTO::new)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private BrickMainAgent getAgent(final AgentProducer producer, final AgentConsumer consumer){
        final BrickMainAgent result = new BrickMainAgent(factoryExecutor, producer, consumer);
        final AgentStatus status = result.activate();
        return result;
    }
}
