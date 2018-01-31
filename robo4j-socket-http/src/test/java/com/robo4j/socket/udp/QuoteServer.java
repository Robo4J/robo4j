package com.robo4j.socket.udp;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class QuoteServer {
    public static void main(String[] args) throws Exception {
        new QuoteServerThread().start();
    }
}
