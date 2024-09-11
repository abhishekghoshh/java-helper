package com.behavioral.mediator;

import java.util.ArrayList;
import java.util.List;

public class ChatMediator implements Mediator {
	private List<Participant> participants = new ArrayList<>();

	@Override
	public void sendMessage(String message, Participant participant) {
		for (Participant participant_ : this.participants) {
			if (participant_ != participant) {
				participant_.receive(message);
			}
		}
	}

	@Override
	public void addUser(Participant participant) {
		this.participants.add(participant);
	}

}
