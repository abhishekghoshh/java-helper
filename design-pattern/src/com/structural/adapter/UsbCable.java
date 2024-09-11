package com.structural.adapter;

import java.util.LinkedList;
import java.util.Queue;

public class UsbCable implements Cable {

	private final Queue<String> messages;

	private Port port;
	private Cable cable;

	public UsbCable() {
		this.messages = new LinkedList<>();
	}

	@Override
	public void transmit(String message) {
		if (cable == null)
			messages.offer(message);
		else
			cable.transmit(message);
	}

	@Override
	public void connectToPort(Port port) {
		this.port = port;
	}

	@Override
	public void plugCable(Cable cable) {
		this.cable = cable;
	}

	@Override
	public boolean isConnected() {
		return null != port;
	}

	@Override
	public String getMessage() {
		return null;
	}

}
