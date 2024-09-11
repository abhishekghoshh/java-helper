package com.behavioral.mediator;

public interface Mediator {

	void sendMessage(String message, Participant participant);
	void addUser(Participant participant);

}
