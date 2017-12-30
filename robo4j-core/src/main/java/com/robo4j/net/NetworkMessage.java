package com.robo4j.net;

import java.io.Serializable;

/**
 * 
 */
public class NetworkMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private String id;
	private Serializable message;
	
	public NetworkMessage() {
	}
	
	public NetworkMessage(String id, Serializable message) {
		this.setId(id);
		this.setMessage(message);
	}

	public Serializable getMessage() {
		return message;
	}

	public void setMessage(Serializable message) {
		this.message = message;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
