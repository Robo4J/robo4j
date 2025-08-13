module robo4j.prometheus {

    requires transitive robo4j.core;
    requires jdk.httpserver;
    requires io.prometheus.metrics.core;
    requires io.prometheus.writer.text;
    requires io.prometheus.metrics.instrumentation.jvm;
    requires io.prometheus.metrics.model;
    requires org.slf4j;

    exports com.robo4j.prometheus;
    exports com.robo4j.prometheus.model;
}