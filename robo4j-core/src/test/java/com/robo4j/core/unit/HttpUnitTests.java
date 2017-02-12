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
package com.robo4j.core.unit;

import org.junit.Assert;
import org.junit.Test;

public class HttpUnitTests {
	@Test
	public void testArrayDecoder() {
		TestArrayDecoder arrayDecoder = new TestArrayDecoder();
		String[] array = arrayDecoder.decode("[]{}}{");
		Assert.assertArrayEquals(new String[] {""}, array);
		Assert.assertArrayEquals(new String[] {"Lalaa", "Lalala"}, arrayDecoder.decode("Lalaa,Lalala"));
	}
	
	@Test
	public void testArrayEncoder() {
		TestArrayEncoder arrayEncoder = new TestArrayEncoder();
		String json = arrayEncoder.encode(new String[] {"A", "B", "C"});
		Assert.assertEquals("{array:[A,B,C]}", json);
	}

	@Test
	public void testHttpCodecRegistry() {
		HttpCodecRegistry registry = new HttpCodecRegistry("com.robo4j.core.unit");
		HttpEncoder<String[]> encoder = registry.getEncoder(String[].class);
		HttpDecoder<String[]> decoder = registry.getDecoder(String[].class);	
		Assert.assertNotNull(encoder);
		Assert.assertNotNull(decoder);
		
		String [] originalData = new String[] {"A", "B", "C"};
		String encoded = encoder.encode(originalData);
		String [] decoded = decoder.decode(encoded);
		
		Assert.assertArrayEquals(originalData, decoded);
	}
	
	@Test
	public void testHttpCodecRegistryCodec() {
		HttpCodecRegistry registry = new HttpCodecRegistry("com.robo4j.core.unit");
		HttpEncoder<String> encoder = registry.getEncoder(String.class);
		HttpDecoder<String> decoder = registry.getDecoder(String.class);
		Assert.assertNotNull(encoder);
		Assert.assertNotNull(decoder);
		
		String originalData = "Oh my god, it's full of stars";
		String encoded = encoder.encode(originalData);
		String decoded = decoder.decode(encoded);
		
		Assert.assertEquals(originalData, decoded);
	}
	
}
