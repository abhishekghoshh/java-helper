package com.structural.adapter;

public class UsbPort implements Port{
	private boolean isConnected = false;
	private Cable cable;

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public boolean connectWith(Cable cable) {
		this.cable = cable;
		this.isConnected = true;
		return isConnected;
	}

	@Override
	public void transmit(String message) {
		this.cable.transmit(message);
	}
}
