package com.structural.adapter;

public interface Cable {

	boolean isConnected();

	void transmit(String message);

	void connectToPort(Port port);

	void plugCable(Cable cable);

	String getMessage();

}
