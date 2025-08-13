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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.prometheus.metrics.core.metrics.Counter;
import io.prometheus.metrics.core.metrics.Gauge;
import io.prometheus.metrics.core.metrics.Histogram;
import io.prometheus.metrics.expositionformats.ExpositionFormats;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Grafana Resource Test
 */
public class SimpleHttpServerExample {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHttpServerExample.class);
    private static final AtomicInteger COUNTER_1 = new AtomicInteger(0);
    private static final AtomicInteger COUNTER_2 = new AtomicInteger(0);

    private static final int MAX_RANGE = 1000;
    private static final int MAX_LATENCY_RANGE = 50;
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/metrics", new MetricsPrometheusHandler());
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
    }

    private static class MetricsPrometheusHandler implements HttpHandler {
        private final PrometheusRegistry prometheusRegistry = PrometheusRegistry.defaultRegistry;
            private final Counter handlerRequestCounter = Counter.builder()
                .name("robo4j_request_total")
                .help("Robo4j total request")
                .labelNames("metrics_prometheus_handler", "status")
                .register(prometheusRegistry);

        private final Gauge handlderRandomMeasurement = Gauge.builder()
                .name("robo4j_random_measurement")
                .help("Robo4j random measurement")
                .register(prometheusRegistry);

        private final Histogram handlerRandomHistogram = Histogram.builder()
                .name("robo4j_random_histogram_sec")
                .help("Tracks HTTP request latency in seconds")
                .labelNames("method")
                .register(prometheusRegistry);


        MetricsPrometheusHandler() {
            JvmMetrics.builder().register(prometheusRegistry);
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            var counter = COUNTER_2.incrementAndGet();
            var randomNumber = RANDOM.nextDouble(MAX_RANGE);
            var randomLatency = RANDOM.nextDouble(MAX_LATENCY_RANGE);
            LOGGER.info("GET request, COUNTER_2:'{}', randomNumber;'{}'", counter, randomNumber);
            handlerRequestCounter.labelValues("GET", "200").inc();
            handlderRandomMeasurement.set(randomNumber);
            handlerRandomHistogram.labelValues("GET").observe(randomLatency);

            var byteArrayOutputStream = new ByteArrayOutputStream();
            ExpositionFormats.init().getPrometheusTextFormatWriter().write(byteArrayOutputStream, prometheusRegistry.scrape());
            byteArrayOutputStream.flush();
            byteArrayOutputStream.close();
            var metrics = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
            LOGGER.info("GET request, metrics:'{}'", metrics);
            var os = httpExchange.getResponseBody();
            httpExchange.sendResponseHeaders(200, byteArrayOutputStream.size());
            os.write(byteArrayOutputStream.toByteArray());
            httpExchange.close();

        }
    }


    private static byte[] getSampleMetricsByBytes(int counter, double randomNumber) {
        return """
                # HELP http_requests_total Total number of HTTP requests received
                # TYPE http_requests_total counter
                http_metrics_request_total{method="GET",route="/metrics",status="200"} %d
                
                # GAUGE
                # HELP http_server_active_connections Current number of active HTTP connections
                # TYPE guage_server_active_connections gauge
                
                # HELP guage_server_usage_bytes Memory usage of the Node.js process in bytes
                # TYPE guage_server_usage_bytes gauge
                guage_server_usage_bytes{type="random"} .2%f
                guage_server_usage_bytes{type="heapTotal"} 21495808
                
                # HISTOGRAM
                # HELP histogram_duration_seconds Histogram of HTTP request durations in seconds
                # TYPE histogram_duration_seconds histogram
                histogram_duration_seconds_bucket{le="0.005"} 11
                """.formatted(counter, randomNumber).getBytes(StandardCharsets.UTF_8);
    }
}
