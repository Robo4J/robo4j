/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.units.test;

import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class BufferTests {

	public static void main(String[] args) throws Exception {
		example3();
	}

	private static void example3() throws Exception {
		System.out.println("Starting server");
		ServerSocketChannel ssc = ServerSocketChannel.open();

		ssc.socket().bind(new InetSocketAddress(9999));
		ssc.configureBlocking(false);

		String msg = "Local address: " + ssc.socket().getLocalSocketAddress();
		ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());

		while (true) {
			System.out.print(".");
			SocketChannel sc = ssc.accept();
			if (sc != null) {
				System.out.println();
				System.out.println("Received connection from " + sc.socket().getRemoteSocketAddress());
				buffer.rewind();
				sc.write(buffer);
				sc.close();
			} else {
				Thread.sleep(100);

			}
		}
	}

	private static void example2() {
		String[] poem = { "Roses are red", "Violets are blue", "Sugar is sweet", "And so are you" };

		CharBuffer buffer = CharBuffer.allocate(50);
		for (int i = 0; i < poem.length; i++) {
			// Fill the buffer
			for (int j = 0; j < poem[i].length(); j++) {
				buffer.put(poem[i].charAt(j));
			}

			// FLip the buffer so that its content can be read
			buffer.flip();

			// Drain the buffer
			while (buffer.hasRemaining()) {
				System.out.print(buffer.get());
			}
			buffer.clear();

			System.out.println("END");
		}
	}

	private static void example1() {
		Buffer buffer = ByteBuffer.allocate(7);

		System.out.println("capacity; " + buffer.capacity());
		System.out.println("Limit: " + buffer.limit());
		System.out.println("Position: " + buffer.position());
		System.out.println("remaining: " + buffer.remaining());

		System.out.println("changing limit to 5");
		buffer.limit(5);
		System.out.println("capacity; " + buffer.capacity());
		System.out.println("Limit: " + buffer.limit());
		System.out.println("Position: " + buffer.position());
		System.out.println("remaining: " + buffer.remaining());

		System.out.println("changing limit to 3");
		buffer.limit(3);
		System.out.println("Position: " + buffer.position());
		System.out.println("remaining: " + buffer.remaining());
	}

}
