package com.structural.adapter;

public interface Port {

	boolean isConnected();

	boolean connectWith(Cable cable);

	void transmit(String message);

}
