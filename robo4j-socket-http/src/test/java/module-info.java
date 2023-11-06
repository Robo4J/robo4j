module robo4j.http.test {
    requires robo4j.core;
    requires robo4j.http;

    requires org.junit.jupiter;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;


    exports com.robo4j.socket.http.test.utils;
    exports com.robo4j.socket.http.test.units.config to robo4j.core;
    exports com.robo4j.socket.http.test.units.config.codec to robo4j.http;
    exports com.robo4j.socket.http.test.codec to robo4j.http;

    opens com.robo4j.socket.http.test to org.junit.platform.commons;
    opens com.robo4j.socket.http.test.units.config.codec to org.junit.platform.commons;
    opens com.robo4j.socket.http.test.utils to org.junit.platform.commons;
    opens com.robo4j.socket.http.test.message to org.junit.platform.commons;
    opens com.robo4j.socket.http.test.units to org.junit.platform.commons;
    opens com.robo4j.socket.http.test.json to org.junit.platform.commons;
    opens com.robo4j.socket.http.test.request to org.junit.platform.commons;

}
