package com.behavioral.observer;

import java.util.List;

public interface Subscriber {
	void receive(String message);

	String getNotification();

	List<String> getAllNotification();

	boolean hasNotification();

	void printNotification();
}
