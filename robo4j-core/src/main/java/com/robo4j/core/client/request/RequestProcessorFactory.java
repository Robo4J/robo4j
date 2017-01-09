/*
 * Copyright (C)  2016. Miroslav Kopecky
 * This RequestProcessorFactory.java  is part of robo4j.
 *
 *  robo4j is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  robo4j is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.core.client.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.json.simple.parser.ParseException;

import com.robo4j.commons.agent.AgentConsumer;
import com.robo4j.commons.agent.AgentProducer;
import com.robo4j.commons.agent.AgentStatus;
import com.robo4j.commons.agent.AgentStatusEnum;
import com.robo4j.commons.agent.GenericAgent;
import com.robo4j.commons.agent.ReceiverAgent;
import com.robo4j.commons.concurrent.LegoThreadFactory;
import com.robo4j.commons.control.RoboSystemConfig;
import com.robo4j.commons.enums.RegistryTypeEnum;
import com.robo4j.commons.http.RequestHeaderProcessor;
import com.robo4j.commons.logging.SimpleLoggingUtil;
import com.robo4j.commons.registry.RegistryManager;
import com.robo4j.commons.registry.RoboRegistry;
import com.robo4j.commons.unit.BrainUnit;
import com.robo4j.commons.unit.GenericUnit;
import com.robo4j.core.bus.CommandSerialBus;
import com.robo4j.core.client.agent.BrickMainAgent;
import com.robo4j.core.client.command.CommandExecutor;
import com.robo4j.core.client.command.CommandProcessor;
import com.robo4j.core.client.enums.PageEnum;
import com.robo4j.core.client.enums.RequestCommandEnum;
import com.robo4j.core.client.enums.RequestUnitStatusEnum;
import com.robo4j.core.client.enums.RequestUnitTypeEnum;
import com.robo4j.core.client.http.HttpPageLoader;
import com.robo4j.core.client.util.ClientCommException;
import com.robo4j.core.client.util.HttpUtils;
import com.robo4j.core.dto.ClientCommandRequestDTO;
import com.robo4j.core.dto.ClientRequestDTO;
import com.robo4j.core.service.HttpMessageService;
import com.robo4j.core.service.LcdService;
import com.robo4j.core.system.CommandProviderImpl;
import com.robo4j.core.util.ConstantUtil;
import com.robo4j.http.HttpMessage;
import com.robo4j.http.HttpVersion;
import com.robo4j.page.PageParser;

/**
 * Request Factory Should be Singleton Request Factory looks like Unit
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 24.05.2016
 */
public final class RequestProcessorFactory {

	private static final int MAIN_FACTORY_AGENT = 0;
	private static volatile RequestProcessorFactory INSTANCE;
	private final HttpPageLoader pageLoader;
	private final List<GenericAgent> agents;
	private volatile ExecutorService factoryExecutor;
	private volatile AtomicBoolean activeThread;
	private volatile LinkedBlockingQueue<List<ClientCommandRequestDTO>> commandQueue;
	private RegistryManager registryManager;
	private RoboRegistry unitRegistry;
	private LcdService lcdService;
	private HttpMessageService convertService;

	private Map<String, RoboSystemConfig> roboSystemConfig;

	@SuppressWarnings(value = "unchecked")
	private RequestProcessorFactory() {

		this.pageLoader = new HttpPageLoader();
		this.factoryExecutor = Executors.newFixedThreadPool(ConstantUtil.PLATFORM_FACTORY,
				new LegoThreadFactory(ConstantUtil.FACTORY_BUS));

		this.activeThread = new AtomicBoolean(true);
		this.commandQueue = CommandSerialBus.getInstance().getQueue();

		this.agents = new LinkedList<>();
		this.agents.add(MAIN_FACTORY_AGENT, getAgent(new CommandProcessor(activeThread, commandQueue),
				new CommandExecutor(activeThread, new CommandProviderImpl())));
		this.registryManager = RegistryManager.getInstance();

		// TODO :: should be changed
		/* get active configuration */
		this.unitRegistry = registryManager.getRegistryByType(RegistryTypeEnum.UNITS);
		this.roboSystemConfig = new HashMap<>();
		final RoboRegistry<RoboRegistry, RoboSystemConfig> engineRegistry = registryManager
				.getRegistryByType(RegistryTypeEnum.ENGINES);
		final RoboRegistry<RoboRegistry, RoboSystemConfig> sensorsRegistry = registryManager
				.getRegistryByType(RegistryTypeEnum.SENSORS);
		engineRegistry.getRegistry().entrySet().forEach(e -> roboSystemConfig.put(e.getKey(), e.getValue()));
		sensorsRegistry.getRegistry().entrySet().forEach(e -> roboSystemConfig.put(e.getKey(), e.getValue()));

		this.lcdService = (LcdService) registryManager.getItemByRegistry(RegistryTypeEnum.SERVICES, "lcdService");
		this.convertService = (HttpMessageService) registryManager.getItemByRegistry(RegistryTypeEnum.SERVICES,
				"legoSetupService");
	}

	public static RequestProcessorFactory getInstance() {
		if (INSTANCE == null) {
			synchronized (RequestProcessorFactory.class) {
				if (INSTANCE == null) {
					INSTANCE = new RequestProcessorFactory();
				}
			}
		}
		return INSTANCE;
	}

	// TODO : improve registry
	public void deactivate() {
		activeThread.set(false);
	}

	/* default methods */
	@SuppressWarnings(value = "unchecked")
	ProcessorResult activateInner() {

		final RoboRegistry<RoboRegistry, RoboSystemConfig> unitRegistry = registryManager
				.getRegistryByType(RegistryTypeEnum.UNITS);
		unitRegistry.getRegistry().entrySet().stream().map(Map.Entry::getValue).map(GenericUnit.class::cast)
				.filter(u -> u.initLogic().isEmpty()).forEach(u -> u.init(null));

		lcdService.printText("FRONT HAND INIT");
		return new ProcessorResult(RequestUnitTypeEnum.UNIT, RequestUnitStatusEnum.ACTIVE, "AGENT_INTERNAL_INIT");
	}

	ProcessorResult processGet(final HttpMessage httpMessage) throws IOException, InterruptedException {
		RequestUnitStatusEnum result = RequestUnitStatusEnum.ACTIVE;
		final StringBuilder message = new StringBuilder(ConstantUtil.EMPTY_STRING);
		final String generatedMessage;
		if (HttpVersion.containsValue(httpMessage.getVersion())) {
			final URI uri = httpMessage.getUri();
			final List<String> paths = Arrays
					.stream(httpMessage.getUri().getPath().split(ConstantUtil.getHttpSeparator(12)))
					.filter(e -> !e.isEmpty()).collect(Collectors.toList());

			if (paths.size() > ConstantUtil.DEFAULT_VALUE && ConstantUtil.availablePaths.containsAll(paths)) {

				final PageEnum page = PageEnum.getPageEnumByName(paths.get(ConstantUtil.DEFAULT_VALUE).toLowerCase());
				switch (page) {
				case STATUS:
					final ReceiverAgent receiverAgent = (ReceiverAgent) agents.get(MAIN_FACTORY_AGENT);
					generatedMessage = getWebPageByEnum(page, receiverAgent.getCache().toString());
					message.append(HttpUtils.setHeader(HttpUtils.HTTP_HEADER_OK, generatedMessage.length())); // send
																												// a
																												// MIME
																												// header
					message.append(generatedMessage);
					break;
				case EXIT:
					// TODO: // FIXME: 13/11/2016 need to be changed
					activeThread.set(false);
					generatedMessage = pageLoader.getWebPage(page.getPage());
					message.append(HttpUtils.setHeader(HttpUtils.HTTP_HEADER_OK, generatedMessage.length())); // send
																												// a
																												// MIME
																												// header
					message.append(generatedMessage);
					result = RequestUnitStatusEnum.STOP;
					factoryExecutor.shutdown();
					break;
				case SETUP:
					final String data = roboSystemConfig.entrySet().stream()
							.map(convertService::messageByRoboSystemConfig).filter(Objects::nonNull)
							.collect(Collectors.joining());
					generatedMessage = getWebPageByEnum(page, convertService.createTable(data));
					message.append(HttpUtils.setHeader(HttpUtils.HTTP_HEADER_OK, generatedMessage.length()));
					message.append(generatedMessage);
					break;
				default:
					break;
				}
			} else if (uri != null && uri.getQuery() != null && !uri.getQuery().isEmpty()) {
				final List<ClientCommandRequestDTO> resultList = HttpUtils.parseURIQuery(uri.getQuery(),
						ConstantUtil.HTTP_QUERY_SEP);
				commandQueue.put(resultList);
				addAgentMessage(AgentStatusEnum.REQUEST_GET, resultList.toString());
				generatedMessage = getWebPageByEnum(PageEnum.SUCCESS, resultList.toString());
				message.append(HttpUtils.setHeader(HttpUtils.HTTP_HEADER_OK, generatedMessage.length())); // send
																											// a
																											// MIME
																											// header
				message.append(generatedMessage);
			} else {
				generatedMessage = pageLoader.getWebPage(PageEnum.WELCOME.getPage());
				message.append(HttpUtils.setHeader(HttpUtils.HTTP_HEADER_OK, generatedMessage.length())); // send
																											// a
																											// MIME
																											// header
				message.append(generatedMessage);
			}

		} else {
			activeThread.set(false);
			generatedMessage = pageLoader.getWebPage(PageEnum.ERROR.getPage());
			message.append(HttpUtils.setHeader(HttpUtils.HTTP_HEADER_NOT_ALLOWED, generatedMessage.length()));
			message.append(generatedMessage);
			result = RequestUnitStatusEnum.STOP;
		}
		return new ProcessorResult(RequestUnitTypeEnum.UNIT, result, message.toString());
	}

	ProcessorResult processPost(final HttpMessage httpMessage, final BufferedReader in) {
		RequestUnitStatusEnum status = null;
		boolean takenAction = false;

		char[] buffer = new char[RequestHeaderProcessor.getContentLength(httpMessage.getHeader())];
		if (buffer.length != ConstantUtil.DEFAULT_VALUE) {
			try {
				in.read(buffer);

				final ClientRequestDTO parsedRequest = HttpUtils.transformToCommands(String.valueOf(buffer));

				/* POST input parser: commands, brainUnits */
				if (parsedRequest.getCommands().size() > ConstantUtil.DEFAULT_VALUE) {
					addAgentMessage(AgentStatusEnum.REQUEST_POST, parsedRequest.getCommands().toString());
					SimpleLoggingUtil.debug(getClass(), "commands request: " + parsedRequest.getCommands());
					commandQueue.put(parsedRequest.getCommands());
					status = RequestUnitStatusEnum.ACTIVE;
					takenAction = true;
				}

				if (parsedRequest.getUnits().size() > ConstantUtil.DEFAULT_VALUE) {
					parsedRequest.getUnits().forEach(u -> {
						RoboSystemConfig du = unitRegistry.getByName(u.getName());
						if (du instanceof BrainUnit) {
							((BrainUnit) du).setActive(u.getActive());
							if (!u.getActive()) {
								try {
									commandQueue.put(Collections
											.singletonList(new ClientCommandRequestDTO(RequestCommandEnum.STOP)));
								} catch (InterruptedException e) {
									SimpleLoggingUtil.error(getClass(), "BrainUnit: " + " no stop");
								}
							}
						}
					});

					status = RequestUnitStatusEnum.ACTIVE;
					takenAction = true;
					SimpleLoggingUtil.debug(getClass(),
							"POST UNITS  takenAction: " + takenAction + " status: " + status);
				}

				if (!takenAction) {
					SimpleLoggingUtil.error(getClass(), "POST IS BROKEN: " + parsedRequest);
				}

			} catch (IOException | ParseException | InterruptedException e) {
				throw new ClientCommException("POST request issue:", e);
			}
		}

		ProcessorResult result = parserProcessorResult(status, takenAction);
		SimpleLoggingUtil.debug(getClass(), "POST result = " + result);
		return result;
	}

	private ProcessorResult parserProcessorResult(RequestUnitStatusEnum status, boolean takenAction) {
		return new ProcessorResult(RequestUnitTypeEnum.UNIT, takenAction ? status : RequestUnitStatusEnum.STOP,
				"No Information about POST");
	}

	public void processInternalCommand(final ClientCommandRequestDTO clientCommandRequestDTO) {
		try {
			commandQueue.put(Collections.singletonList(clientCommandRequestDTO));
		} catch (InterruptedException e) {
			throw new ClientCommException("InternalCommand issue:", e);
		}
	}

	ProcessorResult processDefault(final HttpMessage httpMessage) throws IOException {

		final StringBuilder message = new StringBuilder(pageLoader.getWebPage(PageEnum.ERROR.getPage()));
		if (HttpVersion.containsValue(httpMessage.getVersion())) {
			message.append(HttpUtils.setHeader(HttpUtils.HTTP_HEADER_NOT, message.length())); // send
																								// a
																								// MIME
																								// header
		} else {
			message.append(HttpUtils.setHeader(HttpUtils.HTTP_HEADER_NOT_ALLOWED, message.length()));
		}

		return new ProcessorResult(RequestUnitTypeEnum.UNIT, RequestUnitStatusEnum.STOP, message.toString());
	}

	// Private Methods
	private String getWebPageByEnum(final PageEnum page, final String input) throws IOException {
		final Map<String, String> valueMap = new HashMap<>();

		if (Objects.nonNull(page.getVariables())) {
			page.getVariables().forEach(e -> valueMap.put(e, input));
		}

		return PageParser.parseAndReplace(pageLoader.getWebPage(page.getPage()), valueMap);
	}

	private void addAgentMessage(final AgentStatusEnum type, final String message) {
		final AgentStatus<String> agentStatus = new AgentStatus<>(type);
		agentStatus.addMessage(message);
		ReceiverAgent receiverAgent = (ReceiverAgent) agents.get(MAIN_FACTORY_AGENT);
		receiverAgent.addStatus(agentStatus);
	}

	private BrickMainAgent getAgent(final AgentProducer producer, final AgentConsumer consumer) {
		final BrickMainAgent result = new BrickMainAgent(factoryExecutor, producer, consumer);
		final AgentStatus status = result.activate();
		return result;
	}
}
