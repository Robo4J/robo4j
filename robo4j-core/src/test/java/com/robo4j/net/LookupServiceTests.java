package com.robo4j.net;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.robo4j.RoboContext;

public class LookupServiceTests {
	private static final float ALLOWED_HEARTBEAT_MISSES = 22f;

	@Test
	public void testEncodeDecode() throws IOException {
		Map<String, String> metadata = new HashMap<>();
		String id = "MyID";
		int heartBeatInterval = 1234;
		metadata.put("name", "Pretty Human Readable Name");
		RoboContextDescriptor descriptor = new RoboContextDescriptor(id, heartBeatInterval, metadata);
		byte[] encodedDescriptor = HearbeatMessageCodec.encode(descriptor);
		RoboContextDescriptor decodedDescriptor = HearbeatMessageCodec.decode(encodedDescriptor);

		Assert.assertEquals(descriptor.getId(), decodedDescriptor.getId());
		Assert.assertEquals(descriptor.getHeartBeatInterval(), decodedDescriptor.getHeartBeatInterval());
		Assert.assertEquals(descriptor.getMetadata(), decodedDescriptor.getMetadata());
	}

	@Test
	public void testLookup() throws IOException, InterruptedException {
		LookupService service = new LookupServiceImpl(LookupServiceProvider.DEFAULT_MULTICAST_ADDRESS, LookupServiceProvider.DEFAULT_PORT, ALLOWED_HEARTBEAT_MISSES);
		service.start();
		RoboContextDescriptor descriptor = createRoboContextDescriptor();
		ContextEmitter emitter = new ContextEmitter(descriptor, InetAddress.getByName(LookupServiceProvider.DEFAULT_MULTICAST_ADDRESS), LookupServiceProvider.DEFAULT_PORT);
	
		for (int i = 0; i < 10; i++) {
			emitter.emit();
			Thread.sleep(250);
		}
		Map<String, RoboContextDescriptor> discoveredContexts = service.getDiscoveredContexts();
		System.out.println(discoveredContexts);
		Assert.assertEquals(1, discoveredContexts.size());
		RoboContext context = service.getContext(descriptor.getId());
		RemoteRoboContext remoteContext = (RemoteRoboContext) context;
		Assert.assertNotNull(remoteContext.getAddress());
		System.out.println("Address: " + remoteContext.getAddress());
	}

	private static RoboContextDescriptor createRoboContextDescriptor() {
		Map<String, String> metadata = new HashMap<>();
		String id = "MyID";
		int heartBeatInterval = 1234;
		metadata.put("name", "Pretty Human Readable Name");
		return new RoboContextDescriptor(id, heartBeatInterval, metadata);
	}
}
