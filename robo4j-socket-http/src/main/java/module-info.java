module robo4j.http {
    requires transitive robo4j.core;
    requires org.slf4j;

    exports com.robo4j.socket.http;
    exports com.robo4j.socket.http.codec;
    exports com.robo4j.socket.http.enums;
    exports com.robo4j.socket.http.units;
    exports com.robo4j.socket.http.util;
    exports com.robo4j.socket.http.provider;
    exports com.robo4j.socket.http.message;
    exports com.robo4j.socket.http.dto;
    exports com.robo4j.socket.http.json;

}

