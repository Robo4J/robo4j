/*
 * Copyright (c) 2014, 2025, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.prometheus;

import com.robo4j.BlockingTrait;
import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.prometheus.model.MetricsElement;
import com.robo4j.prometheus.model.MetricsType;
import com.robo4j.scheduler.RoboThreadFactory;
import com.robo4j.util.StringConstants;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.prometheus.metrics.core.datapoints.DataPoint;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.core.metrics.Info;
import io.prometheus.metrics.core.metrics.StateSet;
import io.prometheus.metrics.core.metrics.Summary;
import io.prometheus.metrics.expositionformats.ExpositionFormats;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.module.ModuleDescriptor;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.robo4j.RoboBuilder.KEY_BLOCKING_POOL_SIZE;
import static com.robo4j.RoboBuilder.KEY_SCHEDULER_POOL_SIZE;
import static com.robo4j.RoboBuilder.KEY_WORKER_POOL_SIZE;

/**
 * Unit is used to write to the Prometheus Metrics
 */
@BlockingTrait
public class PrometheusMetricsUnit extends RoboUnit<MetricsElement> {

    public static final String NAME = "consumer";
    /**
     * Configuration key for the maximum thread size for the metrics server thread pool.
     */
    public static final String PROP_METRICS_POOL_SIZE = "metricsPoolSize";

    public static final String PROP_METRICS_JVM_ENABLE = "metricsJvmEnable";
    /**
     * Configuration key for the default metrics http server port
     */
    public static final String PROP_METRICS_HTTP_PORT = "metricsHttpPort";
    public static final String PROP_METRICS_URI = "metricsUri";
    public static final String PROP_LABEL = "metricsLabel";
    public static final String PROP_JPMS_LIST_MODULES = "jpmsListModules";
    public static final String PROP_JPMS_URI = "jpmsUri";
    public static final String PROP_JPMS_FILTER_MODULES = "jpmsFilterModules";


    private static final String METRICS_LABEL_SOURCE = "source";
    private static final int DEFAULT_METRICS_POOL_SIZE = 3;
    private static final int DEFAULT_METRICS_HTTP_PORT = 8080;
    private static final String DEFAULT_METRICS_LABEL = "robo4j_metrics";

    /**
     * Java Platform Modules System
     */
    private static final Boolean DEFAULT_JPMS_LIST_MODULES = false;
    private static final String DEFAULT_JPMS_URI = "/modules";
    /**
     * Number of the TCP request to be cached
     */
    private static final int DEFAULT_METRICS_HTTP_BACKLOG = 0;
    private static final int DEFAULT_METRICS_HTTP_STOP_DELAY_SEC = 0;
    private static final boolean DEFAULT_METRICS_JVM_ENABLE = true;


    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusMetricsUnit.class);
    private static final String THREAD_GROUP_METRICS_POOL = "robo4j-metrics-pool";
    // TODO : allow multiple metrics URIs
    private static final String DEFAULT_METRICS_URI = "/metrics";
    private final Map<String, DataPoint> metricsMap = new HashMap<>();
    private final PrometheusRegistry prometheusRegistry = PrometheusRegistry.defaultRegistry;
    private HttpServer httpServer;
    private String metricsLabel;
    private Boolean jpmsListModules;
    private String jpmsUri;
    private List<String> jpmsFilterModules;

    /**
     * Constructor.
     *
     * @param context desired Robo context
     * @param id      id of RoboUnit
     */
    public PrometheusMetricsUnit(RoboContext context, String id) {
        super(MetricsElement.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        LOGGER.info("Initializing Prometheus metrics unit:{}", getState());
        var metricsUri = configuration.getString(PROP_METRICS_URI, DEFAULT_METRICS_URI);
        var metricsHttpPort = configuration.getInteger(PROP_METRICS_HTTP_PORT, DEFAULT_METRICS_HTTP_PORT);
        var metricsPoolSize = configuration.getInteger(PROP_METRICS_POOL_SIZE, DEFAULT_METRICS_POOL_SIZE);
        var metricsJvmEnable = configuration.getBoolean(PROP_METRICS_JVM_ENABLE, DEFAULT_METRICS_JVM_ENABLE);
        metricsLabel = configuration.getString(PROP_LABEL, DEFAULT_METRICS_LABEL);
        jpmsListModules = configuration.getBoolean(PROP_JPMS_LIST_MODULES, DEFAULT_JPMS_LIST_MODULES);
        jpmsUri = configuration.getString(PROP_JPMS_URI, DEFAULT_JPMS_URI);
        var jpmsFilterModulesString = configuration.getString(PROP_JPMS_FILTER_MODULES, StringConstants.EMPTY);
        jpmsFilterModules = jpmsFilterModulesString.equals(StringConstants.EMPTY) ?
                Collections.emptyList() : Arrays.stream(jpmsFilterModulesString.trim().split(StringConstants.COMMA))
                .map(String::trim).collect(Collectors.toList());

        if (metricsJvmEnable) {
            JvmMetrics.builder().register(prometheusRegistry);
        }

        var serverThreadFactory = new RoboThreadFactory
                .Builder(THREAD_GROUP_METRICS_POOL)
                .addThreadPrefix(THREAD_GROUP_METRICS_POOL)
                .build();
        var metricsExecutor = Executors.newFixedThreadPool(metricsPoolSize, serverThreadFactory);
        this.httpServer = getHttpServer(metricsExecutor, metricsUri, metricsHttpPort);
        this.httpServer.start();
        setState(LifecycleState.INITIALIZED);
        LOGGER.info(" Prometheus metrics unit:{}", getState());
    }

    @Override
    public void start() {
        setState(LifecycleState.STARTING);
        var systemConfiguration = getContext().getConfiguration();
        createPrometheusInfo(prometheusRegistry, "robo4j_system_name", getContext().getId());
        addPrometheusSystemPoolGauge("robo4j_system_available_processors", Runtime.getRuntime().availableProcessors());
        addPrometheusSystemPoolGauge("robo4j_pool_size_blocking", systemConfiguration.getInteger(KEY_BLOCKING_POOL_SIZE, 0));
        addPrometheusSystemPoolGauge("robo4j_pool_size_worker", systemConfiguration.getInteger(KEY_WORKER_POOL_SIZE,0));
        addPrometheusSystemPoolGauge("robo4j_pool_size_scheduler", systemConfiguration.getInteger(KEY_SCHEDULER_POOL_SIZE,0));
        setState(LifecycleState.STARTED);
    }

    public void addPrometheusSystemPoolGauge(String gaugeName, int value) {
        var systemId = getContext().getId();
        var metricsElement = new MetricsElement(gaugeName, MetricsType.GAUGE, systemId, value);
        var gauge = createPrometheusGauge(prometheusRegistry, metricsElement);
        gauge.labelValues(systemId).set(value);
        metricsMap.put(gaugeName, gauge);
    }

    @Override
    public void stop() {
        setState(LifecycleState.STOPPING);
        httpServer.stop(DEFAULT_METRICS_HTTP_STOP_DELAY_SEC);
        setState(LifecycleState.STOPPED);
    }

    @Override
    public void onMessage(MetricsElement element) {
        if (metricsMap.containsKey(element.name())) {
            var dataPoint = metricsMap.get(element.name());
            updateMetricsMapByDataPoint(dataPoint, element);
            LOGGER.info("UPDATE: Received element {}", element);
        } else {
            var metricDataPoint = createPrometheusMetricsElement(element);
            metricsMap.put(element.name(), metricDataPoint);
            LOGGER.info("CREATE: Received element {}", element);
        }
    }

    private void updateMetricsMapByDataPoint(DataPoint dataPoint, MetricsElement element) {
        switch (dataPoint) {
            case Counter counter -> counter.labelValues(element.label()).inc();
            case Gauge gauge -> gauge.labelValues(element.label()).set((double) element.value());
            case Histogram histogram -> histogram.labelValues(element.label()).observe((double) element.value());
            case Summary summary -> summary.labelValues(element.label()).observe((double) element.value());
            case StateSet stateSet -> {
                var value = (boolean) element.value();
                if (value) {
                    stateSet.setTrue("active");
                } else {
                    stateSet.setFalse("active");
                }

            }
            default -> LOGGER.error("Unexpected dataPoint:'{}, element:'{}''", dataPoint, element);
        }
    }

    /**
     * Info object is not implemented
     * // TODO : Implement prometheus Info
     *
     * @param element robo4j MetricsElement
     * @return prometheus DataPoint
     * @see DataPoint
     */
    private DataPoint createPrometheusMetricsElement(MetricsElement element) {
        return switch (element.type()) {
            case COUNTER -> createPrometheusCounter(prometheusRegistry, element);
            case GAUGE -> createPrometheusGauge(prometheusRegistry, element);
            case HISTOGRAM -> createPrometheusHistogram(prometheusRegistry, element);
            case SUMMARY -> createPrometheusSummary(prometheusRegistry, element);
            case STATE_SET -> createPrometheusStateSet(prometheusRegistry, element);
        };
    }

    /**
     * Counter elements increments over time, example unexpected values
     *
     * @param registry prometheus registry
     * @param element  robo4j metrics element
     * @return prometheus Counter
     * @see Counter
     */
    private Counter createPrometheusCounter(PrometheusRegistry registry, MetricsElement element) {
        return Counter.builder()
                .name(element.name())
                .help(createMetricsHelp(element))
                .labelNames(METRICS_LABEL_SOURCE)
                .register(registry);
    }

    /**
     * Gauge represent measurement that may increase or decrease over time
     *
     * @param registry prometheus registry
     * @param element  robo4j metrics element
     * @return prometheus Gauge
     * @see Gauge
     */
    private Gauge createPrometheusGauge(PrometheusRegistry registry, MetricsElement element) {
        return Gauge.builder()
                .name(element.name())
                .help(createMetricsHelp(element))
                .labelNames(METRICS_LABEL_SOURCE)
                .register(registry);
    }

    /**
     * Histogram used for track distribution of desired values over in considered time window, example latencies
     *
     * @param registry prometheus registry
     * @param element  robo4j metrics element
     * @return prometheus Histogram
     * @see Histogram
     */
    private Histogram createPrometheusHistogram(PrometheusRegistry registry, MetricsElement element) {
        return Histogram.builder()
                .name(element.name())
                .help(createMetricsHelp(element))
                .labelNames(METRICS_LABEL_SOURCE)
                .register(registry);

    }

    /**
     * currently configured following quantiles:
     * 50th percentile (0.5): approximate median with 5% error shows difference between expected value
     * 90th percentile (0.9): 90% of values are above the current value with 1% error shows difference between expected value
     *
     * @param registry prometheus registry
     * @param element  robo4j metrics element
     * @return prometheus Summary
     * @see Summary
     */
    private Summary createPrometheusSummary(PrometheusRegistry registry, MetricsElement element) {
        return Summary.builder()
                .name(element.name())
                .help(createMetricsHelp(element))
                .labelNames(METRICS_LABEL_SOURCE)
                .quantile(0.5, 0.05)
                .quantile(0.9, 0.01)
                .register(registry);
    }

    /**
     * Info provides not changed information about the considered Robo4jUnit or expected measurement
     *
     * @param registry prometheus registry
     * @param name     robo4j metrics element
     * @see Info
     */
    private void createPrometheusInfo(PrometheusRegistry registry, String name, String value) {
        var infoElement = Info.builder()
                .name(name)
                .help("Robo4j app metrics")
                .labelNames(METRICS_LABEL_SOURCE, "name")
                .register(registry);
        infoElement.addLabelValues(this.id(), value);
    }

    private StateSet createPrometheusStateSet(PrometheusRegistry registry, MetricsElement element) {
        return StateSet.builder()
                .name(element.name())
                .help(createMetricsHelp(element))
                .labelNames(METRICS_LABEL_SOURCE)
                .states("function")
                .register(registry);
    }

    private record JpmsModuleInfo(String moduleName, Set<ModuleDescriptor.Modifier> modifiers) {
    }

    private HttpServer getHttpServer(ExecutorService executorService, String metricsUri, Integer metricsHttpPort) throws ConfigurationException {
        try {
            var server = HttpServer.create(new InetSocketAddress(metricsHttpPort), DEFAULT_METRICS_HTTP_BACKLOG);
            server.createContext(metricsUri, new MetricsPrometheusHandler());
            if (jpmsListModules) {
                var modulesString = ModuleLayer.boot().modules().stream()
                        .map(module -> new JpmsModuleInfo(module.getName(), module.getDescriptor().modifiers()))
                        .filter(moduleInfo -> jpmsFilterModules.isEmpty() || jpmsFilterModules.stream().noneMatch(filterModuleName -> moduleInfo.moduleName().startsWith(filterModuleName)))
                        .map(moduleInfo -> """
                                {"name":"$moduleName", "modifiers":[$modifiers]}""".replace("$moduleName", moduleInfo.moduleName())
                                .replace("$modifiers", moduleInfo.modifiers().stream()
                                        .map(ModuleDescriptor.Modifier::name).map(name -> "\"" + name + "\"").collect(Collectors.joining(StringConstants.COMMA))))
                        .collect(Collectors.joining(StringConstants.COMMA + StringConstants.LINE_BREAK));
                server.createContext(jpmsUri, new JpmsHandler(modulesString));
            }
            server.setExecutor(executorService);
            LOGGER.info("creating metrics server, uri:{} port:{}", metricsUri, metricsHttpPort);
            return server;
        } catch (IOException e) {
            throw new ConfigurationException(e.getMessage(), e);
        }
    }

    private String createMetricsHelp(MetricsElement element) {
        return """
                Robo4j Unit metrics element id=%s, type=%s, label=%s""".formatted(element.name(), element.type(), element.label());
    }

    private class MetricsPrometheusHandler implements HttpHandler {

        MetricsPrometheusHandler() {
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            var byteArrayOutputStream = createByteArrayOutputStreamByPrometheusRegistry();
            var os = httpExchange.getResponseBody();
            httpExchange.sendResponseHeaders(200, byteArrayOutputStream.size());
            os.write(byteArrayOutputStream.toByteArray());
            httpExchange.close();
        }

        private ByteArrayOutputStream createByteArrayOutputStreamByPrometheusRegistry() throws IOException {
            var byteArrayOutputStream = new ByteArrayOutputStream();
            ExpositionFormats.init().getPrometheusTextFormatWriter().write(byteArrayOutputStream, prometheusRegistry.scrape());
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            return byteArrayOutputStream;
        }
    }

    private class JpmsHandler implements HttpHandler {
        private final String content;

        JpmsHandler(String modules) {
            this.content = """
                    {
                    "app": "Robo4j-app: used modules",
                    "modules": [
                    $modules
                    ]
                    }
                    """.replace("$modules", modules);
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            var os = httpExchange.getResponseBody();
            httpExchange.sendResponseHeaders(200, content.length());
            os.write(content.getBytes());
            httpExchange.close();
        }

    }
}
