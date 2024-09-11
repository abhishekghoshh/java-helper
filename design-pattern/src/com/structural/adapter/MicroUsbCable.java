package com.structural.adapter;

import java.util.LinkedList;
import java.util.Queue;

public class MicroUsbCable implements Cable {
	private final Queue<String> messages;

	@SuppressWarnings("unused")
	private Port port;
	private Cable cable;

	public MicroUsbCable() {
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
	public boolean isConnected() {
		return false;
	}

	@Override
	public void connectToPort(Port port) {

	}

	@Override
	public void plugCable(Cable cable) {
	}

	@Override
	public String getMessage() {
		if (messages.isEmpty())
			return null;
		return messages.poll();
	}
}